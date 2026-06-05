package org.chenile.configuration.query.mcp;

import org.chenile.query.service.impl.QueryDefinitions;
import org.chenile.query.service.impl.QueryPolymorph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Optional MCP beans for Chenile Query.
 */
@Configuration
public class QueryMcpConfiguration {

    @Bean("queryPolymorph")
    QueryPolymorph queryPolymorph(@Autowired @Qualifier("queryDefinitions") QueryDefinitions queryDefinitions) {
        return new QueryPolymorph(queryDefinitions);
    }
}
