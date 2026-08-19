package com.scrapDetection.config;

import com.scrapDetection.controller.DetectionFrameController;
import com.scrapDetection.controller.DeviceController;
import com.scrapDetection.controller.TransactionController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void recyClickOpenApi_registersAuthenticationSchemesWithoutGlobalRequirement() {
        OpenAPI openApi = config.recyClickOpenApi();

        SecurityScheme bearer = openApi.getComponents()
                .getSecuritySchemes()
                .get(OpenApiConfig.BEARER_AUTH);
        SecurityScheme deviceId = openApi.getComponents()
                .getSecuritySchemes()
                .get(OpenApiConfig.DEVICE_ID_AUTH);
        SecurityScheme deviceKey = openApi.getComponents()
                .getSecuritySchemes()
                .get(OpenApiConfig.DEVICE_KEY_AUTH);

        assertNotNull(bearer);
        assertEquals(SecurityScheme.Type.HTTP, bearer.getType());
        assertEquals("bearer", bearer.getScheme());
        assertEquals("JWT", bearer.getBearerFormat());

        assertHeaderScheme(deviceId, "X-Device-Id");
        assertHeaderScheme(deviceKey, "X-Device-Key");
        assertTrue(openApi.getSecurity() == null || openApi.getSecurity().isEmpty());
    }

    @Test
    void preAuthorizeSecurityCustomizer_marksOnlySecuredOperationsAsBearer() throws NoSuchMethodException {
        ExampleController controller = new ExampleController();

        Operation secured = new Operation();
        HandlerMethod securedHandler = new HandlerMethod(
                controller, ExampleController.class.getDeclaredMethod("secured"));
        OpenApiConfig.applyPreAuthorizeSecurity(secured, securedHandler);

        assertEquals(1, secured.getSecurity().size());
        assertTrue(secured.getSecurity().get(0).containsKey(OpenApiConfig.BEARER_AUTH));

        Operation publicOperation = new Operation();
        HandlerMethod publicHandler = new HandlerMethod(
                controller, ExampleController.class.getDeclaredMethod("publicEndpoint"));
        OpenApiConfig.applyPreAuthorizeSecurity(publicOperation, publicHandler);

        assertNull(publicOperation.getSecurity());
    }

    @Test
    void deviceEndpointSecurityCustomizer_requiresBothDeviceHeadersForPostOnly() {
        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/api/detections", new PathItem()
                        .get(new Operation())
                        .post(new Operation()))
                .addPathItem("/api/detections/frame", new PathItem()
                        .get(new Operation())
                        .post(new Operation())));

        OpenApiConfig.applyDeviceEndpointSecurity(openApi);

        assertDeviceSecurity(openApi.getPaths().get("/api/detections").getPost());
        assertDeviceSecurity(openApi.getPaths().get("/api/detections/frame").getPost());
        assertNull(openApi.getPaths().get("/api/detections").getGet().getSecurity());
        assertNull(openApi.getPaths().get("/api/detections/frame").getGet().getSecurity());
    }

    @Test
    void authenticatedMethodsWithoutPreAuthorize_declareBearerSecurity() throws NoSuchMethodException {
        assertBearerAnnotation(TransactionController.class.getMethod("getTransactionSummaries"));
        assertBearerAnnotation(TransactionController.class.getMethod(
                "getTransactionsByDateRange", LocalDateTime.class, LocalDateTime.class));
        assertBearerAnnotation(DeviceController.class.getMethod("getDeviceById", Long.class));
        assertBearerAnnotation(DetectionFrameController.class.getMethod("getLatestFrame"));
    }

    private static void assertHeaderScheme(SecurityScheme scheme, String headerName) {
        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.APIKEY, scheme.getType());
        assertEquals(SecurityScheme.In.HEADER, scheme.getIn());
        assertEquals(headerName, scheme.getName());
    }

    private static void assertDeviceSecurity(Operation operation) {
        assertEquals(1, operation.getSecurity().size());
        assertEquals(2, operation.getSecurity().get(0).size());
        assertTrue(operation.getSecurity().get(0).containsKey(OpenApiConfig.DEVICE_ID_AUTH));
        assertTrue(operation.getSecurity().get(0).containsKey(OpenApiConfig.DEVICE_KEY_AUTH));
    }

    private static void assertBearerAnnotation(Method method) {
        SecurityRequirement requirement = method.getAnnotation(SecurityRequirement.class);
        assertNotNull(requirement);
        assertEquals(OpenApiConfig.BEARER_AUTH, requirement.name());
    }

    static class ExampleController {
        @PreAuthorize("isAuthenticated()")
        public void secured() {
        }

        public void publicEndpoint() {
        }
    }
}
