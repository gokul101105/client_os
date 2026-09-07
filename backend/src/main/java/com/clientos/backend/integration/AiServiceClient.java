package com.clientos.backend.integration;

import com.clientos.backend.dto.AiServiceChatRequest;
import com.clientos.backend.dto.AiServiceChatResponse;
import com.clientos.backend.dto.AiServiceMeetingBriefRequest;
import com.clientos.backend.dto.AiServiceMeetingBriefResponse;
import com.clientos.backend.dto.AiServiceProcessDocumentRequest;
import com.clientos.backend.dto.AiServiceRecommendRequest;
import com.clientos.backend.dto.AiServiceRecommendResponse;
import com.clientos.backend.dto.AiServiceSummarizeRequest;
import com.clientos.backend.dto.AiServiceSummarizeResponse;
import com.clientos.backend.exception.AiServiceException;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
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
        } catch (RestClientException e) {
            // Neither of the above -- e.g. a timeout that happens mid-way
            // through reading/converting the response body (as opposed to
            // the connect-phase timeout ResourceAccessException covers)
            // throws the bare parent type instead. Caught live as an
            // unhandled 500 before this catch existed.
            throw new AiServiceException("The AI assistant returned an error", e);
        }
    }

    // Called right after a document is stored, so it's immediately
    // searchable -- chunking + local embedding of one small document is
    // fast, so this stays synchronous like every other AI service call
    // here rather than introducing a second (async/background) pattern
    // for just this one case.
    public void processDocument(Long clientId, Long documentId, String filePath) {
        try {
            restClient.post()
                    .uri("/ai/process-document")
                    .body(new AiServiceProcessDocumentRequest(clientId, documentId, filePath))
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new AiServiceException(
                    "The AI assistant is taking too long to respond or is unreachable", e);
        } catch (RestClientResponseException e) {
            throw new AiServiceException("Could not process this document for AI features", e);
        } catch (RestClientException e) {
            throw new AiServiceException("Could not process this document for AI features", e);
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
        } catch (RestClientException e) {
            throw new AiServiceException("The AI assistant returned an error", e);
        }
    }

    public AiServiceRecommendResponse recommend(
            Long clientId,
            String clientName,
            String industry,
            String plan,
            Integer healthScore,
            String healthBand,
            List<String> healthBreakdownReasons,
            String currentSituation,
            List<String> majorProblems,
            String sentiment,
            Boolean attentionRequired,
            String attentionReason
    ) {
        try {
            AiServiceRecommendResponse response = restClient.post()
                    .uri("/ai/recommend")
                    .body(new AiServiceRecommendRequest(
                            clientId, clientName, industry, plan,
                            healthScore, healthBand, healthBreakdownReasons,
                            currentSituation, majorProblems, sentiment,
                            attentionRequired, attentionReason
                    ))
                    .retrieve()
                    .body(AiServiceRecommendResponse.class);

            if (response == null) {
                throw new AiServiceException("The AI assistant returned an empty recommendations response");
            }
            return response;
        } catch (ResourceAccessException e) {
            throw new AiServiceException(
                    "The AI assistant is taking too long to respond or is unreachable", e);
        } catch (RestClientResponseException e) {
            throw new AiServiceException("The AI assistant returned an error", e);
        } catch (RestClientException e) {
            throw new AiServiceException("The AI assistant returned an error", e);
        }
    }

    public AiServiceMeetingBriefResponse meetingBrief(
            Long clientId, String clientName, String industry, String plan, Integer openIssuesCount
    ) {
        try {
            AiServiceMeetingBriefResponse response = restClient.post()
                    .uri("/ai/agent/meeting-brief")
                    .body(new AiServiceMeetingBriefRequest(clientId, clientName, industry, plan, openIssuesCount))
                    .retrieve()
                    .body(AiServiceMeetingBriefResponse.class);

            if (response == null) {
                throw new AiServiceException("The AI assistant returned an empty meeting brief response");
            }
            return response;
        } catch (ResourceAccessException e) {
            throw new AiServiceException(
                    "The AI assistant is taking too long to respond or is unreachable", e);
        } catch (RestClientResponseException e) {
            throw new AiServiceException("The AI assistant returned an error", e);
        } catch (RestClientException e) {
            throw new AiServiceException("The AI assistant returned an error", e);
        }
    }
}
