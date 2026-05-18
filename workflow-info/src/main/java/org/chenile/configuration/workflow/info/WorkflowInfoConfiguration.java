package org.chenile.configuration.workflow.info;

import org.chenile.configuration.workflow.info.service.WorkflowInfoServiceImpl;
import org.chenile.workflow.cli.CLIHelper;
import org.chenile.workflow.info.service.WorkflowInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowInfoConfiguration {
    @Bean(name = "workflowInfoCliHelper")
    CLIHelper workflowInfoCliHelper() {
        return new CLIHelper();
    }

    @Bean(name = "workflowInfoService")
    WorkflowInfoService workflowInfoService(@Qualifier("workflowInfoCliHelper") CLIHelper cliHelper) {
        return new WorkflowInfoServiceImpl(cliHelper);
    }
}
