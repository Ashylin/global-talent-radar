package Jar;
import Jar.service.ResumeSections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ResumeSectionsTests {
    private final ResumeSections sections = new ResumeSections();
    @Test void recognizesLetterSpacedHeadings() {
        var r = sections.extract("E D U C A T I O N\nB.Tech\nI N T E R N S H I P E X P E R I E N C E\nIntern\nPROJECTS\nApp\nT E C H N I C A L S K I L L S\nPython, C\nCERTIFICATIONS\nAWS");
        assertEquals("B.Tech", r.get("education"));
        assertEquals("Intern", r.get("experience"));
        assertEquals("Python, C", r.get("skills"));
    }
    @Test void supportsCommonHeadings() {
        var r = sections.extract("Skills:\nJava, SQL\nExperience\n2 years\nEducation\nBSc\nProjects\nExample");
        assertEquals("Java, SQL", r.get("skills"));
        assertEquals("2 years", r.get("experience"));
        assertEquals("BSc", r.get("education"));
    }
    @Test void bodyTextIsNotAHeading() {
        assertEquals("Built education software", sections.extract("Experience\nBuilt education software\nSkills\nJava").get("experience"));
        assertEquals("", sections.extract("No headings").get("skills"));
    }
}
