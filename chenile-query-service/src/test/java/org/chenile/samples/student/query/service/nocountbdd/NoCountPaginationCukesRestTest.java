package org.chenile.samples.student.query.service.nocountbdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/rest/no-count-features",
		glue = {"classpath:org/chenile/cucumber/rest",
				"classpath:org/chenile/samples/student/query/service/nocountbdd"},
		plugin = {"pretty"})
@ActiveProfiles({"unittest", "count-query-off"})
public class NoCountPaginationCukesRestTest {

}
