package com.uidai.governance.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the Swagger UI at {@code /meetings/docs} (context-path + {@code /docs}).
 *
 * <p>Uses an internal forward to the springdoc UI resources rather than a
 * redirect, so the browser address bar stays on {@code /docs} instead of jumping
 * to {@code /swagger-ui/index.html}. The springdoc-generated
 * {@code swagger-initializer.js} already points at {@code /meetings/v3/api-docs},
 * so the forwarded page loads this API's spec.</p>
 */
@Hidden
@Controller
public class SwaggerUiController {

    @GetMapping("/docs")
    public String docs() {
        return "forward:/swagger-ui/index.html";
    }
}
