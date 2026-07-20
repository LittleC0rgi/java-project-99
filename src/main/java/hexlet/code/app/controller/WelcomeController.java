package hexlet.code.app.controller;

import hexlet.code.app.utils.NamedRoutes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping(NamedRoutes.WELCOME)
    public String welcome() {
        return "Welcome to Spring";
    }
}
