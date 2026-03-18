package com.student.config;

import com.student.exception.BusinessException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口限流拦截器
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limit.requests-per-second:10}")
    private int requestsPerSecond;

    @Value("${rate-limit.capacity:20}")
    private int capacity;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 对非API请求放行
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/") && !uri.startsWith("/auth/")) {
            return true;
        }

        // 排除登录接口
        if (uri.contains("/auth/login") || uri.contains("/public/")) {
            return true;
        }

        String clientId = resolveClientId(request);
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            throw new BusinessException("请求过于频繁，请稍后再试");
        }
    }

    private String resolveClientId(HttpServletRequest request) {
        // 优先使用用户ID，如果没有登录则使用IP
        String userId = request.getParameter("userId");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }
        String ip = getClientIp(request);
        return "ip:" + ip;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(requestsPerSecond, Duration.ofSeconds(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
