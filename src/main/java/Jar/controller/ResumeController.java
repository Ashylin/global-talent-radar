package Jar.controller;
import Jar.entity.Resume;
import Jar.repository.*;
import Jar.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
 private final ResumeRepository resumes;private final UserRepository users;private final AuthService auth;
 public ResumeController(ResumeRepository resumes,UserRepository users,AuthService auth){this.resumes=resumes;this.users=users;this.auth=auth;}
 @GetMapping public List<Resume> getResumes(){var u=auth.current();return resumes.findAll().stream().filter(r -> "RECRUITER".equals(u.getRole()) || (r.getUser()!=null && u.getUserId().equals(r.getUser().getUserId()))).toList();}
 @PostMapping public Resume createResume(@RequestBody Resume r){
  Long id=r.getUser()==null?null:r.getUser().getUserId();auth.writeCandidate(id);
  if(r.getExtractedText()==null || r.getExtractedText().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Parsed resume text is required");
  r.setResumeId(null);r.setFilePath(null);r.setUser(users.findById(id).orElseThrow());r.setUploadedAt(java.time.LocalDateTime.now());return resumes.save(r);
 }
}
