package ru.vsu.ppa.simplecode.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobe.client")
public record JobeClientProperties(
        Duration timeout,
        String baseUrl,
        int maxErrorsInRow) {
}
