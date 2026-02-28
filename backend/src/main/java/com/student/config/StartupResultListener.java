package com.student.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;

public class StartupResultListener implements ApplicationListener<ApplicationEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupResultListener.class);
    private volatile long startupBeginAt = System.nanoTime();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            startupBeginAt = System.nanoTime();
            return;
        }

        if (event instanceof ApplicationReadyEvent readyEvent) {
            logStartupSuccess(readyEvent);
            return;
        }

        if (event instanceof ApplicationFailedEvent failedEvent) {
            logStartupFailure(failedEvent);
        }
    }

    private void logStartupSuccess(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String appName = env.getProperty("spring.application.name", "application");
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "unknown"));
        String contextPath = env.getProperty("server.servlet.context-path", "");
        if (!StringUtils.hasText(contextPath)) {
            contextPath = "/";
        }

        String profiles = resolveProfiles(env);
        Duration elapsed = event.getTimeTaken() != null
                ? event.getTimeTaken()
                : Duration.ofNanos(Math.max(System.nanoTime() - startupBeginAt, 0L));

        log.info("Startup result: SUCCESS | app={} | port={} | contextPath={} | profiles={} | elapsed={}ms",
                appName, port, contextPath, profiles, elapsed.toMillis());
    }

    private void logStartupFailure(ApplicationFailedEvent event) {
        long elapsedMillis = Duration.ofNanos(Math.max(System.nanoTime() - startupBeginAt, 0L)).toMillis();
        Throwable throwable = event.getException();
        String reason = throwable == null ? "unknown" : throwable.getMessage();
        String exceptionName = throwable == null ? "UnknownException" : throwable.getClass().getSimpleName();
        log.error("Startup result: FAILED | exception={} | reason={} | elapsed={}ms", exceptionName, reason, elapsedMillis, throwable);
    }

    private String resolveProfiles(Environment env) {
        String[] profiles = env.getActiveProfiles();
        if (profiles.length == 0) {
            profiles = env.getDefaultProfiles();
        }
        String joined = String.join(",", Arrays.stream(profiles).filter(StringUtils::hasText).toList());
        return StringUtils.hasText(joined) ? joined : "default";
    }
}
