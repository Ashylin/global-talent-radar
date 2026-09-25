package Jar.controller;
import Jar.entity.User;
import Jar.repository.UserRepository;
import Jar.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/users")
public class UserController {
 private final UserRepository users;private final AuthService auth;
 public UserController(UserRepository users,AuthService auth){this.users=users;this.auth=auth;}
 @GetMapping public List<User> getUsers(){return auth.recruiter()?users.findAll():List.of(auth.current());}
 @PostMapping public User createUser(@RequestBody User user){return auth.register(user);}
}
