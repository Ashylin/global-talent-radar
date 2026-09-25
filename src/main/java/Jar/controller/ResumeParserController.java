package Jar.controller;
import Jar.service.ResumeParserService;
import Jar.service.ResumeSections;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
@RestController
@RequestMapping("/api/resumes")
public class ResumeParserController {
    private final ResumeParserService parser;
    private final ResumeSections sections;
    public ResumeParserController(ResumeParserService parser, ResumeSections sections) {
        this.parser = parser; this.sections = sections;
    }
    @PostMapping("/parse")
    public Map<String, String> parseResume(@RequestParam("file") MultipartFile file) throws IOException {
        String text = parser.extractText(file);
        Map<String, String> result = sections.extract(text);
        result.put("fileName", Objects.toString(file.getOriginalFilename(), "resume"));
        result.put("extractedText", text);
        return result;
    }
}
