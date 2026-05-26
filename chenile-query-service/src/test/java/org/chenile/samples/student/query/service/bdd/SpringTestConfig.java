package org.chenile.samples.student.query.service.bdd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.util.Map;
import javax.sql.DataSource;

@Configuration
@SpringBootApplication(scanBasePackages = { "org.chenile"})
@ActiveProfiles("unittest")
public class SpringTestConfig extends SpringBootServletInitializer{

	@Bean
	public ApplicationRunner querySchemaInitializer(@Qualifier("queryTargetDataSources") Map<String, DataSource> targetDataSources,
			Environment environment) {
		return args -> {
			for (Map.Entry<String, DataSource> entry : targetDataSources.entrySet()) {
				String tenantId = entry.getKey();
				String provider = environment.getProperty("query.test.schema",
						environment.getProperty("query.provider", "mybatis"));
				ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
				populator.addScript(schemaScript(provider, tenantId));
				populator.execute(entry.getValue());
			}
		};
	}

	private ClassPathResource schemaScript(String provider, String tenantId) {
		ClassPathResource providerTenantScript = new ClassPathResource("schema-" + provider + "-" + tenantId + ".sql");
		if (providerTenantScript.exists()) {
			return providerTenantScript;
		}
		ClassPathResource providerScript = new ClassPathResource("schema-" + provider + ".sql");
		if (providerScript.exists()) {
			return providerScript;
		}
		ClassPathResource tenantScript = new ClassPathResource("schema-" + tenantId + ".sql");
		if (tenantScript.exists()) {
			return tenantScript;
		}
		return new ClassPathResource("schema.sql");
	}
}
