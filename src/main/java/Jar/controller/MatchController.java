package Jar.controller;
import Jar.service.*;
import Jar.repository.ResumeRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    public record Request(Long userId, Long resumeId, Long jobId, String candidateSkills, String resumeText, Double candidateExperienceYears, String requiredSkills, String jobDescription, Double requiredExperienceYears) {}
    private final MatchEngine engine; private final TalentService talent; private final ResumeRepository resumes; private final AuthService auth;
    public MatchController(MatchEngine engine,TalentService talent,ResumeRepository resumes,AuthService auth){this.engine=engine;this.talent=talent;this.resumes=resumes;this.auth=auth;}
    @PostMapping("/calculate")
    public MatchEngine.Result calculate(@RequestBody Request r) {
        String skills=r.candidateSkills(),text=r.resumeText(),required=r.requiredSkills(),description=r.jobDescription();
        Double years=r.candidateExperienceYears(),requiredYears=r.requiredExperienceYears();
        if(r.userId()!=null){var c=talent.candidate(r.userId());skills=c.skillText();text=c.resume()==null?"":c.resume().getExtractedText();years=c.yearsOfExperience();}
        if(r.resumeId()!=null){var resume=resumes.findById(r.resumeId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume not found"));
            auth.readCandidate(resume.getUser().getUserId());
            if(r.userId()!=null && (resume.getUser()==null || !r.userId().equals(resume.getUser().getUserId()))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Resume does not belong to candidate");
            skills=resume.getSkills();text=resume.getExtractedText();years=engine.explicitYears(resume.getExperience());}
        if(r.jobId()!=null){var job=talent.job(r.jobId());required=job.getRequiredSkills();description=job.getDescription();requiredYears=engine.explicitYears(job.getExperienceRequired());}
        if(skills==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supply userId, resumeId or candidateSkills");
        try{return engine.calculate(skills,text,years,required,description,requiredYears==null?0:requiredYears);}
        catch(IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());}
    }
}

