package com.redpatitas.redPatitas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI redPatiasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ReddePatitas Backend API")
                        .description("API para reportes de mascotas perdidas")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ReddePatitas Team")
                                .email("support@reddepatitas.com")
                                .url("https://reddepatitas.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
