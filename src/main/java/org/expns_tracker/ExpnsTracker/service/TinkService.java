package org.expns_tracker.ExpnsTracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.config.TinkProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class TinkService {
    final TinkProperties tinkProperties;
    final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, String> categoryParentCache = new ConcurrentHashMap<>();

    TinkService(TinkProperties tinkProperties, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.tinkProperties = tinkProperties;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(this.tinkProperties.getApiUrl())
                .build();
    }

    public String getClientAppToken() {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("grant_type", "client_credentials");
        bodyValues.add("client_id", this.tinkProperties.getClientId());
        bodyValues.add("client_secret", this.tinkProperties.getClientSecret());
        bodyValues.add("scope", "user:create user:delete authorization:grant");

        JsonNode response = this.webClient.post()
                .uri("api/v1/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.has("access_token")) {
            throw new RuntimeException("Failed to get client access token");
        }

        return response.get("access_token").asText();
    }

    public String createPermanentUser(String externalUserId) {
        String clientToken = getClientAppToken();

        ObjectNode jsonBody = objectMapper.createObjectNode();
        jsonBody.put("market", "GB");
        jsonBody.put("locale", "en_GB");
        jsonBody.put("external_user_id", externalUserId);


        JsonNode response = this.webClient.post()
                .uri("/api/v1/user/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        if (response == null || !response.has("user_id")) {
            throw new RuntimeException("Failed to create user");
        }

        return response.get("user_id").asText();
    }

    public String generateTinkLinkUrl(String tinkUserId){
        String clientToken = getClientAppToken();

        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("user_id", tinkUserId);
        bodyValues.add("actor_client_id", TinkProperties.TINK_LINK_ACTOR_ID);
        bodyValues.add("scope", "authorization:read,authorization:grant,credentials:refresh,credentials:read,credentials:write,providers:read,user:read");
        bodyValues.add("id_hint", "Expense Tracker User");

        JsonNode response = this.webClient.post()
                .uri("/api/v1/oauth/authorization-grant/delegate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.has("code")) {
            throw new RuntimeException("Failed to delegate access code");
        }

        String authorizationCode = response.get("code").asText();

        return "https://link.tink.com/1.0/transactions/connect-accounts" +
                "?client_id=" + this.tinkProperties.getClientId() +
                "&redirect_uri=" + this.tinkProperties.getRedirectUri() +
                "&authorization_code=" + authorizationCode +
                "&test=true";

    }

    public String getUserAccessCode(String tinkUserId){
        String clientToken = getClientAppToken();

        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("user_id", tinkUserId);
        bodyValues.add("scope", "accounts:read,balances:read,transactions:read,provider-consents:read,credentials:read");

        JsonNode response = this.webClient.post()
                .uri("/api/v1/oauth/authorization-grant")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.has("code")) {
            throw new RuntimeException("Failed to get user access code");
        }

        return response.get("code").asText();

    }

    public String getAccessToken(String code) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("code", code);
        bodyValues.add("client_id", this.tinkProperties.getClientId());
        bodyValues.add("client_secret", this.tinkProperties.getClientSecret());
        bodyValues.add("grant_type", "authorization_code");

        log.info("Exchanging code for access token...");

        JsonNode response = webClient.post()
                .uri("/api/v1/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null ||  !response.has("access_token")) {
            throw new RuntimeException("Failed to get access token");
        }

        return response.get("access_token").asText();
    }

    public JsonNode fetchTransactions(String accessToken, String pageToken) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                                    .path("/data/v2/transactions")
                                    .queryParam("bookedDateGte", LocalDate.now().minusMonths(1));

                    if (pageToken != null) {
                        uriBuilder.queryParam("pageToken", pageToken);
                    }

                    return uriBuilder.build();
                    }
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode listCredentials(String accessToken) {
        return webClient.get()
                .uri("/api/v1/credentials/list")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    @PostConstruct
    public void loadCategories() {
        try {
            JsonNode response = webClient.get()
                    .uri("/api/v1/categories")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null) {
                for (JsonNode cat : response) {
                    String id = cat.get("id").asText();
                    String parent = cat.get("parent").asText();

                    categoryParentCache.put(id, parent);
                }
                log.info("Loaded {} Tink categories.", categoryParentCache.size());
            }
        } catch (Exception e) {
            log.error("Failed to load categories", e);
        }
    }

    public String getCategoryParent(String id) {
        return categoryParentCache.getOrDefault(id, null);
    }
}
