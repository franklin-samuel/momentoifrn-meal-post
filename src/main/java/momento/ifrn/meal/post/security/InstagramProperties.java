package momento.ifrn.meal.post.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "instagram")
public class InstagramProperties {
    private String accessToken;
    private String userId;
    private String apiVersion;
}
