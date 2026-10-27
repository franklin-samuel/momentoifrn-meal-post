package momento.ifrn.meal.post.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import momento.ifrn.meal.post.core.infrastructure.TokenStore;
import momento.ifrn.meal.post.domain.InstagramToken;
import momento.ifrn.meal.post.security.R2Properties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2TokenStore implements TokenStore {

    private static final String KEY = "instagram/token.properties";

    private final S3Client r2Client;
    private final R2Properties r2Properties;

    @Override
    public boolean isPersistent() {
        return StringUtils.hasText(r2Properties.getTokenBucketName());
    }

    @Override
    public Optional<InstagramToken> load() {
        if (!isPersistent()) {
            return Optional.empty();
        }
        try {
            final String content = r2Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(r2Properties.getTokenBucketName())
                    .key(KEY)
                    .build()).asUtf8String();

            final Properties props = new Properties();
            props.load(new StringReader(content));

            final String token = props.getProperty("accessToken");
            final String seedHash = props.getProperty("seedHash");
            final String expiresAt = props.getProperty("expiresAt");
            if (token == null || seedHash == null) {
                return Optional.empty();
            }
            return Optional.of(new InstagramToken(token, expiresAt == null ? null : Instant.parse(expiresAt), seedHash));
        } catch (final NoSuchKeyException e) {
            return Optional.empty();
        } catch (final Exception e) {
            log.warn("Não foi possível ler o token do R2: {}", e.toString());
            return Optional.empty();
        }
    }

    @Override
    public void save(final InstagramToken token) {
        if (!isPersistent()) {
            return;
        }
        try {
            final Properties props = new Properties();
            props.setProperty("accessToken", token.accessToken());
            props.setProperty("seedHash", token.seedHash());
            if (token.expiresAt() != null) {
                props.setProperty("expiresAt", token.expiresAt().toString());
            }
            final StringWriter out = new StringWriter();
            props.store(out, null);

            r2Client.putObject(PutObjectRequest.builder()
                            .bucket(r2Properties.getTokenBucketName())
                            .key(KEY)
                            .contentType("text/plain")
                            .build(),
                    RequestBody.fromString(out.toString()));
        } catch (final Exception e) {
            log.error("Falha ao salvar o token renovado no R2 (continua só em memória): {}", e.toString());
        }
    }
}
