package com.example.sigelic.controller;

import com.example.sigelic.dto.request.TurnoRequestDTO;
import com.example.sigelic.dto.response.TurnoResponseDTO;
import com.example.sigelic.mapper.TurnoMapper;
import com.example.sigelic.model.Turno;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TurnoService;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.service.TramiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestión de turnos
 */
@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private TitularService titularService;

    @Autowired
    private TramiteService tramiteService;

    @Autowired
    private TurnoMapper turnoMapper;

    /**
     * Obtiene un turno por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> obtenerTurnoPorId(@PathVariable Long id) {
        Optional<Turno> turnoOpt = turnoService.findById(id);
        if (turnoOpt.isPresent()) {
            TurnoResponseDTO dto = turnoMapper.toResponseDTOWithDetails(turnoOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Crea un nuevo turno
     */
    @PostMapping
    public ResponseEntity<TurnoResponseDTO> crearTurno(@Valid @RequestBody TurnoRequestDTO turnoRequest) {
        // Obtener titular
        Optional<Titular> titularOpt = titularService.findById(turnoRequest.getTitularId());
        if (!titularOpt.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Obtener trámite si se especifica
        Tramite tramite = null;
        if (turnoRequest.getTramiteId() != null) {
            Optional<Tramite> tramiteOpt = tramiteService.findById(turnoRequest.getTramiteId());
            if (!tramiteOpt.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            tramite = tramiteOpt.get();
        }

        // Crear turno
        Turno turno = turnoService.reservarTurno(
            turnoRequest.getTitularId(),
            turnoRequest.getTipo(),
            turnoRequest.getInicio(),
            turnoRequest.getFin(),
            turnoRequest.getRecursoId(),
            turnoRequest.getTramiteId()
        );

        TurnoResponseDTO dto = turnoMapper.toResponseDTO(turno);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Actualiza un turno existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> actualizarTurno(
            @PathVariable Long id,
            @Valid @RequestBody TurnoRequestDTO turnoRequest) {
        
        Optional<Turno> turnoOpt = turnoService.findById(id);
        if (turnoOpt.isPresent()) {
            Turno turnoExistente = turnoOpt.get();
            turnoMapper.updateEntity(turnoExistente, turnoRequest);
            // No hay método save directo, usar el repositorio directamente o implementar
            TurnoResponseDTO dto = turnoMapper.toResponseDTO(turnoExistente);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Confirma la asistencia a un turno
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<TurnoResponseDTO> confirmarTurno(@PathVariable Long id) {
        try {
            Turno turno = turnoService.confirmarTurno(id);
            TurnoResponseDTO dto = turnoMapper.toResponseDTO(turno);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Cancela un turno
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<TurnoResponseDTO> cancelarTurno(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        
        try {
            Turno turno = turnoService.cancelarTurno(id, motivo);
            TurnoResponseDTO dto = turnoMapper.toResponseDTO(turno);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Marca un turno como ausente
     */
    @PatchMapping("/{id}/ausente")
    public ResponseEntity<TurnoResponseDTO> marcarAusente(@PathVariable Long id) {
        try {
            Turno turno = turnoService.marcarAusente(id);
            TurnoResponseDTO dto = turnoMapper.toResponseDTO(turno);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtiene turnos de un titular
     */
    @GetMapping("/titular/{titularId}")
    public ResponseEntity<List<TurnoResponseDTO>> obtenerTurnosPorTitular(@PathVariable Long titularId) {
        List<Turno> turnos = turnoService.findByTitular(titularId);
        List<TurnoResponseDTO> dtos = turnoMapper.toResponseDTOList(turnos);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene turnos por fecha
     */
    @GetMapping("/fecha")
    public ResponseEntity<List<TurnoResponseDTO>> obtenerTurnosPorFecha(
            @RequestParam LocalDateTime fechaDesde,
            @RequestParam LocalDateTime fechaHasta) {
        
        List<Turno> turnos = turnoService.findTurnosEnPeriodo(fechaDesde, fechaHasta);
        List<TurnoResponseDTO> dtos = turnoMapper.toResponseDTOList(turnos);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene turnos disponibles para una fecha específica
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<LocalDateTime>> obtenerTurnosDisponibles(
            @RequestParam String tipoRecurso,
            @RequestParam LocalDateTime fechaDesde,
            @RequestParam LocalDateTime fechaHasta,
            @RequestParam(defaultValue = "30") int duracionMinutos) {
        
        List<LocalDateTime> horariosDisponibles = turnoService.getHorariosDisponibles(
            com.example.sigelic.model.TipoRecurso.valueOf(tipoRecurso), 
            fechaDesde, 
            fechaHasta, 
            duracionMinutos
        );
        return ResponseEntity.ok(horariosDisponibles);
    }

    /**
     * Obtiene los próximos turnos de un titular
     */
    @GetMapping("/titular/{titularId}/proximos")
    public ResponseEntity<List<TurnoResponseDTO>> obtenerProximosTurnos(@PathVariable Long titularId) {
        List<Turno> turnos = turnoService.getProximosTurnos(titularId);
        List<TurnoResponseDTO> dtos = turnoMapper.toResponseDTOList(turnos);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Elimina un turno
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTurno(@PathVariable Long id) {
        Optional<Turno> turnoOpt = turnoService.findById(id);
        if (turnoOpt.isPresent()) {
            // Cancelar el turno en lugar de eliminarlo físicamente
            turnoService.cancelarTurno(id, "Eliminado por el usuario");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
