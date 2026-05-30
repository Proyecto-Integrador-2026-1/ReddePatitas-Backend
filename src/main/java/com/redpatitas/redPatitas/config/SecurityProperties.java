package com.redpatitas.redPatitas.config;

import org.springframework.stereotype.Component;

@Component
public class SecurityProperties {
    // Placeholder. Could be bound to properties.
    public boolean isHstsEnabled() {
        return false;
    }
    // Permitir autenticación por cabeceras (solo para desarrollo/manual testing)
    public boolean isAllowHeaderAuth() {
        return true;
    }
}
