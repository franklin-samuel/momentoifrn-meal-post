package momento.ifrn.meal.post.core.infrastructure;

import momento.ifrn.meal.post.domain.DinnerMenu;

public interface ImageGeneratorService {
    byte[] generate(DinnerMenu menu);
}
