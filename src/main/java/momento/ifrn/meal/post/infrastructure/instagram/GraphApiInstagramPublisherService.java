package momento.ifrn.meal.post.infrastructure.instagram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import momento.ifrn.meal.post.core.infrastructure.InstagramPublisherService;
import momento.ifrn.meal.post.domain.StoryPost;
import momento.ifrn.meal.post.domain.exceptions.InstagramPublishException;
import momento.ifrn.meal.post.security.InstagramProperties;
import momento.ifrn.meal.post.web.model.response.ContainerStatusResponse;
import momento.ifrn.meal.post.web.model.response.MediaContainerResponse;
import momento.ifrn.meal.post.web.model.response.MediaPublishResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphApiInstagramPublisherService implements InstagramPublisherService {

    private static final int MAX_POLL_ATTEMPTS = 10;
    private static final long POLL_INTERVAL_MS = 2000L;

    private final RestClient instagramRestClient;
    private final InstagramProperties instagramProperties;

    @Override
    public void publish(final StoryPost post) {
        final String creationId = createMediaContainer(post);
        waitUntilReady(creationId);
        publishContainer(creationId);
    }

    private String createMediaContainer(final StoryPost post) {
        final MediaContainerResponse response = instagramRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/{igUserId}/media")
                        .queryParam("image_url", post.getImageUrl())
                        .queryParam("media_type", post.getMediaType())
                        .queryParam("access_token", instagramProperties.getAccessToken())
                        .build(instagramProperties.getUserId()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(MediaContainerResponse.class);

        if (response == null || response.id() == null) {
            throw new InstagramPublishException("Resposta inválida ao criar container de mídia");
        }

        log.info("Container de mídia criado: {}", response.id());
        return response.id();
    }

    private void waitUntilReady(final String creationId) {
        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            final String status = fetchStatus(creationId);

            if ("FINISHED".equals(status)) {
                return;
            }
            if ("ERROR".equals(status) || "EXPIRED".equals(status)) {
                throw new InstagramPublishException("Processamento do container falhou com status: " + status);
            }

            log.debug("Container {} ainda em processamento (status={}), tentativa {}/{}",
                    creationId, status, attempt, MAX_POLL_ATTEMPTS);
            sleep(POLL_INTERVAL_MS);
        }

        throw new InstagramPublishException("Timeout aguardando processamento do container " + creationId);
    }

    private String fetchStatus(final String creationId) {
        final ContainerStatusResponse response = instagramRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{creationId}")
                        .queryParam("fields", "status_code")
                        .queryParam("access_token", instagramProperties.getAccessToken())
                        .build(creationId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(ContainerStatusResponse.class);

        return response != null ? response.statusCode() : null;
    }

    private void publishContainer(final String creationId) {
        final MediaPublishResponse response = instagramRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/{igUserId}/media_publish")
                        .queryParam("creation_id", creationId)
                        .queryParam("access_token", instagramProperties.getAccessToken())
                        .build(instagramProperties.getUserId()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(MediaPublishResponse.class);

        if (response == null || response.id() == null) {
            throw new InstagramPublishException("Resposta inválida ao publicar o Story");
        }

        log.info("Story publicado com sucesso. Media ID: {}", response.id());
    }

    private void handleError(final HttpRequest request, final ClientHttpResponse response) throws IOException {
        final String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        log.error("Erro na Instagram Graph API [{}]: {}", response.getStatusCode(), body);
        throw new InstagramPublishException("Erro na Instagram Graph API: " + body);
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InstagramPublishException("Interrompido ao aguardar processamento do container", e);
        }
    }
}