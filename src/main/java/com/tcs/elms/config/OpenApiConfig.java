package com.tcs.elms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI elmsOpenAPI() {
        Info info = new Info()
                .title("ELMS API")
                .version("1.0.0")
                .description("Employee Leave Management System REST API — manage employees, " +
                             "submit leave requests, and allow managers to approve or reject them.")
                .contact(new Contact()
                        .name("TCS ELMS Team")
                        .email("elms-support@tcs.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local development server");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
