package com.scrapDetection.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    public static final String DEVICE_ID_AUTH = "deviceIdAuth";
    public static final String DEVICE_KEY_AUTH = "deviceKeyAuth";

    @Bean
    public OpenAPI recyClickOpenApi() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT returned by POST /api/auth/login");

        SecurityScheme deviceIdScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Device-Id")
                .description("Numeric camera device ID");

        SecurityScheme deviceKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Device-Key")
                .description("Raw camera device key");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerScheme)
                        .addSecuritySchemes(DEVICE_ID_AUTH, deviceIdScheme)
                        .addSecuritySchemes(DEVICE_KEY_AUTH, deviceKeyScheme));
    }

    @Bean
    public OperationCustomizer preAuthorizeSecurityCustomizer() {
        return OpenApiConfig::applyPreAuthorizeSecurity;
    }

    @Bean
    public OpenApiCustomizer deviceEndpointSecurityCustomizer() {
        return OpenApiConfig::applyDeviceEndpointSecurity;
    }

    static Operation applyPreAuthorizeSecurity(Operation operation, HandlerMethod handlerMethod) {
        boolean methodSecured = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), PreAuthorize.class) != null;
        boolean controllerSecured = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(), PreAuthorize.class) != null;

        if (methodSecured || controllerSecured) {
            operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
        }
        return operation;
    }

    static void applyDeviceEndpointSecurity(OpenAPI openApi) {
        setDevicePostSecurity(openApi, "/api/detections");
        setDevicePostSecurity(openApi, "/api/detections/frame");
    }

    private static void setDevicePostSecurity(OpenAPI openApi, String path) {
        if (openApi.getPaths() == null) {
            return;
        }

        PathItem pathItem = openApi.getPaths().get(path);
        if (pathItem == null || pathItem.getPost() == null) {
            return;
        }

        SecurityRequirement deviceCredentials = new SecurityRequirement()
                .addList(DEVICE_ID_AUTH)
                .addList(DEVICE_KEY_AUTH);
        pathItem.getPost().setSecurity(List.of(deviceCredentials));
    }
}
