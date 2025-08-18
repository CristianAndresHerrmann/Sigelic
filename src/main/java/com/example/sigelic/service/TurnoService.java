package com.example.sigelic.service;

import com.example.sigelic.model.*;
import com.example.sigelic.repository.TurnoRepository;
import com.example.sigelic.repository.RecursoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar turnos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final RecursoRepository recursoRepository;
    private final TitularService titularService;

    /**
     * Busca un turno por ID
     */
    @Transactional(readOnly = true)
    public Optional<Turno> findById(Long id) {
        return turnoRepository.findById(id);
    }

    /**
     * Obtiene todos los turnos de un titular
     */
    @Transactional(readOnly = true)
    public List<Turno> findByTitular(Long titularId) {
        Titular titular = titularService.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));
        return turnoRepository.findByTitular(titular);
    }

    /**
     * Obtiene turnos por recurso
     */
    @Transactional(readOnly = true)
    public List<Turno> findByRecurso(Long recursoId) {
        Recurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado con ID: " + recursoId));
        return turnoRepository.findByRecurso(recurso);
    }

    /**
     * Obtiene turnos en un período
     */
    @Transactional(readOnly = true)
    public List<Turno> findTurnosEnPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        return turnoRepository.findTurnosEnPeriodo(desde, hasta);
    }

    /**
     * Reserva un nuevo turno
     */
    public Turno reservarTurno(Long titularId, TipoTurno tipo, LocalDateTime inicio, LocalDateTime fin, Long recursoId, Long tramiteId) {
        Titular titular = titularService.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));

        Recurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado con ID: " + recursoId));

        // Validar que el recurso esté activo
        if (!recurso.getActivo()) {
            throw new IllegalStateException("El recurso no está disponible");
        }

        // Validar que no existan turnos solapados para el mismo titular y tipo
        List<Turno> turnosSolapados = turnoRepository.findTurnosSolapadosDelTitular(titular, tipo, inicio, fin);
        if (!turnosSolapados.isEmpty()) {
            throw new IllegalStateException("El titular ya tiene un turno del mismo tipo en ese horario");
        }

        // Validar que el recurso esté disponible en ese horario
        List<Turno> turnosConflictivos = turnoRepository.findTurnosConflictivos(recurso, inicio, fin);
        if (!turnosConflictivos.isEmpty()) {
            throw new IllegalStateException("El recurso no está disponible en ese horario");
        }

        // Validar horario del recurso
        if (!recurso.isDisponibleEn(inicio.toLocalTime()) || !recurso.isDisponibleEn(fin.toLocalTime())) {
            throw new IllegalStateException("El horario solicitado está fuera del horario de funcionamiento del recurso");
        }

        Turno turno = new Turno();
        turno.setTitular(titular);
        turno.setTipo(tipo);
        turno.setInicio(inicio);
        turno.setFin(fin);
        turno.setRecurso(recurso);
        turno.setTipoRecurso(recurso.getTipo());
        turno.setEstado(EstadoTurno.RESERVADO);

        if (tramiteId != null) {
            // Aquí podrías agregar la lógica para asociar con el trámite
            // turno.setTramite(tramiteService.findById(tramiteId).orElse(null));
        }

        log.info("Reservando turno de {} para titular: {} {} - Horario: {} a {}", 
                tipo.name(), titular.getNombre(), titular.getApellido(), inicio, fin);
        
        return turnoRepository.save(turno);
    }

    /**
     * Confirma un turno reservado
     */
    public Turno confirmarTurno(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado con ID: " + turnoId));

        if (turno.getEstado() != EstadoTurno.RESERVADO) {
            throw new IllegalStateException("Solo se pueden confirmar turnos en estado RESERVADO");
        }

        if (turno.isVencido()) {
            throw new IllegalStateException("No se puede confirmar un turno vencido");
        }

        turno.confirmar();
        log.info("Confirmando turno ID: {}", turnoId);
        
        return turnoRepository.save(turno);
    }

    /**
     * Completa un turno
     */
    public Turno completarTurno(Long turnoId, String observaciones) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado con ID: " + turnoId));

        if (turno.getEstado() != EstadoTurno.CONFIRMADO && turno.getEstado() != EstadoTurno.RESERVADO) {
            throw new IllegalStateException("Solo se pueden completar turnos confirmados o reservados");
        }

        turno.completar();
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            turno.setObservaciones(observaciones);
        }

        log.info("Completando turno ID: {}", turnoId);
        
        return turnoRepository.save(turno);
    }

    /**
     * Cancela un turno
     */
    public Turno cancelarTurno(Long turnoId, String motivo) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado con ID: " + turnoId));

        if (turno.getEstado() == EstadoTurno.COMPLETADO) {
            throw new IllegalStateException("No se puede cancelar un turno completado");
        }

        turno.cancelar(motivo);
        log.info("Cancelando turno ID: {} - Motivo: {}", turnoId, motivo);
        
        return turnoRepository.save(turno);
    }

    /**
     * Marca un turno como ausente
     */
    public Turno marcarAusente(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado con ID: " + turnoId));

        if (turno.getEstado() != EstadoTurno.CONFIRMADO && turno.getEstado() != EstadoTurno.RESERVADO) {
            throw new IllegalStateException("Solo se pueden marcar como ausente turnos confirmados o reservados");
        }

        turno.marcarAusente();
        log.info("Marcando como ausente turno ID: {}", turnoId);
        
        return turnoRepository.save(turno);
    }

    /**
     * Asigna un profesional a un turno
     */
    public Turno asignarProfesional(Long turnoId, String profesional) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado con ID: " + turnoId));

        turno.setProfesionalAsignado(profesional);
        log.info("Asignando profesional {} al turno ID: {}", profesional, turnoId);
        
        return turnoRepository.save(turno);
    }

    /**
     * Obtiene turnos disponibles para un tipo de recurso en un período
     */
    @Transactional(readOnly = true)
    public List<LocalDateTime> getHorariosDisponibles(TipoRecurso tipoRecurso, LocalDateTime desde, LocalDateTime hasta, int duracionMinutos) {
        List<Recurso> recursos = recursoRepository.findRecursosActivosPorTipo(tipoRecurso);
        
        // Esta es una implementación simplificada
        // En una implementación real, calcularías los slots disponibles considerando:
        // - Horarios de funcionamiento de cada recurso
        // - Turnos ya reservados
        // - Capacidad de cada recurso
        // - Duración requerida
        
        return List.of(); // Placeholder - implementar lógica completa
    }

    /**
     * Obtiene turnos de un profesional en un período
     */
    @Transactional(readOnly = true)
    public List<Turno> getTurnosByProfesionalEnPeriodo(String profesional, LocalDateTime desde, LocalDateTime hasta) {
        return turnoRepository.findTurnosByProfesionalEnPeriodo(profesional, desde, hasta);
    }

    /**
     * Obtiene estadísticas de turnos por estado en un período
     */
    @Transactional(readOnly = true)
    public Long getCountByEstadoEnPeriodo(EstadoTurno estado, LocalDateTime desde, LocalDateTime hasta) {
        return turnoRepository.countByEstadoEnPeriodo(estado, desde, hasta);
    }

    /**
     * Verifica si un titular puede reservar un turno
     */
    @Transactional(readOnly = true)
    public boolean puedeReservarTurno(Long titularId, TipoTurno tipo) {
        // Verificar que el titular pueda iniciar trámites (sin inhabilitaciones)
        return titularService.puedeIniciarTramite(titularId);
    }

    /**
     * Obtiene los próximos turnos de un titular
     */
    @Transactional(readOnly = true)
    public List<Turno> getProximosTurnos(Long titularId) {
        Titular titular = titularService.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));
        
        return turnoRepository.findByTitularAndEstado(titular, EstadoTurno.CONFIRMADO);
    }
}
