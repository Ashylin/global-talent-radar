package Jar.controller;
import Jar.entity.User;
import Jar.service.AuthService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
 private final AuthService auth;
 public AuthController(AuthService auth){this.auth=auth;}
 public record Credentials(String email,String password){}
 @PostMapping("/register") public AuthService.LoginResult register(@RequestBody User user){String password=user.getPassword();var saved=auth.register(user);return auth.login(saved.getEmail(),password);}
 @PostMapping("/login") public AuthService.LoginResult login(@RequestBody Credentials c){return auth.login(c.email(),c.password());}
 @GetMapping("/me") public User me(){return auth.current();}
 @PostMapping("/logout") public void logout(@RequestHeader(value="Authorization",required=false) String value){auth.logout(value);}
}
