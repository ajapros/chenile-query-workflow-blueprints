package org.chenile.samples.student.query.service.bdd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.util.Map;
import javax.sql.DataSource;

@Configuration
@SpringBootApplication(scanBasePackages = { "org.chenile"})
@ActiveProfiles("unittest")
public class SpringTestConfig extends SpringBootServletInitializer{

	@Bean
	public ApplicationRunner querySchemaInitializer(@Qualifier("queryTargetDataSources") Map<String, DataSource> targetDataSources) {
		return args -> {
			for (Map.Entry<String, DataSource> entry : targetDataSources.entrySet()) {
				String tenantId = entry.getKey();
				ClassPathResource tenantScript = new ClassPathResource("schema-" + tenantId + ".sql");
				ClassPathResource defaultScript = new ClassPathResource("schema.sql");
				ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
				if (tenantScript.exists()) {
					populator.addScript(tenantScript);
				} else {
					populator.addScript(defaultScript);
				}
				populator.execute(entry.getValue());
			}
		};
	}
}
