package momento.ifrn.meal.post.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import momento.ifrn.meal.post.domain.enums.MealType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DinnerMenu {
    private String text;
    private MealType mealType;
}
