package org.expns_tracker.ExpnsTracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.expns_tracker.ExpnsTracker.config.TinkProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TinkServiceTest {

    private MockWebServer mockWebServer;
    private TinkService tinkService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        TinkProperties properties = new TinkProperties();
        properties.setApiUrl(mockWebServer.url("/").toString());
        properties.setClientId("test-client-id");
        properties.setClientSecret("test-client-secret");
        properties.setRedirectUri("http://localhost:8080/callback");

        WebClient.Builder webClientBuilder = WebClient.builder();
        ObjectMapper objectMapper = new ObjectMapper();

        tinkService = new TinkService(properties, webClientBuilder, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void createPermanentUser_Success() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"fake-app-token\", \"expires_in\": 1800}")
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"user_id\": \"d7c6e6884618\", \"external_user_id\": \"my-db-id\"}")
                .addHeader("Content-Type", "application/json"));

        String userId = tinkService.createPermanentUser("my-db-id");

        assertEquals("d7c6e6884618", userId);

        RecordedRequest request1 = mockWebServer.takeRequest();
        assertEquals("/api/v1/oauth/token", request1.getPath());
        assertTrue(request1.getBody().readUtf8().contains("grant_type=client_credentials"));

        RecordedRequest request2 = mockWebServer.takeRequest();
        assertEquals("/api/v1/user/create", request2.getPath());
        assertEquals("Bearer fake-app-token", request2.getHeader("Authorization"));
    }

    @Test
    void createPermanentUser_ClientAccessFailure() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tinkService.createPermanentUser("any-id");
        });

        assertEquals("Failed to get client access token", exception.getMessage());
    }

    @Test
    void createPermanentUser_CreateUserFailure() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"fake-app-token\", \"expires_in\": 1800}")
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tinkService.createPermanentUser("any-id");
        });

        assertEquals("Failed to create user", exception.getMessage());
    }

    @Test
    void generateTinkLinkUrl_Success() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"fake-app-token\"}")
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"code\": \"delegate-code-123\"}")
                .addHeader("Content-Type", "application/json"));

        String url = tinkService.generateTinkLinkUrl("tink-user-id");

        assertNotNull(url);
        assertTrue(url.contains("https://link.tink.com/1.0/transactions/connect-accounts"));
        assertTrue(url.contains("authorization_code=delegate-code-123"));
        assertTrue(url.contains("client_id=test-client-id"));
    }

    @Test
    void generateTinkLinkUrl_Failure() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"fake-app-token\"}")
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        // Execute
        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> tinkService.generateTinkLinkUrl("tink-user-id")
        );

        assertEquals("Failed to delegate access code", exception.getMessage());

    }

    @Test
    void refreshAccessToken_Success() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"new-access-token\", \"expires_in\": 1800}")
                .addHeader("Content-Type", "application/json"));

        String newToken = tinkService.refreshAccessToken("valid-refresh-token");

        assertEquals("new-access-token", newToken);

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("grant_type=refresh_token"));
        assertTrue(body.contains("refresh_token=valid-refresh-token"));
    }

    @Test
    void refreshAccessToken_Failure(){

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> tinkService.refreshAccessToken("valid-refresh-token"));

        assertEquals("Failed to refresh access token", exception.getMessage());
    }

    @Test
    void getTokens_Success() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"new-access-token\", \"expires_in\": 1800 , \"refresh_token\": \"new-refresh-token\"}")
                .addHeader("Content-Type", "application/json"));

        JsonNode response = tinkService.getTokens("new-access-code");

        assertNotNull(response);
        assertEquals("new-access-token", response.get("access_token").asText());
        assertEquals("new-refresh-token", response.get("refresh_token").asText());
    }

    @Test
    void getTokens_Failure() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> tinkService.getTokens("new-access-code"));

        assertEquals("Failed to get tokens", exception.getMessage());
    }

    @Test
    void fetchTransactions_Success() throws InterruptedException {
        String mockResponseBody = """
            {
                "results": [
                    {
                        "transactionId": "t123",
                        "amount": { "value": 150.0, "currencyCode": "GBP" }
                    }
                ]
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponseBody)
                .addHeader("Content-Type", "application/json"));

        String validAccessToken = "valid-access-token-123";
        JsonNode result = tinkService.fetchTransactions(validAccessToken);

        assertNotNull(result);
        assertEquals("t123", result.get("results").get(0).get("transactionId").asText());
        assertEquals(150.0, result.get("results").get(0).get("amount").get("value").asDouble());

        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("/data/v2/transactions", request.getPath());

        assertEquals("Bearer valid-access-token-123", request.getHeader("Authorization"));
    }

    @Test
    void fetchTransactions_Unauthorized() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        assertThrows(Exception.class, () -> {
            tinkService.fetchTransactions("expired-token");
        });
    }
}