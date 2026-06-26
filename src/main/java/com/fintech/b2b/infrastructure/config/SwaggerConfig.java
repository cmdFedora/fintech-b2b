package com.fintech.b2b.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // nombre interno a nuestro esquema de seguridad
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Metadatos básicos
                .info(new Info()
                        .title("B2B Fintech API")
                        .version("1.0")
                        .description("Documentación interactiva de la API financiera transaccional B2B."))
                
                // Aplicar el requisito de seguridad a toda la API por defecto
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                
                // Definir el "Componente de Seguridad" (El Candado Físico)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}