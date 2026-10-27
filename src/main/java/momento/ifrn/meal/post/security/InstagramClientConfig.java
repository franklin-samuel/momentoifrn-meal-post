package momento.ifrn.meal.post.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class InstagramClientConfig {

    private final InstagramProperties instagramProperties;

    @Bean
    public RestClient instagramRestClient() {
        final JacksonJsonHttpMessageConverter jsonConverter = new JacksonJsonHttpMessageConverter();

        jsonConverter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                new MediaType("text", "javascript")
        ));

        return RestClient.builder()
                .baseUrl("https://graph.facebook.com/" + instagramProperties.getApiVersion())
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof JacksonJsonHttpMessageConverter);
                    converters.add(0, jsonConverter);
                })
                .build();
    }
}