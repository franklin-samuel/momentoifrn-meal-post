package momento.ifrn.meal.post.domain;

import java.time.Instant;

public record InstagramToken(
        String accessToken,
        Instant expiresAt,
        String seedHash
) {
}
