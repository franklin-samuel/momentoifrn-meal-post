package momento.ifrn.meal.post.web.controller;

import lombok.RequiredArgsConstructor;
import momento.ifrn.meal.post.domain.DinnerMenu;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @ModelAttribute("dinnerMenu")
    public DinnerMenu dinnerMenu() {
        return new DinnerMenu();
    }

    @GetMapping
    public String home() {
        return "index";
    }
}