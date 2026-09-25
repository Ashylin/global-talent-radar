package Jar.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class WebController {
    @GetMapping({"/app", "/app/"})
    public String app() { return "forward:/index.html"; }
}
