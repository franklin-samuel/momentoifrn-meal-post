package momento.ifrn.meal.post.infrastructure.instagram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import momento.ifrn.meal.post.core.infrastructure.TokenStore;
import momento.ifrn.meal.post.domain.InstagramToken;
import momento.ifrn.meal.post.domain.exceptions.InstagramPublishException;
import momento.ifrn.meal.post.security.InstagramProperties;
import momento.ifrn.meal.post.web.model.response.TokenRefreshResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramTokenManager {

    private static final Duration MIN_INTERVAL_BETWEEN_ATTEMPTS = Duration.ofHours(1);
    private static final Duration FALLBACK_LIFETIME = Duration.ofDays(60);

    private final RestClient instagramRestClient;
    private final InstagramProperties properties;
    private final TokenStore tokenStore;

    private InstagramToken current;
    private boolean refreshEnabled;
    private Instant lastAttempt = Instant.MIN;

    public synchronized String getAccessToken() {
        ensureLoaded();
        refreshIfNeeded();
        return current.accessToken();
    }

    @Scheduled(cron = "0 0 4 * * *")
    public synchronized void scheduledCheck() {
        ensureLoaded();
        refreshIfNeeded();
    }

    private void ensureLoaded() {
        if (current != null) {
            return;
        }
        final String seedHash = sha256(properties.getAccessToken());
        current = tokenStore.load()
                .filter(stored -> seedHash.equals(stored.seedHash()))
                .orElseGet(() -> new InstagramToken(properties.getAccessToken(), null, seedHash));

        refreshEnabled = properties.getLoginType() == InstagramProperties.LoginType.INSTAGRAM
                || (StringUtils.hasText(properties.getAppId()) && StringUtils.hasText(properties.getAppSecret()));

        if (!refreshEnabled) {
            log.warn("Renovação do token desativada: defina IG_APP_ID e IG_APP_SECRET (login FACEBOOK).");
        }
        if (!tokenStore.isPersistent()) {
            log.warn("R2_TOKEN_BUCKET_NAME não definido: o token renovado ficará só em memória e se perde no restart.");
        }
    }

    private void refreshIfNeeded() {
        if (!refreshEnabled) {
            return;
        }
        final Instant now = Instant.now();
        final boolean due = current.expiresAt() == null
                || now.isAfter(current.expiresAt().minus(Duration.ofDays(properties.getTokenRefreshThresholdDays())));
        if (!due || now.isBefore(lastAttempt.plus(MIN_INTERVAL_BETWEEN_ATTEMPTS))) {
            return;
        }

        lastAttempt = now;
        try {
            final TokenRefreshResponse response = requestRefresh(current.accessToken());
            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new InstagramPublishException("Resposta inválida ao renovar o token");
            }
            final Duration lifetime = response.expiresIn() != null
                    ? Duration.ofSeconds(response.expiresIn())
                    : FALLBACK_LIFETIME;

            current = new InstagramToken(response.accessToken(), now.plus(lifetime), current.seedHash());
            tokenStore.save(current);
            log.info("Token do Instagram renovado; expira em {}", current.expiresAt());
        } catch (final Exception e) {
            log.error("Falha ao renovar o token do Instagram (nova tentativa em até 1h): {}", e.getMessage());
        }
    }

    private TokenRefreshResponse requestRefresh(final String token) {
        final UriComponentsBuilder uri = switch (properties.getLoginType()) {
            case INSTAGRAM -> UriComponentsBuilder
                    .fromUriString("https://graph.instagram.com/refresh_access_token")
                    .queryParam("grant_type", "ig_refresh_token")
                    .queryParam("access_token", token);
            case FACEBOOK -> UriComponentsBuilder
                    .fromUriString("https://graph.facebook.com/" + properties.getApiVersion() + "/oauth/access_token")
                    .queryParam("grant_type", "fb_exchange_token")
                    .queryParam("client_id", properties.getAppId())
                    .queryParam("client_secret", properties.getAppSecret())
                    .queryParam("fb_exchange_token", token);
        };

        return instagramRestClient.get()
                .uri(uri.build().encode().toUri())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    final String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new InstagramPublishException("Graph API recusou a renovação [" + response.getStatusCode() + "]: " + body);
                })
                .body(TokenRefreshResponse.class);
    }

    private static String sha256(final String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
