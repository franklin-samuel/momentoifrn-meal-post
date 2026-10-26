package momento.ifrn.meal.post.core.infrastructure;

import momento.ifrn.meal.post.domain.StoryPost;

public interface InstagramPublisherService {
    void publish(StoryPost post);
}