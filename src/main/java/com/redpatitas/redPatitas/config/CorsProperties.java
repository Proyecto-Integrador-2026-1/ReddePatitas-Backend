package com.redpatitas.redPatitas.config;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CorsProperties {
    // Simple placeholder. Adjust to read from properties if needed.
    private String allowedOrigins = "http://localhost:3000, http://localhost:5173";

    public List<String> allowedOriginList() {
		return Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();
	}
}
