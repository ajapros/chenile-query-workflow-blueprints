package org.chenile.workflow.service.test1;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;


@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features1",
		glue = {"classpath:org/chenile/cucumber/rest", "classpath:org/chenile/cucumber/workflow",
                "classpath:org/chenile/workflow/service/test1"},
        plugin = {"pretty"}
        )
@ActiveProfiles("unittest")

public class CukesRestTest {

}
