package com.scrapDetection.config;

import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.DeviceRepository;
import com.scrapDetection.security.device.DeviceApiKeyService;
import com.scrapDetection.security.device.DeviceAuthenticationFilter;
import com.scrapDetection.security.jwt.JwtAuthenticationFilter;
import com.scrapDetection.security.jwt.JwtService;
import com.scrapDetection.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitConfig(classes = {SecurityConfig.class, SecurityConfigTest.TestWebConfig.class})
@WebAppConfiguration
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void deviceRole_canPostDetections() throws Exception {
        mockMvc.perform(post("/api/detections")
                        .with(SecurityMockMvcRequestPostProcessors.user("camera").roles("DEVICE")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/detections/frame")
                        .with(SecurityMockMvcRequestPostProcessors.user("camera").roles("DEVICE")))
                .andExpect(status().isOk());
    }

    @Test
    void accountRole_cannotPostDetectionsAsDevice() throws Exception {
        mockMvc.perform(post("/api/detections")
                        .with(SecurityMockMvcRequestPostProcessors.user("staff").roles("STAFF")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/detections/frame")
                        .with(SecurityMockMvcRequestPostProcessors.user("staff").roles("STAFF")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deviceRole_cannotAccessProtectedAccountEndpoints() throws Exception {
        var device = SecurityMockMvcRequestPostProcessors.user("camera").roles("DEVICE");

        mockMvc.perform(get("/api/devices/1").with(device))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/account/me").with(device))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/logout").with(device))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/transactions/summary").with(device))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/transactions/date-range").with(device))
                .andExpect(status().isForbidden());
    }

    @Test
    void accountRole_canAccessAuthenticatedAccountEndpoint() throws Exception {
        var staff = SecurityMockMvcRequestPostProcessors.user("staff").roles("STAFF");

        mockMvc.perform(get("/api/transactions/summary").with(staff))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/logout").with(staff))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUser_cannotAccessProtectedAccountEndpoint() throws Exception {
        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUser_canAccessPublicLoginEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void corsOrigins_areReadFromCommaSeparatedConfiguration() {
        SecurityConfig securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(DeviceAuthenticationFilter.class)
        );

        var source = securityConfig.corsConfigurationSource(
                "https://web.example, https://admin.example"
        );
        var cors = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/account/me")
        );

        assertNotNull(cors);
        assertEquals(
                java.util.List.of("https://web.example", "https://admin.example"),
                cors.getAllowedOrigins()
        );
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    static class TestWebConfig {

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(
                    mock(JwtService.class),
                    mock(AccountRepository.class),
                    mock(SessionService.class)
            );
        }

        @Bean
        DeviceAuthenticationFilter deviceAuthenticationFilter() {
            return new DeviceAuthenticationFilter(
                    mock(DeviceRepository.class),
                    mock(DeviceApiKeyService.class)
            );
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {

        @PostMapping({"/api/detections", "/api/detections/frame"})
        void postDetection() {
        }

        @PostMapping({"/api/auth/login", "/api/auth/logout"})
        void authEndpoint() {
        }

        @GetMapping({
                "/api/devices/1",
                "/api/account/me",
                "/api/transactions/summary",
                "/api/transactions/date-range"
        })
        void accountEndpoint() {
        }
    }
}
