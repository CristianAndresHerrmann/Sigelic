package com.example.sigelic.util;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.example.sigelic.repository.PagoRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para generar números de comprobante de forma automática
 * Formato: PPP-NNNNNNNN donde PPP es el prefijo del año y NNNNNNNN es el número secuencial
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ComprobanteNumberGenerator {

    private final PagoRepository pagoRepository;
    
    private static final AtomicLong contador = new AtomicLong(0);
    private static final String FORMATO_NUMERO = "%08d";
    private static final String SEPARADOR = "-";
    
    /**
     * Inicializa el contador con el último número de comprobante existente en la BD
     */
    @PostConstruct
    public void inicializarContador() {
        try {
            // Buscar el último comprobante en la base de datos
            String ultimoComprobante = pagoRepository.findUltimoComprobante();
            
            if (ultimoComprobante != null && !ultimoComprobante.trim().isEmpty()) {
                Long ultimoNumero = extraerNumeroDelComprobante(ultimoComprobante);
                if (ultimoNumero != null) {
                    contador.set(ultimoNumero);
                    log.info("Contador inicializado con el último comprobante: {} (número: {})", 
                            ultimoComprobante, ultimoNumero);
                } else {
                    log.warn("No se pudo extraer número del comprobante: {}", ultimoComprobante);
                }
            } else {
                log.info("No se encontraron comprobantes previos, iniciando contador en 0");
            }
        } catch (Exception e) {
            log.warn("Error al inicializar contador desde BD, iniciando en 0: {}", e.getMessage());
        }
    }
    
    /**
     * Genera el siguiente número de comprobante para el año actual
     * Formato: 025-00000001 (para el año 2025)
     */
    public String generarNumeroComprobante() {
        String prefijo = generarPrefijo(LocalDate.now().getYear());
        long numero = contador.incrementAndGet();
        String numeroFormateado = String.format(FORMATO_NUMERO, numero);
        
        String comprobante = prefijo + SEPARADOR + numeroFormateado;
        log.debug("Número de comprobante generado: {}", comprobante);
        
        return comprobante;
    }

    /**
     * Genera el siguiente número de comprobante para un año específico
     */
    public String generarNumeroComprobante(int anio) {
        String prefijo = generarPrefijo(anio);
        long numero = contador.incrementAndGet();
        String numeroFormateado = String.format(FORMATO_NUMERO, numero);
        
        String comprobante = prefijo + SEPARADOR + numeroFormateado;
        log.debug("Número de comprobante generado para año {}: {}", anio, comprobante);
        
        return comprobante;
    }

    /**
     * Genera un número de comprobante con un número específico (para casos especiales)
     */
    public String generarNumeroComprobanteConNumero(long numero) {
        String prefijo = generarPrefijo(LocalDate.now().getYear());
        String numeroFormateado = String.format(FORMATO_NUMERO, numero);
        
        String comprobante = prefijo + SEPARADOR + numeroFormateado;
        log.debug("Número de comprobante generado con número específico {}: {}", numero, comprobante);
        
        return comprobante;
    }

    /**
     * Previsualiza el próximo número de comprobante sin incrementar el contador
     */
    public String previsualizarSiguienteComprobante() {
        String prefijo = generarPrefijo(LocalDate.now().getYear());
        long siguienteNumero = contador.get() + 1;
        String numeroFormateado = String.format(FORMATO_NUMERO, siguienteNumero);
        
        return prefijo + SEPARADOR + numeroFormateado;
    }

    /**
     * Obtiene el último número generado
     */
    public long getUltimoNumero() {
        return contador.get();
    }

    /**
     * Obtiene el contador actual (próximo número a asignar)
     */
    public long getContadorActual() {
        return contador.get() + 1;
    }

    /**
     * Reinicia el contador (usar con cuidado, solo para casos especiales)
     */
    public void reiniciarContador() {
        contador.set(0);
        log.warn("Contador de comprobantes reiniciado a 0");
    }

    /**
     * Establece el contador en un valor específico (para sincronizar con la base de datos)
     */
    public void establecerContador(long valor) {
        contador.set(valor);
        log.info("Contador de comprobantes establecido en: {}", valor);
    }

    /**
     * Genera el prefijo para el año especificado
     * Formato: últimos 3 dígitos del año (2024 -> 024, 2025 -> 025)
     */
    private String generarPrefijo(int anio) {
        return String.format("%03d", anio % 1000);
    }

    /**
     * Valida si un número de comprobante tiene el formato correcto
     */
    public boolean validarFormatoComprobante(String numeroComprobante) {
        if (numeroComprobante == null || numeroComprobante.trim().isEmpty()) {
            return false;
        }
        
        // Formato esperado: XXX-NNNNNNNN
        String regex = "^\\d{3}-\\d{8}$";
        return numeroComprobante.matches(regex);
    }

    /**
     * Extrae el año del número de comprobante
     */
    public Integer extraerAnioDelComprobante(String numeroComprobante) {
        if (!validarFormatoComprobante(numeroComprobante)) {
            return null;
        }
        
        String prefijo = numeroComprobante.substring(0, 3);
        int sufijo = Integer.parseInt(prefijo);
        
        // Asumir que es del siglo 21 (2000-2099)
        return 2000 + sufijo;
    }

    /**
     * Extrae el número secuencial del comprobante
     */
    public Long extraerNumeroDelComprobante(String numeroComprobante) {
        if (!validarFormatoComprobante(numeroComprobante)) {
            return null;
        }
        
        String numeroStr = numeroComprobante.substring(4); // Después del guión
        return Long.parseLong(numeroStr);
    }

    /**
     * Obtiene información del estado actual del generador
     */
    public String obtenerInfoEstado() {
        return String.format("Último número: %d | Siguiente: %s", 
                getUltimoNumero(), 
                previsualizarSiguienteComprobante());
    }
}
