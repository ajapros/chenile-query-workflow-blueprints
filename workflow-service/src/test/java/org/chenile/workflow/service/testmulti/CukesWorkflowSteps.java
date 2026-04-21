package org.chenile.workflow.service.testmulti;

import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = ServiceTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("unittest")
public class CukesWorkflowSteps {
	@Given("Dummy")
	public void dummy() {
	}
}
