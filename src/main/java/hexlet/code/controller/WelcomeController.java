package hexlet.code.controller;

import hexlet.code.utils.NamedRoutes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping(NamedRoutes.WELCOME)
    public String welcome() {
        return "Welcome to Spring";
    }
}
