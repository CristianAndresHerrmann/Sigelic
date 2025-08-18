package com.example.sigelic.controller;

import com.example.sigelic.dto.response.LicenciaResponseDTO;
import com.example.sigelic.mapper.LicenciaMapper;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.Titular;
import com.example.sigelic.service.LicenciaService;
import com.example.sigelic.service.TitularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestión de licencias
 */
@RestController
@RequestMapping("/api/licencias")
@CrossOrigin(origins = "*")
public class LicenciaController {

    @Autowired
    private LicenciaService licenciaService;

    @Autowired
    private TitularService titularService;

    @Autowired
    private LicenciaMapper licenciaMapper;

    /**
     * Obtiene una licencia por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LicenciaResponseDTO> obtenerLicenciaPorId(@PathVariable Long id) {
        Optional<Licencia> licenciaOpt = licenciaService.findById(id);
        if (licenciaOpt.isPresent()) {
            LicenciaResponseDTO dto = licenciaMapper.toResponseDTOWithDetails(licenciaOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Busca licencias por número
     */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<LicenciaResponseDTO> obtenerLicenciaPorNumero(@PathVariable String numero) {
        Optional<Licencia> licenciaOpt = licenciaService.findByNumero(numero);
        if (licenciaOpt.isPresent()) {
            LicenciaResponseDTO dto = licenciaMapper.toResponseDTOWithDetails(licenciaOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene licencias de un titular
     */
    @GetMapping("/titular/{titularId}")
    public ResponseEntity<List<LicenciaResponseDTO>> obtenerLicenciasPorTitular(@PathVariable Long titularId) {
        Optional<Titular> titularOpt = titularService.findById(titularId);
        if (titularOpt.isPresent()) {
            List<Licencia> licencias = licenciaService.findByTitular(titularOpt.get());
            List<LicenciaResponseDTO> dtos = licenciaMapper.toResponseDTOList(licencias);
            return ResponseEntity.ok(dtos);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene licencias vigentes de un titular
     */
    @GetMapping("/titular/{titularId}/vigentes")
    public ResponseEntity<List<LicenciaResponseDTO>> obtenerLicenciasVigentesPorTitular(@PathVariable Long titularId) {
        Optional<Titular> titularOpt = titularService.findById(titularId);
        if (titularOpt.isPresent()) {
            List<Licencia> licencias = licenciaService.findLicenciasVigentesByTitular(titularOpt.get());
            List<LicenciaResponseDTO> dtos = licenciaMapper.toResponseDTOList(licencias);
            return ResponseEntity.ok(dtos);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene licencias próximas a vencer (30 días por defecto)
     */
    @GetMapping("/proximas-vencer")
    public ResponseEntity<List<LicenciaResponseDTO>> obtenerLicenciasProximasAVencer(
            @RequestParam(defaultValue = "30") int dias) {
        
        List<Licencia> licencias = licenciaService.getLicenciasProximasAVencer(dias);
        List<LicenciaResponseDTO> dtos = licenciaMapper.toResponseDTOList(licencias);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene licencias vencidas
     */
    @GetMapping("/vencidas")
    public ResponseEntity<List<LicenciaResponseDTO>> obtenerLicenciasVencidas() {
        List<Licencia> licencias = licenciaService.getLicenciasVencidas();
        List<LicenciaResponseDTO> dtos = licenciaMapper.toResponseDTOList(licencias);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Suspende una licencia
     */
    @PatchMapping("/{id}/suspender")
    public ResponseEntity<LicenciaResponseDTO> suspenderLicencia(
            @PathVariable Long id,
            @RequestParam String motivo) {
        
        try {
            Licencia licencia = licenciaService.suspenderLicencia(id, motivo);
            LicenciaResponseDTO dto = licenciaMapper.toResponseDTO(licencia);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Inhabilita una licencia
     */
    @PatchMapping("/{id}/inhabilitar")
    public ResponseEntity<LicenciaResponseDTO> inhabilitarLicencia(
            @PathVariable Long id,
            @RequestParam String motivo) {
        
        try {
            Licencia licencia = licenciaService.inhabilitarLicencia(id, motivo);
            LicenciaResponseDTO dto = licenciaMapper.toResponseDTO(licencia);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Actualiza el domicilio en una licencia
     */
    @PatchMapping("/{id}/actualizar-domicilio")
    public ResponseEntity<LicenciaResponseDTO> actualizarDomicilio(
            @PathVariable Long id,
            @RequestParam String nuevoDomicilio) {
        
        try {
            Optional<Licencia> licenciaOpt = licenciaService.findById(id);
            if (licenciaOpt.isPresent()) {
                Licencia licencia = licenciaService.actualizarDomicilio(licenciaOpt.get(), nuevoDomicilio);
                LicenciaResponseDTO dto = licenciaMapper.toResponseDTO(licencia);
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Ejecuta actualización masiva de licencias vencidas
     */
    @PostMapping("/actualizar-vencidas")
    public ResponseEntity<Void> actualizarLicenciasVencidas() {
        licenciaService.actualizarLicenciasVencidas();
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene conteo de licencias emitidas en un período
     */
    @GetMapping("/contador/emitidas")
    public ResponseEntity<Long> obtenerContadorLicenciasEmitidas(
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta) {
        
        Long contador = licenciaService.getCountLicenciasEmitidasEnPeriodo(desde, hasta);
        return ResponseEntity.ok(contador);
    }
}
