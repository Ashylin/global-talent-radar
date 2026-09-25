package Jar.service;

import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.*;

@Service
public class ResumeSections {
    private static final Map<String, String> HEADINGS = Map.ofEntries(
        Map.entry("EDUCATION", "education"), Map.entry("ACADEMICQUALIFICATIONS", "education"),
        Map.entry("EXPERIENCE", "experience"), Map.entry("WORKEXPERIENCE", "experience"),
        Map.entry("PROFESSIONALEXPERIENCE", "experience"), Map.entry("INTERNSHIPEXPERIENCE", "experience"),
        Map.entry("SKILLS", "skills"), Map.entry("TECHNICALSKILLS", "skills"), Map.entry("TECHNOLOGIES", "skills"),
        Map.entry("PROJECTS", "other"), Map.entry("CERTIFICATIONS", "other"),
        Map.entry("LANGUAGESKNOWN", "other"), Map.entry("LANGUAGES", "other"),
        Map.entry("HACKATHONSEVENTS", "other"), Map.entry("ACHIEVEMENTS", "other"),
        Map.entry("SUMMARY", "other"), Map.entry("OBJECTIVE", "other"), Map.entry("INTERESTS", "other")
    );
    public Map<String, String> extract(String text) {
        Map<String, StringBuilder> sections = new HashMap<>();
        for (String key : List.of("education", "experience", "skills")) sections.put(key, new StringBuilder());
        String current = "other";
        for (String line : text.split("\\R")) {
            String normalized = Normalizer.normalize(line, Normalizer.Form.NFKC)
                .toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
            String heading = HEADINGS.get(normalized);
            if (heading != null) { current = heading; continue; }
            if (sections.containsKey(current)) sections.get(current).append(line).append('\n');
        }
        Map<String, String> result = new HashMap<>();
        sections.forEach((key, value) -> result.put(key, value.toString().trim()));
        return result;
    }
}
