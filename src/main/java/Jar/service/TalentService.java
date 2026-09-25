package Jar.service;

import Jar.entity.*;
import Jar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class TalentService {
    private final UserRepository users;
    private final ResumeRepository resumes;
    private final SkillProfileRepository skills;
    private final JobPostingRepository jobs;
    private final MatchEngine engine; private final AuthService auth;
    public TalentService(UserRepository users, ResumeRepository resumes, SkillProfileRepository skills, JobPostingRepository jobs, MatchEngine engine, AuthService auth) {
        this.users=users; this.resumes=resumes; this.skills=skills; this.jobs=jobs; this.engine=engine;this.auth=auth;
    }
    public record CandidateData(User user, Resume resume, List<SkillProfile> skills, String skillText, Double yearsOfExperience) {}
    public record Recommendation(JobPosting job, MatchEngine.Result match) {}
    public CandidateData candidate(Long id) { auth.readCandidate(id);
        User user=users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Candidate not found"));
        if (!"CANDIDATE".equalsIgnoreCase(user.getRole())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User is not a candidate");
        Resume resume=resumes.findAll().stream().filter(r -> r.getUser()!=null && id.equals(r.getUser().getUserId()))
            .max(Comparator.comparing(Resume::getUploadedAt, Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(Resume::getResumeId)).orElse(null);
        var profile=skills.findAll().stream().filter(s -> s.getUser()!=null && id.equals(s.getUser().getUserId())).toList();
        String text=(resume==null?"":Objects.toString(resume.getSkills(),""))+"\n"+String.join(", ",profile.stream().map(SkillProfile::getSkillName).filter(Objects::nonNull).toList());
        Double years=resume==null?null:engine.explicitYears(resume.getExperience());
        var profileYears=profile.stream().map(SkillProfile::getYearsOfExperience).filter(Objects::nonNull).max(Double::compare);
        if(profileYears.isPresent()) years=years==null?profileYears.get():Math.max(years,profileYears.get());
        return new CandidateData(user,resume,profile,text,years);
    }
    public JobPosting job(Long id) {return jobs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found"));}
    public MatchEngine.Result match(CandidateData c, JobPosting job) {
        Double required=engine.explicitYears(job.getExperienceRequired());
        return engine.calculate(c.skillText(),c.resume()==null?"":c.resume().getExtractedText(),c.yearsOfExperience(),job.getRequiredSkills(),job.getDescription(),required==null?0:required);
    }
    public List<Recommendation> recommendations(Long userId) {
        var c=candidate(userId);
        if(c.resume()==null && c.skills().isEmpty()) return List.of();
        return jobs.findAll().stream().filter(j -> j.getRequiredSkills()!=null && !j.getRequiredSkills().isBlank())
            .map(j -> new Recommendation(j,match(c,j))).sorted(Comparator.comparingInt((Recommendation r)->r.match().matchScore()).reversed()).toList();
    }
}

