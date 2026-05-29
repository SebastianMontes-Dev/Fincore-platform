package com.fincore.platform.infrastructure.common.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI finCoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinCore Platform API")
                        .description("API del modulo de infraestructura de FinCore")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .addSecurityItem(new SecurityRequirement().addList("API-Key"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes("API-Key", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER).name("X-API-Key")));
    }
}
