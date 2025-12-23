package org.chenile.workflow.service.test1;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;



@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features1",
		glue = {"classpath:org/chenile/cucumber/rest", "classpath:org/chenile/cucumber/workflow",
                "classpath:org/chenile/workflow/service/test1"},
        plugin = {"pretty"}
        )
@ActiveProfiles("unittest")

public class CukesRestTest {

}
