package Jar;
import Jar.service.MatchEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
class MatchEngineTests {
    private final MatchEngine engine = new MatchEngine();
    @Test void findsExactlyOneMissingSkill() {
        var r = engine.calculate("Java, Python, SQL", "", 2.0, "Java, Spring Boot, SQL", "", 2);
        assertEquals(73, r.matchScore());
        assertEquals(List.of("Java", "SQL"), r.matchedSkills());
        assertEquals(List.of("Spring Boot"), r.missingSkills());
        assertEquals("NEEDS 1 SKILL", r.jobDistanceIndicator());
    }
    @Test void avoidsSubstringAndLanguageFalsePositives() {
        assertFalse(engine.contains("JavaScript", "Java"));
        assertFalse(engine.contains("C++, C#", "C"));
        assertTrue(engine.contains("C, SQL", "C"));
        assertTrue(engine.contains("SpringBoot", "Spring Boot"));
    }
    @Test void unknownExperienceDoesNotQualify() {
        var r = engine.calculate("Java", "", null, "Java", "", 2);
        assertEquals(80, r.matchScore());
        assertEquals("UNKNOWN", r.experienceComparison().status());
        assertEquals("UNDERQUALIFIED", r.jobDistanceIndicator());
    }
    @Test void boundsScoresAndDeduplicatesSkills() {
        assertEquals(100, engine.calculate("Java", "", 10.0, "Java, java", "", 2).matchScore());
        assertEquals(0, engine.calculate("Python", "", 0.0, "Java", "", 2).matchScore());
        assertThrows(IllegalArgumentException.class, () -> engine.calculate("Java", "", 2.0, "", "", 0));
        assertThrows(IllegalArgumentException.class, () -> engine.calculate("Java", "", -1.0, "Java", "", 0));
    }
}
