package com.forum.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();
    private final Properties props = new Properties();

    private AppConfig() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load app.properties", e);
        }
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String get(String key, String defaultValue) {
        // Environment variables override file properties (e.g. ORACLE_URL overrides oracle.url)
        String envKey = key.replace('.', '_').toUpperCase();
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }
        String value = props.getProperty(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    public String getRequired(String key) {
        String value = get(key, "");
        if (value.isEmpty()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }
}
