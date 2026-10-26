package momento.ifrn.meal.post.web.controller;

import lombok.RequiredArgsConstructor;
import momento.ifrn.meal.post.business.MealService;
import momento.ifrn.meal.post.core.Context;
import momento.ifrn.meal.post.domain.DinnerMenu;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("/story")
@RequiredArgsConstructor
public class StoryController {

    private final MealService mealService;

    @PostMapping
    public String post(@ModelAttribute final DinnerMenu dinnerMenu, final Model model) {

        try {
            final Context context = new Context(dinnerMenu);
            mealService.execute(context);
            model.addAttribute("mensagem", "Postado com sucesso no Instagram!");
        } catch (final Exception e) {
            model.addAttribute("erro", "Erro ao postar: " + e.getMessage());
        }

        model.addAttribute("dinnerMenu", new DinnerMenu());
        return "index";
    }

}
