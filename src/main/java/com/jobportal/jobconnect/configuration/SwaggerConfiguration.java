package com.jobportal.jobconnect.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI jobConnectOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JobConnect API Documentation")
                        .description("API documentation for Job Portal Application")
                        .version("1.0"));
    }

}
