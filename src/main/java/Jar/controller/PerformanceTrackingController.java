package Jar.controller;
import Jar.entity.PerformanceTracking;
import Jar.repository.*;
import Jar.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController
@RequestMapping("/api/performance")
public class PerformanceTrackingController {
 private final PerformanceTrackingRepository performance;private final UserRepository users;private final AuthService auth;
 public PerformanceTrackingController(PerformanceTrackingRepository performance,UserRepository users,AuthService auth){this.performance=performance;this.users=users;this.auth=auth;}
 @GetMapping public List<PerformanceTracking> getPerformance(){var u=auth.current();return performance.findAll().stream().filter(p -> "RECRUITER".equals(u.getRole()) || (p.getUser()!=null && u.getUserId().equals(p.getUser().getUserId()))).toList();}
 @PostMapping public PerformanceTracking createPerformance(@RequestBody PerformanceTracking p){Long id=p.getUser()==null?null:p.getUser().getUserId();auth.writeCandidate(id);
  if(p.getMetricName()==null || p.getMetricName().isBlank() || p.getMetricValue()==null || !Double.isFinite(p.getMetricValue()) || p.getMetricValue()<0 || p.getMetricValue()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Provide a skill name and score between 0 and 100");
  p.setPerformanceId(null);p.setUser(users.findById(id).orElseThrow());p.setRecordedAt(java.time.LocalDateTime.now());return performance.save(p);
 }
}
