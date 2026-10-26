package momento.ifrn.meal.post.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class R2ClientConfig {

    private final R2Properties r2Properties;

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(r2Properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                r2Properties.getAccessKey(),
                                r2Properties.getSecretKey()
                        )
                ))
                .region(Region.of("auto"))
                .build();
    }
}
