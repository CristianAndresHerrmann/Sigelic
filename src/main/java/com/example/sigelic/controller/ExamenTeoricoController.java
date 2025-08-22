package com.example.sigelic.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigelic.dto.request.ExamenTeoricoRequestDTO;
import com.example.sigelic.dto.response.ExamenTeoricoResponseDTO;
import com.example.sigelic.dto.response.TramiteResponseDTO;
import com.example.sigelic.mapper.ExamenTeoricoMapper;
import com.example.sigelic.mapper.TramiteMapper;
import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.ExamenService;
import com.example.sigelic.service.TramiteService;

import jakarta.validation.Valid;

/**
 * Controlador REST para gestión de exámenes teóricos
 */
@RestController
@RequestMapping("/api/examenes-teoricos")
@CrossOrigin(origins = "*")
public class ExamenTeoricoController {

    @Autowired
    private TramiteService tramiteService;
    
    @Autowired
    private ExamenService examenService;

    @Autowired
    private ExamenTeoricoMapper examenTeoricoMapper;
    
    @Autowired
    private TramiteMapper tramiteMapper;

    /**
     * Registra un examen teórico para un trámite
     */
    @PostMapping("/tramite/{tramiteId}")
    public ResponseEntity<TramiteResponseDTO> registrarExamenTeorico(
            @PathVariable Long tramiteId,
            @Valid @RequestBody ExamenTeoricoRequestDTO request) {
        
        try {
            ExamenTeorico examen = examenTeoricoMapper.toEntity(request);
            Tramite tramite = tramiteService.registrarExamenTeorico(tramiteId, examen);
            TramiteResponseDTO dto = tramiteMapper.toResponseDTO(tramite);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Rechaza el examen teórico de un trámite
     */
    @PatchMapping("/tramite/{tramiteId}/rechazar")
    public ResponseEntity<TramiteResponseDTO> rechazarExamenTeorico(
            @PathVariable Long tramiteId,
            @RequestParam String motivo) {
        
        try {
            Tramite tramite = tramiteService.rechazarExamenTeorico(tramiteId, motivo);
            TramiteResponseDTO dto = tramiteMapper.toResponseDTO(tramite);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene todos los exámenes teóricos
     */
    @GetMapping
    public ResponseEntity<List<ExamenTeoricoResponseDTO>> obtenerTodosLosExamenes() {
        List<ExamenTeorico> examenes = examenService.findAllTeoricos();
        List<ExamenTeoricoResponseDTO> dtos = examenTeoricoMapper.toResponseDTOList(examenes);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene un examen teórico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExamenTeoricoResponseDTO> obtenerExamenPorId(@PathVariable Long id) {
        Optional<ExamenTeorico> examenOpt = examenService.findExamenTeoricoById(id);
        if (examenOpt.isPresent()) {
            ExamenTeoricoResponseDTO dto = examenTeoricoMapper.toResponseDTO(examenOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene exámenes teóricos por trámite
     */
    @GetMapping("/tramite/{tramiteId}")
    public ResponseEntity<List<ExamenTeoricoResponseDTO>> obtenerExamenesPorTramite(@PathVariable Long tramiteId) {
        try {
            Optional<Tramite> tramiteOpt = tramiteService.findById(tramiteId);
            if (tramiteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<ExamenTeorico> examenes = examenService.findTeoricosByTramite(tramiteOpt.get());
            List<ExamenTeoricoResponseDTO> dtos = examenTeoricoMapper.toResponseDTOList(examenes);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene exámenes teóricos por examinador
     */
    @GetMapping("/examinador/{examinador}")
    public ResponseEntity<List<ExamenTeoricoResponseDTO>> obtenerExamenesPorExaminador(@PathVariable String examinador) {
        List<ExamenTeorico> examenes = examenService.findTeoricosByExaminador(examinador);
        List<ExamenTeoricoResponseDTO> dtos = examenTeoricoMapper.toResponseDTOList(examenes);
        return ResponseEntity.ok(dtos);
    }
}
