package com.scrapDetection.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    @Test
    void rejectsSecretsShorterThan64Bytes() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", "too-short");

        assertThrows(IllegalStateException.class, service::validateSecret);
    }

    @Test
    void acceptsSecretsWithAtLeast64Bytes() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(
                service,
                "secretKey",
                "test-only-jwt-secret-that-is-long-enough-for-hs512-signatures-0000000000000000"
        );

        assertDoesNotThrow(service::validateSecret);
    }
}
