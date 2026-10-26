package momento.ifrn.meal.post.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class InstagramClientConfig {

    private final InstagramProperties instagramProperties;

    @Bean
    public RestClient instagramRestClient() {
        return RestClient.builder()
                .baseUrl("https://graph.facebook.com/" + instagramProperties.getApiVersion())
                .build();
    }
}