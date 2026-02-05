package com.mpt.practyp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI practypOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SWAGGER FOR API")
                        .description("SWAGGGER")
                        .version("v1.0.0"));
    }
}