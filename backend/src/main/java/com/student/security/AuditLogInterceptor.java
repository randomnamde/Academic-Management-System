package com.student.security;

import com.student.entity.SysLog;
import com.student.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_KEY = "audit:start:ms";
    private static final int MAX_TEXT_LENGTH = 1000;

    private final SysLogService sysLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_KEY, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }

        try {
            long start = getStartTime(request);
            long duration = Math.max(System.currentTimeMillis() - start, 0L);

            SysLog sysLog = new SysLog();
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr instanceof String userId) {
                sysLog.setUserId(userId);
            }

            String requestMethod = request.getMethod();
            String uri = request.getRequestURI();
            String operation = handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName();
            sysLog.setOperation(operation);
            sysLog.setMethod(requestMethod + " " + uri);
            sysLog.setParams(truncate(serializeParams(request.getParameterMap())));
            sysLog.setIp(resolveClientIp(request));
            sysLog.setDuration(duration);
            boolean success = ex == null && response.getStatus() < 500;
            sysLog.setStatus(success ? 1 : 0);
            if (ex != null) {
                sysLog.setErrorMsg(truncate(ex.getMessage()));
            } else if (response.getStatus() >= 400) {
                sysLog.setErrorMsg("HTTP " + response.getStatus());
            }

            sysLogService.save(sysLog);
        } catch (Exception logEx) {
            log.warn("Audit log write failed: {}", logEx.getMessage());
        }
    }

    private long getStartTime(HttpServletRequest request) {
        Object start = request.getAttribute(START_TIME_KEY);
        if (start instanceof Long startLong) {
            return startLong;
        }
        return System.currentTimeMillis();
    }

    private String serializeParams(Map<String, String[]> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "";
        }
        return parameterMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String raw) {
        if (raw == null) {
            return null;
        }
        if (raw.length() <= MAX_TEXT_LENGTH) {
            return raw;
        }
        return raw.substring(0, MAX_TEXT_LENGTH);
    }
}
