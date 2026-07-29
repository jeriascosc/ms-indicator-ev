package com.trycore.msindicatorev.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentacion OpenAPI publicada en /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msIndicatorEvOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ms-indicator-ev API")
                .version("1.0.0")
                .description("""
                        API REST para gestionar y analizar el comportamiento de un proyecto mediante la \
                        metodologia Earned Value Management (EVM).

                        Expone el CRUD de actividades y dos sub-recursos de calculo: los indicadores \
                        (PV, EV, CV, SV, CPI, SPI, EAC, VAC) y su interpretacion de negocio.

                        Convenciones:
                        - Los porcentajes se expresan como fraccion entre 0.0 y 1.0 (0.75 = 75%).
                        - Los indicadores cuyo divisor es cero se devuelven como null (no calculables).
                        - Los errores siguen el formato ProblemDetail (RFC 7807).
                        - Las representaciones incluyen enlaces hipermedia (nivel 3 de Richardson).
                        """)
                .contact(new Contact().name("Trycore").url("https://github.com/jeriascosc/ms-indicator-ev"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
