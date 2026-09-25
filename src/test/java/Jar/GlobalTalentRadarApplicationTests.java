package Jar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "RUN_DATABASE_TESTS", matches = "true")
class GlobalTalentRadarApplicationTests {

	@Test
	void contextLoads() {
	}

}
