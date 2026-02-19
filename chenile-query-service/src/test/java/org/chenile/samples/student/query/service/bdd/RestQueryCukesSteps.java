package org.chenile.samples.student.query.service.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(webEnvironment = WebEnvironment.MOCK,classes = SpringTestConfig.class,
  properties = {"spring.profiles.active=unittest"})
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("unittest")
public class RestQueryCukesSteps {
	// Create a dummy method so that Cucumber thinks of this as a steps implementation.
	@Given("dummy")
	public void dummy() {
		
	}
}
