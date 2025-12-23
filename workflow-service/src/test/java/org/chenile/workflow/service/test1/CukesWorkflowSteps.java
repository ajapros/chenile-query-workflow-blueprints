package org.chenile.workflow.service.test1;

import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

/**
 * A dummy steps class to pull together the spring configuration
 * @author Raja Shankar Kolluru
 *
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = ServiceTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("unittest")
public class CukesWorkflowSteps {
	@Given("Dummy")
	public void dummy() {
		
	}	
}
