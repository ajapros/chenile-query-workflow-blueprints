package org.chenile.configuration.query.mcp;

import org.chenile.query.service.impl.QueryDefinitions;
import org.chenile.query.service.impl.QueryPolymorph;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class QueryMcpConfigurationTest {

    @Test
    void registersQueryPolymorphBean() throws IOException {
        QueryDefinitions queryDefinitions = new QueryDefinitions(new ClassPathResource[]{
                new ClassPathResource("org/chenile/samples/query/service/mapper/student.json")
        });

        QueryPolymorph queryPolymorph = new QueryMcpConfiguration().queryPolymorph(queryDefinitions);

        assertNotNull(queryPolymorph);
    }
}
