package com.example.sigelic.config;

import com.example.sigelic.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Componente que inicializa la aplicación con datos por defecto
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitializer implements ApplicationRunner {

    private final ConfiguracionService configuracionService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Iniciando configuración de la aplicación SIGELIC");
        
        try {
            // Inicializar configuraciones por defecto
            configuracionService.inicializarConfiguracionesPorDefecto();
            
            log.info("Aplicación SIGELIC inicializada correctamente");
            
        } catch (Exception e) {
            log.error("Error durante la inicialización de la aplicación", e);
            throw e;
        }
    }
}
