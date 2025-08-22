package com.example.sigelic.controller;

import java.time.LocalDateTime;
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

import com.example.sigelic.dto.request.TramiteRequestDTO;
import com.example.sigelic.dto.response.TramiteResponseDTO;
import com.example.sigelic.mapper.TramiteMapper;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;

import jakarta.validation.Valid;

/**
 * Controlador REST para gestión de trámites
 */
@RestController
@RequestMapping("/api/tramites")
@CrossOrigin(origins = "*")
public class TramiteController {

    @Autowired
    private TramiteService tramiteService;

    @Autowired
    private TramiteMapper tramiteMapper;

    /**
     * Obtiene un trámite por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TramiteResponseDTO> obtenerTramitePorId(@PathVariable Long id) {
        Optional<Tramite> tramiteOpt = tramiteService.findById(id);
        if (tramiteOpt.isPresent()) {
            TramiteResponseDTO dto = tramiteMapper.toResponseDTOWithDetails(tramiteOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Inicia un nuevo trámite
     */
    @PostMapping
    public ResponseEntity<TramiteResponseDTO> iniciarTramite(@Valid @RequestBody TramiteRequestDTO tramiteRequest) {
        Tramite tramite = tramiteService.iniciarTramite(
            tramiteRequest.getTitularId(),
            tramiteRequest.getTipo(),
            tramiteRequest.getClaseSolicitada()
        );
        
        TramiteResponseDTO dto = tramiteMapper.toResponseDTO(tramite);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Valida la documentación de un trámite
     */
    @PatchMapping("/{id}/validar-documentacion")
    public ResponseEntity<TramiteResponseDTO> validarDocumentacion(
            @PathVariable Long id,
            @RequestParam String agente) {
        
        Tramite tramite = tramiteService.validarDocumentacion(id, agente);
        TramiteResponseDTO dto = tramiteMapper.toResponseDTO(tramite);
        return ResponseEntity.ok(dto);
    }

    /**
     * Rechaza un trámite
     */
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<TramiteResponseDTO> rechazarTramite(
            @PathVariable Long id,
            @RequestParam String motivo) {
        
        Tramite tramite = tramiteService.rechazarTramite(id, motivo);
        TramiteResponseDTO dto = tramiteMapper.toResponseDTO(tramite);
        return ResponseEntity.ok(dto);
    }

    /**
     * Rechaza el examen teórico de un trámite
     */
    @PatchMapping("/{id}/rechazar-examen-teorico")
    public ResponseEntity<TramiteResponseDTO> rechazarExamenTeorico(
            @PathVariable Long id,
            @RequestParam String motivo) {
        
        Tramite tramite = tramiteService.rechazarExamenTeorico(id, motivo);
        TramiteResponseDTO dto = tramiteMapper.toResponseDTO(tramite);
        return ResponseEntity.ok(dto);
    }

    /**
     * Permite el reintento para un trámite con examen teórico rechazado
     */
    @PatchMapping("/{id}/permitir-reintento")
    public ResponseEntity<TramiteResponseDTO> permitirReintento(
            @PathVariable Long id,
            @RequestParam String motivo) {
        
        Tramite tramite = tramiteService.permitirReintento(id, motivo);
        TramiteResponseDTO dto = tramiteMapper.toResponseDTO(tramite);
        return ResponseEntity.ok(dto);
    }

    /**
     * Emite una licencia a partir de un trámite
     */
    @PostMapping("/{id}/emitir-licencia")
    public ResponseEntity<?> emitirLicencia(@PathVariable Long id) {
        try {
            tramiteService.emitirLicencia(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtiene trámites por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<TramiteResponseDTO>> obtenerTramitesPorEstado(@PathVariable EstadoTramite estado) {
        List<Tramite> tramites = tramiteService.findByEstado(estado);
        List<TramiteResponseDTO> dtos = tramiteMapper.toResponseDTOList(tramites);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene trámites de un titular
     */
    @GetMapping("/titular/{titularId}")
    public ResponseEntity<List<TramiteResponseDTO>> obtenerTramitesPorTitular(@PathVariable Long titularId) {
        List<Tramite> tramites = tramiteService.findByTitular(titularId);
        List<TramiteResponseDTO> dtos = tramiteMapper.toResponseDTOList(tramites);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene el trámite activo de un titular
     */
    @GetMapping("/titular/{titularId}/activo")
    public ResponseEntity<TramiteResponseDTO> obtenerTramiteActivoPorTitular(@PathVariable Long titularId) {
        Optional<Tramite> tramiteOpt = tramiteService.getTramiteActivo(titularId);
        if (tramiteOpt.isPresent()) {
            TramiteResponseDTO dto = tramiteMapper.toResponseDTOWithDetails(tramiteOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene conteo de trámites por estado
     */
    @GetMapping("/contador/estado/{estado}")
    public ResponseEntity<Long> obtenerContadorPorEstado(@PathVariable EstadoTramite estado) {
        Long contador = tramiteService.getCountByEstado(estado);
        return ResponseEntity.ok(contador);
    }

    /**
     * Obtiene conteo de trámites por tipo en un período
     */
    @GetMapping("/contador/tipo/{tipo}")
    public ResponseEntity<Long> obtenerContadorPorTipoEnPeriodo(
            @PathVariable TipoTramite tipo,
            @RequestParam LocalDateTime desde,
            @RequestParam LocalDateTime hasta) {
        
        Long contador = tramiteService.getCountByTipoEnPeriodo(tipo, desde, hasta);
        return ResponseEntity.ok(contador);
    }
}
