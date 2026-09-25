package Jar.controller;

import Jar.service.*;
import Jar.repository.*;
import Jar.entity.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {
    private final TalentService talent; private final UserRepository users; private final PerformanceTrackingRepository performance; private final MatchEngine engine; private final AuthService auth;
    public CandidateController(TalentService talent, UserRepository users, PerformanceTrackingRepository performance, MatchEngine engine, AuthService auth){this.talent=talent;this.users=users;this.performance=performance;this.engine=engine;this.auth=auth;}
    @GetMapping("/{id}/profile") public TalentService.CandidateData profile(@PathVariable Long id){return talent.candidate(id);}
    @GetMapping("/{id}/recommendations") public List<TalentService.Recommendation> recommendations(@PathVariable Long id){return talent.recommendations(id);}
    @GetMapping("/{id}/history") public List<PerformanceTracking> history(@PathVariable Long id){talent.candidate(id);return performance.findAll().stream().filter(p -> p.getUser()!=null && id.equals(p.getUser().getUserId())).sorted(Comparator.comparing(PerformanceTracking::getRecordedAt)).toList();}
    public record SearchResult(TalentService.CandidateData candidate, MatchEngine.Result match){}
    @GetMapping public List<SearchResult> search(@RequestParam(required=false) Long jobId,
        @RequestParam(defaultValue="") String query,@RequestParam(defaultValue="") String country,@RequestParam(defaultValue="") String region,
        @RequestParam(defaultValue="") String skills,@RequestParam(required=false) Double minExperience,
        @RequestParam(defaultValue="0") int minScore,@RequestParam(defaultValue="") String readiness){
        auth.requireRecruiter(); JobPosting job=jobId==null?null:talent.job(jobId);
        var results=new ArrayList<SearchResult>();
        for(var u:users.findAll()){
            if(!"CANDIDATE".equalsIgnoreCase(u.getRole())) continue;
            var c=talent.candidate(u.getUserId());
            if(!includes(u.getFullName()+" "+c.skillText(),query) || !includes(u.getCountry(),country) || !includes(u.getRegion(),region))continue;
            if(minExperience!=null && (c.yearsOfExperience()==null || c.yearsOfExperience()<minExperience))continue;
            if(engine.splitSkills(skills).stream().anyMatch(s -> !engine.contains(c.skillText(),s)))continue;
            var match=job==null?null:talent.match(c,job);
            if(minScore>0 && (match==null || match.matchScore()<minScore))continue;
            if(!readiness.isBlank() && (match==null || !readiness.equals(match.jobDistanceIndicator())))continue;
            results.add(new SearchResult(c,match));
        }
        results.sort(Comparator.comparingInt((SearchResult r)->r.match()==null?0:r.match().matchScore()).reversed());
        return results;
    }
    private boolean includes(String value,String filter){return Objects.toString(value,"").toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));}
}

