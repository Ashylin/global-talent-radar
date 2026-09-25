package Jar.service;

import Jar.entity.User;
import Jar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(10);
    private record Session(Long userId,Instant expires){}
    public record LoginResult(String token,User user){}
    private final Map<String,Session> sessions=new ConcurrentHashMap<>();
    public AuthService(UserRepository users){this.users=users;}
    @PostConstruct void migratePasswords(){
        for(var user:users.findAll()) if(user.getPassword()!=null && !user.getPassword().matches("^\\$2[aby]\\$\\d{2}\\$.*")){
            user.setPassword(encoder.encode(user.getPassword()));users.save(user);
        }
    }
    public User register(User user){
        if(user.getFullName()==null || user.getFullName().isBlank())bad("Name is required");
        if(user.getEmail()==null || !user.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))bad("A valid email is required");
        if(user.getPassword()==null || user.getPassword().length()<8 || user.getPassword().getBytes(StandardCharsets.UTF_8).length>72)bad("Password must be at least 8 characters and at most 72 UTF-8 bytes");
        String email=user.getEmail().trim().toLowerCase(Locale.ROOT);
        if(users.findAll().stream().anyMatch(u -> email.equalsIgnoreCase(u.getEmail())))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
        if(!Set.of("CANDIDATE","RECRUITER").contains(Objects.toString(user.getRole(),"")))bad("Choose CANDIDATE or RECRUITER");
        user.setUserId(null);user.setFullName(user.getFullName().trim());user.setEmail(email);user.setPassword(encoder.encode(user.getPassword()));user.setCreatedAt(java.time.LocalDateTime.now());
        return users.save(user);
    }
    public LoginResult login(String email,String password){
        var user=users.findAll().stream().filter(u -> u.getEmail().equalsIgnoreCase(Objects.toString(email,"").trim())).findFirst().orElse(null);
        if(user==null || password==null || password.getBytes(StandardCharsets.UTF_8).length>72 || !encoder.matches(password,user.getPassword()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
        sessions.entrySet().removeIf(e -> e.getValue().expires().isBefore(Instant.now()));
        String token=UUID.randomUUID()+"."+UUID.randomUUID();sessions.put(token,new Session(user.getUserId(),Instant.now().plusSeconds(28800)));
        return new LoginResult(token,user);
    }
    public User current(){
        var attrs=(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        String value=attrs==null?null:attrs.getRequest().getHeader("Authorization");
        var session=value!=null && value.startsWith("Bearer ")?sessions.get(value.substring(7)):null;
        if(session==null || session.expires().isBefore(Instant.now()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in");
        return users.findById(session.userId()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in"));
    }
    public void logout(String value){if(value!=null && value.startsWith("Bearer "))sessions.remove(value.substring(7));}
    public boolean recruiter(){return "RECRUITER".equals(current().getRole());}
    public void requireRecruiter(){if(!recruiter())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Recruiter access required");}
    public void readCandidate(Long id){var u=current();if(!u.getUserId().equals(id) && !"RECRUITER".equals(u.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only view your own candidate profile");}
    public void writeCandidate(Long id){var u=current();if(id==null || !u.getUserId().equals(id) || !"CANDIDATE".equals(u.getRole()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only update your own candidate profile");}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
}
