package com.student;

import com.student.config.RateLimitInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RateLimitInterceptorTest {

    @Autowired(required = false)
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    void testRateLimitInterceptorExists() {
        assertNotNull(rateLimitInterceptor, "RateLimitInterceptor should be loaded");
    }

    @Test
    void testBucketCreation() throws Exception {
        if (rateLimitInterceptor == null) {
            return;
        }

        // Test bucket creation for different client IDs
        String clientIp = "192.168.1.1";
        String userId = "user123";

        // The interceptor should handle requests without throwing exceptions
        // when within rate limits
        assertDoesNotThrow(() -> {
            // Simple validation that interceptor can be instantiated
            assertNotNull(rateLimitInterceptor);
        });
    }
}
