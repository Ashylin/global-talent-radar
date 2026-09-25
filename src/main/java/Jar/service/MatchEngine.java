package Jar.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class MatchEngine {
    public record ExperienceComparison(Double candidateYears, double requiredYears, String status) {}
    public record Result(int matchScore, List<String> matchedSkills, List<String> missingSkills,
                         List<String> missingKeywords, ExperienceComparison experienceComparison,
                         String jobDistanceIndicator, String scoringExplanation) {}
    private static final Set<String> STOP = Set.of("the","and","for","with","that","this","from","have","will","your","you","our","are","job","work","team","years","year","required","experience","skills","maintain","develop");
    public List<String> splitSkills(String value) {
        if (value == null) return List.of();
        var unique = new LinkedHashMap<String,String>();
        for (String s : value.split("[,;\\n]")) if (!s.isBlank()) unique.putIfAbsent(normalize(s), s.trim());
        return List.copyOf(unique.values());
    }
    private String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim()
            .replace("springboot", "spring boot").replace("scikit learn", "scikit-learn")
            .replace("postgresql", "postgres").replace("javascript", "javascript");
    }
    public boolean contains(String corpus, String skill) {
        String needle = normalize(skill);
        return !needle.isBlank() && Pattern.compile("(?<![a-z0-9+#])" + Pattern.quote(needle) + "(?![a-z0-9+#])")
            .matcher(normalize(Objects.toString(corpus, ""))).find();
    }
    public Double explicitYears(String value) {
        if (value == null) return null;
        var m = Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*\\+?\\s*(years?|yrs?)").matcher(value);
        double max = -1;
        while (m.find()) max = Math.max(max, Double.parseDouble(m.group(1)));
        return max < 0 ? null : max;
    }
    public Result calculate(String candidateSkills, String resumeText, Double years,
                            String requiredSkills, String description, double requiredYears) {
        var required = splitSkills(requiredSkills);
        if (required.isEmpty()) throw new IllegalArgumentException("At least one required skill is needed");
        if (requiredYears < 0 || !Double.isFinite(requiredYears) || (years != null && (years < 0 || !Double.isFinite(years))))
            throw new IllegalArgumentException("Experience must be a finite non-negative number");
        var matched = new ArrayList<String>(); var missing = new ArrayList<String>();
        for (String skill : required) (contains(candidateSkills, skill) ? matched : missing).add(skill);
        double exp = requiredYears == 0 ? 1 : years == null ? 0 : Math.min(1, years / requiredYears);
        int score = (int)Math.round(80.0 * matched.size() / required.size() + 20 * exp);
        String readiness = exp < 1 || missing.size() > 1 ? "UNDERQUALIFIED" : missing.size() == 1 ? "NEEDS 1 SKILL" : "READY TO APPLY";
        var keywords = new LinkedHashSet<String>();
        String corpus = Objects.toString(resumeText, "") + " " + Objects.toString(candidateSkills, "");
        for (String word : Objects.toString(description, "").toLowerCase(Locale.ROOT).split("[^a-z0-9+#.-]+"))
            if (word.length() > 3 && !STOP.contains(word) && !contains(corpus, word)) keywords.add(word);
        return new Result(score, List.copyOf(matched), List.copyOf(missing), keywords.stream().limit(25).toList(),
            new ExperienceComparison(years, requiredYears, requiredYears == 0 ? "NOT REQUIRED" : years == null ? "UNKNOWN" : years >= requiredYears ? "MEETS REQUIREMENT" : "BELOW REQUIREMENT"),
            readiness, "80% required-skill coverage + 20% experience coverage (capped at 100%). Unknown experience earns no experience points when experience is required. Keywords are informational. Readiness requires the experience requirement to be met.");
    }
}
