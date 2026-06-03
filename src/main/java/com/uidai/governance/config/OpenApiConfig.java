package com.uidai.governance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI definition for the Meetings &amp; MoM Governance Module.
 *
 * <p>Swagger UI is served at {@code /api/swagger-ui.html} and the raw OpenAPI
 * document at {@code /api/v3/api-docs} (the {@code /api} prefix comes from
 * {@code server.servlet.context-path}).</p>
 */
@Configuration
public class OpenApiConfig {

    /** The header the audit subsystem reads to attribute actions to a user. */
    private static final String USER_HEADER = "X-User-Id";

    /** Name of the bearer-token security scheme, applied to every operation. */
    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI meetingsMomOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UIDAI Meetings & Minutes of Meeting Governance API")
                        .version("v1")
                        .description("""
                                Structured, auditable capture of meeting outcomes, action-item tracking \
                                and accountability across governance, steering and migration meetings \
                                (UIDAI / PMC / MSP). Implements requirements MEET-FR-01 through MEET-FR-05.

                                Send the `X-User-Id` header on write requests so actions are attributed \
                                in the audit trail. Click **Authorize** and paste your JWT to send it as \
                                `Authorization: Bearer <token>`; it is forwarded to the User and Activity \
                                services (e.g. for participant validation).""")
                        .contact(new Contact().name("UIDAI Governance Platform"))
                        .license(new License().name("Proprietary - UIDAI")))
                .servers(List.of(new Server().url("/api").description("Default (context-path)")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT bearer token forwarded to the external User & Activity services")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    /**
     * Adds the optional {@code X-User-Id} header to every operation so it can be
     * supplied directly from Swagger UI when exercising write endpoints.
     */
    @Bean
    public OperationCustomizer userHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            boolean alreadyPresent = operation.getParameters() != null
                    && operation.getParameters().stream()
                    .anyMatch(p -> USER_HEADER.equals(p.getName()));
            if (!alreadyPresent) {
                operation.addParametersItem(new HeaderParameter()
                        .name(USER_HEADER)
                        .description("Acting user id, recorded in the audit trail")
                        .required(false)
                        .schema(new StringSchema()));
            }
            return operation;
        };
    }
}
