package com.example.sigelic.service;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import com.example.sigelic.security.Authorities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VencimientosScheduler {

    private final PagoService pagoService;
    private final LicenciaService licenciaService;

    /**
     * Tarea programada diaria para procesar vencimientos de pagos y actualizar licencias vencidas.
     * Se ejecuta todos los días a las 00:00 hora de Argentina.
     *
     * La zona es explícita porque el contenedor puede correr en UTC mientras la
     * base de datos usa America/Argentina/Buenos_Aires: sin ella el proceso se
     * ejecutaría con 3 horas de desfase.
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Argentina/Buenos_Aires")
    public void procesarVencimientosDiarios() {
        log.info("Iniciando proceso automático diario de vencimientos...");
        
        // Guardar el contexto original por seguridad
        SecurityContext originalContext = SecurityContextHolder.getContext();
        
        try {
            // Configurar contexto de seguridad mock con usuario SYSTEM y los permisos necesarios
            SecurityContext context = new SecurityContextImpl();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "SYSTEM", 
                null, 
                List.of(
                    new SimpleGrantedAuthority(Authorities.PROCESO_VENCIMIENTOS_EJECUTAR),
                    new SimpleGrantedAuthority(Authorities.LICENCIA_GESTIONAR_ESTADO)
                )
            );
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            log.info("Ejecutando pagoService.procesarPagosVencidos()...");
            pagoService.procesarPagosVencidos();
            log.info("procesarPagosVencidos() completado con éxito.");

            log.info("Ejecutando licenciaService.actualizarLicenciasVencidas()...");
            licenciaService.actualizarLicenciasVencidas();
            log.info("actualizarLicenciasVencidas() completado con éxito.");
            
        } catch (Exception e) {
            log.error("Error durante el proceso automático de vencimientos: {}", e.getMessage(), e);
        } finally {
            // Restaurar el contexto original o limpiar si no había
            if (originalContext != null && originalContext.getAuthentication() != null) {
                SecurityContextHolder.setContext(originalContext);
            } else {
                SecurityContextHolder.clearContext();
            }
            log.info("Proceso automático diario de vencimientos finalizado.");
        }
    }
}
