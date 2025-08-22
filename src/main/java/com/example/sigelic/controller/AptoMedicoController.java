package com.example.sigelic.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigelic.dto.AptoMedicoRequestDTO;
import com.example.sigelic.dto.response.AptoMedicoResponseDTO;
import com.example.sigelic.service.TramiteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para gestión de aptos médicos
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Apto Médico", description = "Gestión de exámenes médicos para trámites")
public class AptoMedicoController {

    private final TramiteService tramiteService;

    @PostMapping("/tramites/{tramiteId}/apto-medico")
    @PreAuthorize("hasAnyAuthority('TRAMITES_ESCRIBIR', 'MEDICO')")
    @Operation(summary = "Registrar apto médico", 
               description = "Registra el resultado del examen médico para un trámite")
    @ApiResponse(responseCode = "200", description = "Apto médico registrado exitosamente")
    @ApiResponse(responseCode = "404", description = "Trámite no encontrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o trámite en estado incorrecto")
    public ResponseEntity<AptoMedicoResponseDTO> registrarAptoMedico(
            @Parameter(description = "ID del trámite") @PathVariable Long tramiteId,
            @Valid @RequestBody AptoMedicoRequestDTO request) {
        
        AptoMedicoResponseDTO response = tramiteService.registrarAptoMedico(tramiteId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tramites/{tramiteId}/apto-medico")
    @PreAuthorize("hasAnyAuthority('TRAMITES_LEER', 'MEDICO')")
    @Operation(summary = "Obtener apto médico del trámite", 
               description = "Recupera la información del apto médico de un trámite")
    @ApiResponse(responseCode = "200", description = "Apto médico encontrado")
    @ApiResponse(responseCode = "404", description = "Trámite o apto médico no encontrado")
    public ResponseEntity<AptoMedicoResponseDTO> obtenerAptoMedico(
            @Parameter(description = "ID del trámite") @PathVariable Long tramiteId) {
        
        return tramiteService.obtenerAptoMedico(tramiteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aptos-medicos/proximos-vencer")
    @PreAuthorize("hasAnyAuthority('TRAMITES_LEER', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(summary = "Listar aptos médicos próximos a vencer", 
               description = "Obtiene lista de aptos médicos que vencen pronto para alertas")
    @ApiResponse(responseCode = "200", description = "Lista de aptos próximos a vencer")
    public ResponseEntity<List<AptoMedicoResponseDTO>> obtenerAptosProximosAVencer() {
        
        List<AptoMedicoResponseDTO> aptosProximos = tramiteService.obtenerAptosProximosAVencer();
        return ResponseEntity.ok(aptosProximos);
    }

    @GetMapping("/aptos-medicos/estadisticas")
    @PreAuthorize("hasAnyAuthority('REPORTES_LEER', 'ADMINISTRADOR')")
    @Operation(summary = "Estadísticas de aptos médicos", 
               description = "Obtiene estadísticas generales de exámenes médicos")
    @ApiResponse(responseCode = "200", description = "Estadísticas de aptos médicos")
    public ResponseEntity<Object> obtenerEstadisticasAptosMedicos() {
        
        // TODO: Implementar estadísticas de aptos médicos
        return ResponseEntity.ok("Estadísticas de aptos médicos - Por implementar");
    }
}
