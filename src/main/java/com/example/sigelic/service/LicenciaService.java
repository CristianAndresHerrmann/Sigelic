package com.example.sigelic.service;

import com.example.sigelic.model.*;
import com.example.sigelic.repository.LicenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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
    public Optional<Licencia> findById(Long id) {
        return licenciaRepository.findById(id);
    }

    /**
     * Busca una licencia por número
     */
    @Transactional(readOnly = true)
    public Optional<Licencia> findByNumero(String numeroLicencia) {
        return licenciaRepository.findByNumeroLicencia(numeroLicencia);
    }

    /**
     * Obtiene todas las licencias de un titular
     */
    @Transactional(readOnly = true)
    public List<Licencia> findByTitular(Titular titular) {
        return licenciaRepository.findByTitular(titular);
    }

    /**
     * Obtiene las licencias vigentes de un titular
     */
    @Transactional(readOnly = true)
    public List<Licencia> findLicenciasVigentesByTitular(Titular titular) {
        return licenciaRepository.findLicenciasVigentesByTitular(titular);
    }

    /**
     * Emite una nueva licencia basada en un trámite
     */
    public Licencia emitirLicencia(Tramite tramite) {
        if (!tramite.todosLosRequisitosCumplidos()) {
            throw new IllegalStateException("No se puede emitir licencia sin cumplir todos los requisitos");
        }

        Titular titular = tramite.getTitular();
        ClaseLicencia clase = tramite.getClaseSolicitada();
        
        // Para duplicados, mantener fecha de vencimiento original
        LocalDate fechaVencimiento;
        if (tramite.getTipo() == TipoTramite.DUPLICADO) {
            fechaVencimiento = calcularVencimientoParaDuplicado(titular, clase);
        } else {
            // Para emisión y renovación, calcular nueva vigencia
            boolean esPrimeraVez = tramite.getTipo() == TipoTramite.EMISION;
            int vigenciaAnios = Licencia.calcularVigenciaEnAnios(titular.getEdad(), esPrimeraVez);
            fechaVencimiento = Licencia.calcularFechaVencimiento(
                titular.getFechaNacimiento(), 
                LocalDate.now(), 
                vigenciaAnios
            );
        }

        Licencia licencia = new Licencia();
        licencia.setTitular(titular);
        licencia.setClase(clase);
        licencia.setFechaEmision(LocalDate.now());
        licencia.setFechaVencimiento(fechaVencimiento);
        licencia.setEstado(EstadoLicencia.VIGENTE);
        licencia.setNumeroLicencia(generarNumeroLicencia());
        licencia.setTramite(tramite);

        log.info("Emitiendo licencia clase {} para titular: {} {}", 
                clase.name(), titular.getNombre(), titular.getApellido());
        
        return licenciaRepository.save(licencia);
    }

    /**
     * Renueva una licencia existente
     */
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
    public List<Licencia> getLicenciasProximasAVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);
        return licenciaRepository.findLicenciasProximasAVencer(hoy, fechaLimite);
    }

    /**
     * Obtiene licencias vencidas
     */
    @Transactional(readOnly = true)
    public List<Licencia> getLicenciasVencidas() {
        return licenciaRepository.findLicenciasVencidas(LocalDate.now());
    }

    /**
     * Actualiza el estado de licencias vencidas
     */
    public void actualizarLicenciasVencidas() {
        List<Licencia> licenciasVencidas = getLicenciasVencidas();
        for (Licencia licencia : licenciasVencidas) {
            licencia.setEstado(EstadoLicencia.VENCIDA);
            licenciaRepository.save(licencia);
        }
        log.info("Actualizadas {} licencias vencidas", licenciasVencidas.size());
    }

    /**
     * Obtiene estadísticas de licencias emitidas en un período
     */
    @Transactional(readOnly = true)
    public Long getCountLicenciasEmitidasEnPeriodo(LocalDate desde, LocalDate hasta) {
        return licenciaRepository.countLicenciasEmitidasEnPeriodo(desde, hasta);
    }

    private LocalDate calcularVencimientoParaDuplicado(Titular titular, ClaseLicencia clase) {
        // Buscar la licencia vigente anterior para mantener el vencimiento
        Optional<Licencia> licenciaAnterior = licenciaRepository.findByTitularAndClaseAndEstado(
                titular, clase, EstadoLicencia.DUPLICADA);
        
        if (licenciaAnterior.isPresent()) {
            return licenciaAnterior.get().getFechaVencimiento();
        }
        
        // Si no hay licencia anterior, calcular nueva vigencia
        int vigenciaAnios = Licencia.calcularVigenciaEnAnios(titular.getEdad(), false);
        return Licencia.calcularFechaVencimiento(titular.getFechaNacimiento(), LocalDate.now(), vigenciaAnios);
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
}
