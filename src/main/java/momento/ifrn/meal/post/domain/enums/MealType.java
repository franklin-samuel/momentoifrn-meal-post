package momento.ifrn.meal.post.domain.enums;

import lombok.Getter;

@Getter
public enum MealType {
    SNACK("Lanche"),
    LUNCH("Almoço"),
    DINNER("Janta");

    private final String displayName;

    MealType(final String displayName) {
        this.displayName = displayName;
    }
}
