package org.expns_tracker.ExpnsTracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

@Service
@Log4j2
public class TinkService {
    final TinkProperties tinkProperties;
    final WebClient webClient;
    private final ObjectMapper objectMapper;

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
        bodyValues.add("scope", "user:create authoriazation:grant");

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
        bodyValues.add("scope", "accounts:read,transactions:read,credentials:read,credentials:refresh");

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
                "&market=GB" +
                "&authorization_code=" + authorizationCode;

    }

    public JsonNode getTokens(String code) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("code", code);
        bodyValues.add("client_id", this.tinkProperties.getClientId());
        bodyValues.add("client_secret", this.tinkProperties.getClientSecret());
        bodyValues.add("grant_type", "authorization_code");

        log.info("Exchanging code for tokens...");

        JsonNode response = webClient.post()
                .uri("/api/v1/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Failed to get tokens");
        }

        return response;
    }

    public String refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("refresh_token", refreshToken);
        bodyValues.add("client_id", this.tinkProperties.getClientId());
        bodyValues.add("client_secret", this.tinkProperties.getClientSecret());
        bodyValues.add("grant_type", "refresh_token");

        JsonNode response = this.webClient.post()
                .uri("/api/v1/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null ||  !response.has("access_token")) {
            throw new RuntimeException("Failed to refresh access token");
        }

        return response.get("access_token").asText();
    }

    public JsonNode fetchTransactions(String accessToken) {
        return webClient.get()
                .uri("/data/v2/transactions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}
