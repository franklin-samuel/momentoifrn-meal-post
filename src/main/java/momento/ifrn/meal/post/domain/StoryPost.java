package momento.ifrn.meal.post.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryPost {
    private String imageUrl;

    @Builder.Default
    private String mediaType = "STORIES";
}
