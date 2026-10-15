package dev.ferrox.showcase.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "PASETO v4 Bearer Auth";

        return new OpenAPI()
                .info(new Info()
                        .title("Ferrox-Java Enterprise Showcase API")
                        .version("2.0.0")
                        .description("State-of-the-Art Java 21 Enterprise Showcase running on Spring Boot 3.3 and Virtual Threads (Project Loom). " +
                                "Demonstrates PASETO v4 Cryptography, Sentinel Threat Engine (Shannon Entropy WAF), " +
                                "Zero-GC Panama MemorySegment Off-Heap Storage, Singleflight Cache Stampede Protection, " +
                                "and Decoupled Virtual-Thread CQRS.")
                        .contact(new Contact().name("Ferrox Ecosystem Team").url("https://ferrox.dev").email("info@ferrox.dev"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("PASETO")));
    }
}
