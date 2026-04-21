package org.chenile.workflow.service.testmulti;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/testmulti",
		glue = {"classpath:org/chenile/cucumber/rest", "classpath:org/chenile/workflow/service/testmulti"},
        plugin = {"pretty"}
        )
@ActiveProfiles("unittest")
public class CukesRestTest {
}
