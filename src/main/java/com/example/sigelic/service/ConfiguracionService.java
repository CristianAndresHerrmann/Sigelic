package com.example.sigelic.service;

import com.example.sigelic.model.Configuracion;
import com.example.sigelic.repository.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de configuración del sistema
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    /**
     * Obtiene todas las configuraciones
     */
    @Transactional(readOnly = true)
    public List<Configuracion> findAll() {
        log.debug("Obteniendo todas las configuraciones");
        return configuracionRepository.findAllOrderedByCategoriaAndClave();
    }

    /**
     * Obtiene configuraciones por categoría
     */
    @Transactional(readOnly = true)
    public List<Configuracion> findByCategoria(String categoria) {
        log.debug("Obteniendo configuraciones de la categoría: {}", categoria);
        return configuracionRepository.findByCategoriaOrderByClave(categoria);
    }

    /**
     * Obtiene una configuración por su clave
     */
    @Transactional(readOnly = true)
    public Optional<Configuracion> findByClave(String clave) {
        log.debug("Obteniendo configuración con clave: {}", clave);
        return configuracionRepository.findByClave(clave);
    }

    /**
     * Obtiene el valor de una configuración por su clave
     */
    @Transactional(readOnly = true)
    public Optional<String> getValor(String clave) {
        log.debug("Obteniendo valor de configuración con clave: {}", clave);
        return configuracionRepository.findValorByClave(clave);
    }

    /**
     * Obtiene el valor de una configuración como String con valor por defecto
     */
    @Transactional(readOnly = true)
    public String getValor(String clave, String valorPorDefecto) {
        return getValor(clave).orElse(valorPorDefecto);
    }

    /**
     * Obtiene el valor de una configuración como Integer
     */
    @Transactional(readOnly = true)
    public Optional<Integer> getValorComoInteger(String clave) {
        return findByClave(clave)
                .map(Configuracion::getValorComoInteger);
    }

    /**
     * Obtiene el valor de una configuración como Integer con valor por defecto
     */
    @Transactional(readOnly = true)
    public Integer getValorComoInteger(String clave, Integer valorPorDefecto) {
        return getValorComoInteger(clave).orElse(valorPorDefecto);
    }

    /**
     * Obtiene el valor de una configuración como Boolean
     */
    @Transactional(readOnly = true)
    public Optional<Boolean> getValorComoBoolean(String clave) {
        return findByClave(clave)
                .map(Configuracion::getValorComoBoolean);
    }

    /**
     * Obtiene el valor de una configuración como Boolean con valor por defecto
     */
    @Transactional(readOnly = true)
    public Boolean getValorComoBoolean(String clave, Boolean valorPorDefecto) {
        return getValorComoBoolean(clave).orElse(valorPorDefecto);
    }

    /**
     * Guarda o actualiza una configuración
     */
    public Configuracion save(Configuracion configuracion) {
        log.info("Guardando configuración: {} = {}", configuracion.getClave(), configuracion.getValor());
        return configuracionRepository.save(configuracion);
    }

    /**
     * Actualiza el valor de una configuración existente
     */
    public Optional<Configuracion> actualizarValor(String clave, String nuevoValor, String usuario) {
        Optional<Configuracion> configOpt = findByClave(clave);
        
        if (configOpt.isPresent()) {
            Configuracion config = configOpt.get();
            
            if (!config.getModificable()) {
                log.warn("Intento de modificar configuración no modificable: {}", clave);
                throw new IllegalArgumentException("La configuración '" + clave + "' no es modificable");
            }
            
            String valorAnterior = config.getValor();
            config.setValor(nuevoValor);
            config.setActualizadoPor(usuario);
            
            log.info("Actualizando configuración: {} de '{}' a '{}' por usuario '{}'", 
                    clave, valorAnterior, nuevoValor, usuario);
            
            return Optional.of(save(config));
        }
        
        log.warn("No se encontró configuración con clave: {}", clave);
        return Optional.empty();
    }

    /**
     * Actualiza múltiples configuraciones
     */
    public void actualizarConfiguraciones(Map<String, String> configuraciones, String usuario) {
        log.info("Actualizando {} configuraciones por usuario '{}'", configuraciones.size(), usuario);
        
        for (Map.Entry<String, String> entry : configuraciones.entrySet()) {
            String clave = entry.getKey();
            String valor = entry.getValue();
            
            if (valor != null && !valor.trim().isEmpty()) {
                actualizarValor(clave, valor.trim(), usuario);
            }
        }
    }

    /**
     * Obtiene todas las configuraciones modificables agrupadas por categoría
     */
    @Transactional(readOnly = true)
    public Map<String, List<Configuracion>> getConfiguracionesModificablesPorCategoria() {
        log.debug("Obteniendo configuraciones modificables agrupadas por categoría");
        
        return configuracionRepository.findByModificableTrue()
                .stream()
                .collect(Collectors.groupingBy(Configuracion::getCategoria));
    }

    /**
     * Inicializa las configuraciones por defecto si no existen
     */
    public void inicializarConfiguracionesPorDefecto() {
        log.info("Inicializando configuraciones por defecto del sistema");
        
        // Configuración General
        crearConfiguracionSiNoExiste(
            "sistema.nombre", 
            "SIGELIC - Sistema de Gestión de Licencias", 
            "Nombre del sistema", 
            "GENERAL", 
            Configuracion.TipoConfiguracion.TEXT
        );
        
        crearConfiguracionSiNoExiste(
            "sistema.url", 
            "http://localhost:8080", 
            "URL base del sistema", 
            "GENERAL", 
            Configuracion.TipoConfiguracion.URL
        );
        
        crearConfiguracionSiNoExiste(
            "contacto.email", 
            "contacto@sigelic.gov.ar", 
            "Email de contacto", 
            "GENERAL", 
            Configuracion.TipoConfiguracion.EMAIL
        );
        
        crearConfiguracionSiNoExiste(
            "contacto.telefono", 
            "+54 342 4573000", 
            "Teléfono de contacto", 
            "GENERAL", 
            Configuracion.TipoConfiguracion.PHONE
        );

        // Configuración de Seguridad
        crearConfiguracionSiNoExiste(
            "seguridad.max_intentos_fallidos", 
            "3", 
            "Máximo de intentos fallidos antes de bloquear cuenta", 
            "SEGURIDAD", 
            Configuracion.TipoConfiguracion.INTEGER
        );
        
        crearConfiguracionSiNoExiste(
            "seguridad.tiempo_bloqueo_minutos", 
            "30", 
            "Tiempo de bloqueo en minutos", 
            "SEGURIDAD", 
            Configuracion.TipoConfiguracion.INTEGER
        );
        
        crearConfiguracionSiNoExiste(
            "seguridad.duracion_sesion_minutos", 
            "60", 
            "Duración de sesión en minutos", 
            "SEGURIDAD", 
            Configuracion.TipoConfiguracion.INTEGER
        );
        
        crearConfiguracionSiNoExiste(
            "seguridad.cambio_password_obligatorio", 
            "true", 
            "Requerir cambio de contraseña en primer acceso", 
            "SEGURIDAD", 
            Configuracion.TipoConfiguracion.BOOLEAN
        );

        // Configuración de Licencias
        crearConfiguracionSiNoExiste(
            "licencias.validez_anos", 
            "5", 
            "Años de validez por defecto para nuevas licencias", 
            "LICENCIAS", 
            Configuracion.TipoConfiguracion.INTEGER
        );
        
        crearConfiguracionSiNoExiste(
            "licencias.dias_aviso_vencimiento", 
            "90", 
            "Días antes del vencimiento para notificar", 
            "LICENCIAS", 
            Configuracion.TipoConfiguracion.INTEGER
        );

        log.info("Configuraciones por defecto inicializadas correctamente");
    }

    /**
     * Crea una configuración si no existe
     */
    private void crearConfiguracionSiNoExiste(String clave, String valor, String descripcion, 
                                            String categoria, Configuracion.TipoConfiguracion tipo) {
        if (!configuracionRepository.existsByClave(clave)) {
            Configuracion config = new Configuracion(clave, valor, descripcion, categoria, tipo);
            config.setActualizadoPor("SYSTEM");
            configuracionRepository.save(config);
            log.debug("Configuración creada: {} = {}", clave, valor);
        }
    }
}
