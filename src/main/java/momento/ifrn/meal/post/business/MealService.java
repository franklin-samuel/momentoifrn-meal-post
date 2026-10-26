package momento.ifrn.meal.post.business;

import lombok.RequiredArgsConstructor;
import momento.ifrn.meal.post.core.Context;
import momento.ifrn.meal.post.core.infrastructure.ImageGeneratorService;
import momento.ifrn.meal.post.core.infrastructure.InstagramPublisherService;
import momento.ifrn.meal.post.core.infrastructure.StorageService;
import momento.ifrn.meal.post.domain.DinnerMenu;
import momento.ifrn.meal.post.domain.StoryPost;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealService {

    private final ImageGeneratorService imageGeneratorService;
    private final StorageService storageService;
    private final InstagramPublisherService instagramPublisherService;

    public void execute(final Context context) {

        final DinnerMenu menu = context.getData(DinnerMenu.class);

        final byte[] image = imageGeneratorService.generate(menu);
        final String publicUrl = storageService.upload(image, "story.png", "image/png");

        final StoryPost post = StoryPost.builder()
                .imageUrl(publicUrl)
                .build();

        instagramPublisherService.publish(post);

    }

}
