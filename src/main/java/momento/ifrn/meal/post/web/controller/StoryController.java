package momento.ifrn.meal.post.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import momento.ifrn.meal.post.business.MealService;
import momento.ifrn.meal.post.core.Context;
import momento.ifrn.meal.post.domain.DinnerMenu;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/story")
@RequiredArgsConstructor
public class StoryController {

    private final MealService mealService;

    @PostMapping
    public String post(@ModelAttribute final DinnerMenu dinnerMenu, final RedirectAttributes redirectAttributes) {

        if (!StringUtils.hasText(dinnerMenu.getText())) {
            redirectAttributes.addFlashAttribute("erro", "Digite o texto do cardápio antes de publicar.");
            redirectAttributes.addFlashAttribute("dinnerMenu", dinnerMenu);
            return "redirect:/";
        }

        if (dinnerMenu.getMealType() == null) {
            redirectAttributes.addFlashAttribute("erro", "Selecione o tipo de refeição");
            redirectAttributes.addFlashAttribute("dinnerMenu", dinnerMenu);
            return "redirect:/";
        }

        try {
            final Context context = new Context(dinnerMenu);
            mealService.execute(context);
            redirectAttributes.addFlashAttribute("mensagem", "Postado com sucesso no Instagram!");
        } catch (final Exception e) {
            log.error("Erro ao postar cardápio no Instagram", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao postar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("dinnerMenu", dinnerMenu);
        }

        return "redirect:/";
    }
}