package org.chenile.workflow.service.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.chenile.core.init.AbstractServiceInitializer;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.workflow.service.stmcmds.StmBodyTypeSelector;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Configuration
public class ChenileOpenApiConfiguration {

    @Autowired
    private ChenileConfiguration serviceConfiguration;

    private PathItem buildActivityPath(String k, String  t) {

        Operation postOperation = new Operation()
                .summary(k +" Todo Summary")
                .description(k +" Todo key")
                .requestBody(
                        new io.swagger.v3.oas.models.parameters.RequestBody()
                                .required(true)
                                .content(new Content().addMediaType(
                                        "application/json",
                                        new MediaType().schema(
                                                new Schema<>().$ref("#/components/schemas/"+t)
                                        )
                                ))
                )
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("OK"))
                );

        return new PathItem().post(postOperation);
    }

    @Bean
    public OpenApiCustomizer activityOpenApi(@Autowired List<StmBodyTypeSelector> bodyTypeSelectors) {


        return openApi -> {

            // Ensure paths exist
            if (openApi.getPaths() == null) {
                openApi.setPaths(new Paths());
            }

            // Ensure components exist
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }


            serviceConfiguration.getServices().forEach((name,sd)->{

                sd.getOperations().forEach(ods->{

                    if(ods.getBodyTypeSelector()!=null){
                        System.out.println(ods.getBodyTypeSelector());

                        if(ods.getBodyTypeSelector() instanceof AbstractServiceInitializer.InterceptorChain){

                            AbstractServiceInitializer.InterceptorChain chain = (AbstractServiceInitializer.InterceptorChain) ods.getBodyTypeSelector();

                            System.out.println(ods.getBodyTypeSelector());
                        }
                    }



                });

            });

            for(StmBodyTypeSelector bodyTypeSelector: bodyTypeSelectors){
                bodyTypeSelector.storeBodyTypeSelector();
                bodyTypeSelector.getConfigs().forEach((k,t)->{

                    Map<String, Schema> schemas =
                            ModelConverters.getInstance().read(t.getType());

                    schemas.forEach((schemaName, schema) -> {
                        openApi.getComponents().addSchemas(schemaName, schema);
                    });

                    String path = "/"+ Instant.now().getEpochSecond()+"/" + k;
                    openApi.getPaths().addPathItem(path, buildActivityPath(k,schemas.keySet().stream().findFirst().get()));


                });
            }

            // Create event-specific endpoints

        };
    }

}
