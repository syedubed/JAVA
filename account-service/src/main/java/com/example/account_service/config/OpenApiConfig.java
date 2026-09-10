package com.example.account_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Account Service API",
                version = "1.0",
                description = "API for creating and managing customer accounts"
        )
)
public class OpenApiConfig {
}