package org.expns_tracker.ExpnsTracker.service;

import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.config.TinkProperties;
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

    TinkService(TinkProperties tinkProperties, WebClient.Builder webClientBuilder) {
        this.tinkProperties = tinkProperties;
        this.webClient = webClientBuilder
                .baseUrl(this.tinkProperties.getApiUrl())
                .build();
    }

    public String getTinkLinkUrl() {
        return "https://link.tink.com/1.0/transactions/connect-accounts" +
                "?client_id=" + this.tinkProperties.getClientId() +
                "&redirect_uri=" + this.tinkProperties.getRedirectUri() +
                "&market=GB" +
                "&test=true";
    }

    public String getAccessToken(String code) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("code", code);
        bodyValues.add("client_id", this.tinkProperties.getClientId());
        bodyValues.add("client_secret", this.tinkProperties.getClientSecret());
        bodyValues.add("grant_type", "authorization_code");
        log.info("bodyValues: {}", bodyValues);
        JsonNode response = this.webClient.post()
                .uri("api/v1/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Failed to get token");
        }

        return response.get("access_token").asText();
    }

    public JsonNode fetchTransactions(String accessToken) {
        return webClient.get()
                .uri("data/v2/transactions")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}
