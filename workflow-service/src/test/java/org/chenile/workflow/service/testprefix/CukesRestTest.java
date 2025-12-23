package org.chenile.workflow.service.testprefix;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;


@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/testprefix",
		glue = {"classpath:org/chenile/cucumber/rest", "classpath:org/chenile/workflow/service/testprefix"},
        plugin = {"pretty"}
        )
@ActiveProfiles("unittest")

public class CukesRestTest {

}
