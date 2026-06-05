package org.ogee.infrastructure.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.ogee.infrastructure.http.dto.AuctionHistoryResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class StalcraftApiClient {

    private static final String BASE_URL = "https://eapi.stalcraft.net";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public AuctionHistoryResponse getAuctionHistory(
            String region,
            String itemId,
            int limit,
            int offset
    ) {
        String clientId = System.getenv("STALCRAFT_CLIENT_ID");
        String clientSecret = System.getenv("STALCRAFT_CLIENT_SECRET");

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("STALCRAFT_CLIENT_ID is empty");
        }

        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("STALCRAFT_CLIENT_SECRET is empty");
        }

        String url = BASE_URL + "/" + region + "/auction/" + itemId
                + "/history?limit=" + limit
                + "&offset=" + offset;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Client-Id", clientId)
                .header("Client-Secret", clientSecret)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "STALCRAFT API error: HTTP "
                                + response.statusCode()
                                + "\nBody: "
                                + response.body()
                );
            }

            return objectMapper.readValue(response.body(), AuctionHistoryResponse.class);

        } catch (IOException e) {
            throw new RuntimeException("Network/API parse error while calling STALCRAFT API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);
        }
    }
}