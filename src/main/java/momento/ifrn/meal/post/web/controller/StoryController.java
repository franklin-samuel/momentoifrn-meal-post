package momento.ifrn.meal.post.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import momento.ifrn.meal.post.business.MealService;
import momento.ifrn.meal.post.core.Context;
import momento.ifrn.meal.post.domain.DinnerMenu;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/story")
@RequiredArgsConstructor
public class StoryController {

    private final MealService mealService;

    @PostMapping
    public String post(@ModelAttribute final DinnerMenu dinnerMenu, final Model model) {

        if (!StringUtils.hasText(dinnerMenu.getText())) {
            model.addAttribute("erro", "Digite o texto do cardápio antes de publicar.");
            model.addAttribute("dinnerMenu", dinnerMenu);
            return "index";
        }

        if (dinnerMenu.getMealType() == null) {
            model.addAttribute("erro", "Selecione o tipo de refeição.");
            model.addAttribute("dinnerMenu", dinnerMenu);
            return "index";
        }

        try {
            final Context context = new Context(dinnerMenu);
            mealService.execute(context);
            model.addAttribute("mensagem", "Postado com sucesso no Instagram!");
            model.addAttribute("dinnerMenu", new DinnerMenu());
        } catch (final Exception e) {
            log.error("Erro ao postar cardápio no Instagram", e);
            model.addAttribute("erro", "Erro ao postar: " + e.getMessage());
            model.addAttribute("dinnerMenu", dinnerMenu);
        }

        return "index";
    }
}
