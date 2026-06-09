package com.example.sigelic.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoLicencia;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.LicenciaRepository;
import com.example.sigelic.security.Authorities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar licencias de conducir
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LicenciaService {

    private final LicenciaRepository licenciaRepository;

    /**
     * Busca una licencia por ID
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public Optional<Licencia> findById(Long id) {
        return licenciaRepository.findById(id);
    }

    /**
     * Obtiene todas las licencias
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public List<Licencia> findAll() {
        try {
            log.info("Iniciando búsqueda de todas las licencias...");
            List<Licencia> licencias = licenciaRepository.findAllWithTitular();
            log.info("Se encontraron {} licencias", licencias.size());
            return licencias;
        } catch (Exception e) {
            log.error("Error al buscar licencias: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Busca una licencia por número
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public Optional<Licencia> findByNumero(String numeroLicencia) {
        return licenciaRepository.findByNumeroLicencia(numeroLicencia);
    }

    /**
     * Obtiene todas las licencias de un titular
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public List<Licencia> findByTitular(Titular titular) {
        return licenciaRepository.findByTitular(titular);
    }

    /**
     * Obtiene las licencias vigentes de un titular
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public List<Licencia> findLicenciasVigentesByTitular(Titular titular) {
        return licenciaRepository.findLicenciasVigentesByTitular(titular);
    }

    /**
     * Emite una nueva licencia basada en un trámite
     */
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_EMITIR + "')")
    public Licencia emitirLicencia(Tramite tramite) {
        if (!tramite.todosLosRequisitosCumplidos()) {
            throw new IllegalStateException("No se puede emitir licencia sin cumplir todos los requisitos");
        }

        Titular titular = tramite.getTitular();
        ClaseLicencia clase = tramite.getClaseSolicitada();
        
        // Buscar si existe una licencia vigente previa para la misma clase
        List<Licencia> licenciasVigentes = licenciaRepository.findLicenciasVigentesByTitular(titular);
        Optional<Licencia> licenciaPreviaOpt = licenciasVigentes.stream()
                .filter(l -> l.getClase() == clase)
                .findFirst();

        LocalDate fechaVencimiento;
        
        if (tramite.getTipo() == TipoTramite.DUPLICADO || tramite.getTipo() == TipoTramite.CAMBIO_DOMICILIO) {
            if (licenciaPreviaOpt.isPresent()) {
                fechaVencimiento = licenciaPreviaOpt.get().getFechaVencimiento();
            } else {
                // Fallback si no hay previa
                int vigenciaAnios = Licencia.calcularVigenciaEnAnios(titular.getEdad(), false);
                fechaVencimiento = Licencia.calcularFechaVencimiento(titular.getFechaNacimiento(), LocalDate.now(), vigenciaAnios);
            }
        } else {
            // EMISION o RENOVACION
            boolean esPrimeraVez = tramite.getTipo() == TipoTramite.EMISION;
            int vigenciaAnios = Licencia.calcularVigenciaEnAnios(titular.getEdad(), esPrimeraVez);
            fechaVencimiento = Licencia.calcularFechaVencimiento(titular.getFechaNacimiento(), LocalDate.now(), vigenciaAnios);
        }

        // Si existe una licencia previa vigente, marcarla como DUPLICADA
        if (licenciaPreviaOpt.isPresent()) {
            Licencia licenciaPrevia = licenciaPreviaOpt.get();
            licenciaPrevia.setEstado(EstadoLicencia.DUPLICADA);
            licenciaRepository.save(licenciaPrevia);
        }

        Licencia licencia = new Licencia();
        licencia.setTitular(titular);
        licencia.setClase(clase);
        licencia.setFechaEmision(LocalDate.now());
        licencia.setFechaVencimiento(fechaVencimiento);
        licencia.setEstado(EstadoLicencia.VIGENTE);
        licencia.setNumeroLicencia(generarNumeroLicencia());
        licencia.setTramite(tramite);
        
        if (tramite.getTipo() == TipoTramite.DUPLICADO) {
            licenciaPreviaOpt.ifPresent(prev -> 
                licencia.setObservaciones("Duplicado de licencia N° " + prev.getNumeroLicencia())
            );
        } else if (tramite.getTipo() == TipoTramite.CAMBIO_DOMICILIO) {
            licencia.setObservaciones("Cambio de domicilio - Licencia anterior N° " + 
                licenciaPreviaOpt.map(Licencia::getNumeroLicencia).orElse("N/A"));
        }

        log.info("Emitiendo licencia clase {} (Trámite: {}) para titular: {} {}", 
                clase.name(), tramite.getTipo().name(), titular.getNombre(), titular.getApellido());
        
        return licenciaRepository.save(licencia);
    }

    /**
     * Renueva una licencia existente
     */
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_EMITIR + "')")
    public Licencia renovarLicencia(Licencia licenciaAnterior, Tramite tramite) {
        // Marcar la licencia anterior como duplicada
        licenciaAnterior.setEstado(EstadoLicencia.DUPLICADA);
        licenciaRepository.save(licenciaAnterior);

        // Crear nueva licencia
        return emitirLicencia(tramite);
    }

    /**
     * Duplica una licencia (por pérdida, robo o deterioro)
     */
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_EMITIR + "')")
    public Licencia duplicarLicencia(Licencia licenciaOriginal, Tramite tramite) {
        if (licenciaOriginal.getEstado() != EstadoLicencia.VIGENTE) {
            throw new IllegalStateException("Solo se pueden duplicar licencias vigentes");
        }

        // Marcar la licencia original como duplicada
        licenciaOriginal.setEstado(EstadoLicencia.DUPLICADA);
        licenciaRepository.save(licenciaOriginal);

        Titular titular = tramite.getTitular();
        
        Licencia duplicado = new Licencia();
        duplicado.setTitular(titular);
        duplicado.setClase(licenciaOriginal.getClase());
        duplicado.setFechaEmision(LocalDate.now());
        duplicado.setFechaVencimiento(licenciaOriginal.getFechaVencimiento()); // Mantiene vencimiento original
        duplicado.setEstado(EstadoLicencia.VIGENTE);
        duplicado.setNumeroLicencia(generarNumeroLicencia());
        duplicado.setTramite(tramite);
        duplicado.setObservaciones("Duplicado de licencia N° " + licenciaOriginal.getNumeroLicencia());

        log.info("Duplicando licencia para titular: {} {}", titular.getNombre(), titular.getApellido());
        
        return licenciaRepository.save(duplicado);
    }

    /**
     * Actualiza el domicilio en una licencia (requiere reimpresión)
     */
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_EMITIR + "')")
    public Licencia actualizarDomicilio(Licencia licencia, String nuevoDomicilio) {
        if (licencia.getEstado() != EstadoLicencia.VIGENTE) {
            throw new IllegalStateException("Solo se puede actualizar el domicilio de licencias vigentes");
        }

        // Marcar la licencia anterior como duplicada
        licencia.setEstado(EstadoLicencia.DUPLICADA);
        licenciaRepository.save(licencia);

        // Crear nueva licencia con domicilio actualizado
        Licencia nuevaLicencia = new Licencia();
        nuevaLicencia.setTitular(licencia.getTitular());
        nuevaLicencia.setClase(licencia.getClase());
        nuevaLicencia.setFechaEmision(LocalDate.now());
        nuevaLicencia.setFechaVencimiento(licencia.getFechaVencimiento()); // Mantiene vencimiento
        nuevaLicencia.setEstado(EstadoLicencia.VIGENTE);
        nuevaLicencia.setNumeroLicencia(generarNumeroLicencia());
        nuevaLicencia.setObservaciones("Cambio de domicilio - Licencia anterior N° " + licencia.getNumeroLicencia());

        // Actualizar domicilio del titular
        licencia.getTitular().setDomicilio(nuevoDomicilio);

        log.info("Actualizando domicilio en licencia para titular: {} {}", 
                licencia.getTitular().getNombre(), licencia.getTitular().getApellido());
        
        return licenciaRepository.save(nuevaLicencia);
    }

    /**
     * Suspende una licencia
     */
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_GESTIONAR_ESTADO + "')")
    public Licencia suspenderLicencia(Long licenciaId, String motivo) {
        Licencia licencia = licenciaRepository.findById(licenciaId)
                .orElseThrow(() -> new IllegalArgumentException("Licencia no encontrada con ID: " + licenciaId));

        if (licencia.getEstado() != EstadoLicencia.VIGENTE) {
            throw new IllegalStateException("Solo se pueden suspender licencias vigentes");
        }

        licencia.setEstado(EstadoLicencia.SUSPENDIDA);
        licencia.setObservaciones(motivo);

        log.info("Suspendiendo licencia N° {} - Motivo: {}", licencia.getNumeroLicencia(), motivo);
        
        return licenciaRepository.save(licencia);
    }

    /**
     * Inhabilita una licencia
     */
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_GESTIONAR_ESTADO + "')")
    public Licencia inhabilitarLicencia(Long licenciaId, String motivo) {
        Licencia licencia = licenciaRepository.findById(licenciaId)
                .orElseThrow(() -> new IllegalArgumentException("Licencia no encontrada con ID: " + licenciaId));

        licencia.setEstado(EstadoLicencia.INHABILITADA);
        licencia.setObservaciones(motivo);

        log.info("Inhabilitando licencia N° {} - Motivo: {}", licencia.getNumeroLicencia(), motivo);
        
        return licenciaRepository.save(licencia);
    }

    /**
     * Obtiene licencias próximas a vencer
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public List<Licencia> getLicenciasProximasAVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);
        return licenciaRepository.findLicenciasProximasAVencer(hoy, fechaLimite);
    }

    /**
     * Obtiene licencias vencidas
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public List<Licencia> getLicenciasVencidas() {
        return licenciaRepository.findLicenciasVencidas(LocalDate.now());
    }

    /**
     * Actualiza el estado de licencias vencidas
     */
    @PreAuthorize("hasAuthority('" + Authorities.PROCESO_VENCIMIENTOS_EJECUTAR + "')")
    public void actualizarLicenciasVencidas() {
        List<Licencia> licenciasVencidas = getLicenciasVencidas();
        for (Licencia licencia : licenciasVencidas) {
            licencia.setEstado(EstadoLicencia.VENCIDA);
            licenciaRepository.save(licencia);
        }
        log.info("Actualizadas {} licencias vencidas", licenciasVencidas.size());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.LICENCIA_VER + "')")
    public Long getCountLicenciasEmitidasEnPeriodo(LocalDate desde, LocalDate hasta) {
        return licenciaRepository.countLicenciasEmitidasEnPeriodo(desde, hasta);
    }

    private String generarNumeroLicencia() {
        // Formato: YYYYMMDD-XXXXXX donde XXXXXX es un número aleatorio
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int numeroAleatorio = new Random().nextInt(999999) + 1;
        String numero = fecha + "-" + String.format("%06d", numeroAleatorio);
        
        // Verificar que no exista
        while (licenciaRepository.existsByNumeroLicencia(numero)) {
            numeroAleatorio = new Random().nextInt(999999) + 1;
            numero = fecha + "-" + String.format("%06d", numeroAleatorio);
        }
        
        return numero;
    }

    /**
     * Cuenta las licencias emitidas actualmente vigentes
     */
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public long countLicenciasEmitidas() {
        LocalDate hoy = LocalDate.now();
        return licenciaRepository.countByFechaVencimientoAfter(hoy);
    }
}
