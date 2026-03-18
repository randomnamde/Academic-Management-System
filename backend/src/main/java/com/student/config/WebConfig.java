package com.student.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册日志追踪拦截器 - 最先执行
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**", "/auth/**")
                .excludePathPatterns("/swagger-ui/**",
                        "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/doc.html");

        // 注册限流拦截器
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**", "/auth/**")
                .excludePathPatterns("/auth/login", "/public/**", "/swagger-ui/**",
                        "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/doc.html");
    }
}
