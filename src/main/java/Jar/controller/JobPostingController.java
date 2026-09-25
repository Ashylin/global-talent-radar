package Jar.controller;
import Jar.entity.JobPosting;
import Jar.repository.*;
import Jar.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController
@RequestMapping("/api/jobs")
public class JobPostingController {
 private final JobPostingRepository jobs;private final CompanyRepository companies;private final AuthService auth;
 public JobPostingController(JobPostingRepository jobs,CompanyRepository companies,AuthService auth){this.jobs=jobs;this.companies=companies;this.auth=auth;}
 @GetMapping public List<JobPosting> getJobs(){return jobs.findAll();}
 @GetMapping("/{id}") public JobPosting getJob(@PathVariable Long id){return jobs.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found"));}
 @PostMapping public JobPosting createJob(@RequestBody JobPosting j){auth.requireRecruiter();
  if(j.getTitle()==null || j.getTitle().isBlank() || j.getDescription()==null || j.getDescription().isBlank() || j.getRequiredSkills()==null || j.getRequiredSkills().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Title, description and required skills are required");
  if(j.getCompany()==null || j.getCompany().getCompanyId()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select a company");
  j.setCompany(companies.findById(j.getCompany().getCompanyId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Company not found")));j.setJobId(null);return jobs.save(j);
 }
}
