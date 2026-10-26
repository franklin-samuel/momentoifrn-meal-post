package momento.ifrn.meal.post.security;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "r2")
public class R2Properties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String publicBaseUrl;

    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
}