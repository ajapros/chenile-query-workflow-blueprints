package org.chenile.workflow.service.testprefix;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;


@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/testprefix",
		glue = {"classpath:org/chenile/cucumber/rest", "classpath:org/chenile/workflow/service/testprefix"},
        plugin = {"pretty"}
        )
@ActiveProfiles("unittest")

public class CukesRestTest {

}
