package Jar.controller;
import Jar.entity.SkillProfile;
import Jar.repository.*;
import Jar.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@RestController
@RequestMapping("/api/skills")
public class SkillProfileController {
 private final SkillProfileRepository skills;private final UserRepository users;private final AuthService auth;
 public SkillProfileController(SkillProfileRepository skills,UserRepository users,AuthService auth){this.skills=skills;this.users=users;this.auth=auth;}
 @GetMapping public List<SkillProfile> getSkills(){var u=auth.current();return skills.findAll().stream().filter(s -> "RECRUITER".equals(u.getRole()) || (s.getUser()!=null && u.getUserId().equals(s.getUser().getUserId()))).toList();}
 @PostMapping public SkillProfile createSkill(@RequestBody SkillProfile s){Long id=s.getUser()==null?null:s.getUser().getUserId();auth.writeCandidate(id);
  if(s.getSkillName()==null || s.getSkillName().isBlank() || s.getYearsOfExperience()==null || !Double.isFinite(s.getYearsOfExperience()) || s.getYearsOfExperience()<0 || !Set.of("BEGINNER","INTERMEDIATE","ADVANCED").contains(Objects.toString(s.getProficiencyLevel(),"")))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Provide skill, level and non-negative years of experience");
  var existing=skills.findAll().stream().filter(v -> v.getUser()!=null && id.equals(v.getUser().getUserId()) && s.getSkillName().trim().equalsIgnoreCase(v.getSkillName())).findFirst();
  s.setSkillProfileId(existing.map(SkillProfile::getSkillProfileId).orElse(null));s.setSkillName(s.getSkillName().trim());s.setUser(users.findById(id).orElseThrow());s.setLastUpdated(java.time.LocalDateTime.now());return skills.save(s);
 }
}
