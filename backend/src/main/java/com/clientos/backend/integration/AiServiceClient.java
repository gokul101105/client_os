package com.clientos.backend.integration;

import com.clientos.backend.dto.AiServiceChatRequest;
import com.clientos.backend.dto.AiServiceChatResponse;
import com.clientos.backend.dto.AiServiceSummarizeRequest;
import com.clientos.backend.dto.AiServiceSummarizeResponse;
import com.clientos.backend.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// The only class in this codebase that knows the AI service is a separate
// HTTP service at all — knows its base URL, its shared-secret header, and
// its snake_case JSON contract. AiAssistantService (business logic) never
// sees any of that; it just calls chat(clientId, message).
@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${ai-service.base-url}") String baseUrl,
            @Value("${ai-service.internal-api-key}") String internalApiKey,
            @Value("${ai-service.timeout-seconds:30}") int timeoutSeconds
    ) {
        // A synchronous RAG + Claude call can genuinely take several
        // seconds — the read timeout is deliberately generous, not the
        // default (which would fail fast on exactly the calls that are
        // supposed to take a while).
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(timeoutSeconds * 1000);

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }

    public AiServiceChatResponse chat(Long clientId, String message) {
        try {
            AiServiceChatResponse response = restClient.post()
                    .uri("/ai/chat")
                    .body(new AiServiceChatRequest(clientId, message, null))
                    .retrieve()
                    .body(AiServiceChatResponse.class);

            if (response == null) {
                throw new AiServiceException("The AI assistant returned an empty response");
            }
            return response;
        } catch (ResourceAccessException e) {
            // Connection refused, DNS failure, or the read timeout above.
            throw new AiServiceException(
                    "The AI assistant is taking too long to respond or is unreachable", e);
        } catch (RestClientResponseException e) {
            // The AI service itself returned a 4xx/5xx (e.g. its own 503
            // when Postgres or Claude is unavailable).
            throw new AiServiceException("The AI assistant returned an error", e);
        }
    }

    public AiServiceSummarizeResponse summarize(Long clientId, String clientName, String industry, String plan) {
        try {
            AiServiceSummarizeResponse response = restClient.post()
                    .uri("/ai/summarize")
                    .body(new AiServiceSummarizeRequest(clientId, clientName, industry, plan))
                    .retrieve()
                    .body(AiServiceSummarizeResponse.class);

            if (response == null) {
                throw new AiServiceException("The AI assistant returned an empty summary response");
            }
            return response;
        } catch (ResourceAccessException e) {
            throw new AiServiceException(
                    "The AI assistant is taking too long to respond or is unreachable", e);
        } catch (RestClientResponseException e) {
            throw new AiServiceException("The AI assistant returned an error", e);
        }
    }
}
