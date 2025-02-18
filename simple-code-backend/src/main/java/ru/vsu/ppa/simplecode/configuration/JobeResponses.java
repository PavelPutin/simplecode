package ru.vsu.ppa.simplecode.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobe.responses")
public record JobeResponses(int compilationError,
                            int runtimeError,
                            int timeLimitExceeded,
                            int ok,
                            int memoryLimitExceeded,
                            int illegalSystemCall,
                            int internalError,
                            int serverOverload) {
}
