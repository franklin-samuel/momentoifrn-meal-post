package momento.ifrn.meal.post.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ContainerStatusResponse(@JsonProperty("status_code") String statusCode) {
}
