package Jar.controller;

import Jar.entity.Resume;
import Jar.repository.ResumeRepository;
import Jar.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
public class ResumeUploadController {
    private final ResumeParserController parser;
    private final ResumeRepository resumes;
    private final UserRepository users; private final Jar.service.AuthService auth;
    public ResumeUploadController(ResumeParserController parser, ResumeRepository resumes, UserRepository users, Jar.service.AuthService auth) {
        this.parser = parser; this.resumes = resumes; this.users = users; this.auth=auth;
    }
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public Resume upload(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) throws IOException {
        auth.writeCandidate(userId);
        var user = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        if (!"CANDIDATE".equalsIgnoreCase(user.getRole()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resumes must belong to a candidate");
        var parsed = parser.parseResume(file);
        var resume = new Resume();
        resume.setUser(user);
        resume.setFileName(parsed.get("fileName"));
        resume.setExtractedText(parsed.get("extractedText"));
        resume.setEducation(parsed.get("education"));
        resume.setExperience(parsed.get("experience"));
        resume.setSkills(parsed.get("skills"));
        return resumes.save(resume);
    }
}

