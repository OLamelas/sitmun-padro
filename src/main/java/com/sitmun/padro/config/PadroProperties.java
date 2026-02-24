package com.sitmun.padro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "padro")
public record PadroProperties(
        String aytosSoapUrl,
        String aytosUsername,
        String aytosPassword,
        String aytosPubKey
) {
}
