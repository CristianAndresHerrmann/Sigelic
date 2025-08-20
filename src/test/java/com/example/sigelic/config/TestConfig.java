package com.example.sigelic.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuración específica para tests
 */
@TestConfiguration
@ComponentScan(excludeFilters = @ComponentScan.Filter(
    type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, 
    classes = {com.example.sigelic.service.DataInitializationService.class}
))
public class TestConfig {
    // Configuración específica para tests que excluye DataInitializationService
}
