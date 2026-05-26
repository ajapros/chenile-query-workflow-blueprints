package org.chenile.samples.student.query.service.nocountbdd;

import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.chenile.samples.student.query.service.bdd.SpringTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = SpringTestConfig.class,
		properties = {"spring.profiles.active=unittest,count-query-off"})
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles({"unittest", "count-query-off"})
public class NoCountRestQueryCukesSteps {
	// Create a dummy method so that Cucumber thinks of this as a steps implementation.
	@Given("dummy no count")
	public void dummyNoCount() {

	}
}
