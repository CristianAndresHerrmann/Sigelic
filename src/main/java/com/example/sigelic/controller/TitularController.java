package com.example.sigelic.controller;

import com.example.sigelic.dto.request.TitularRequestDTO;
import com.example.sigelic.dto.response.TitularResponseDTO;
import com.example.sigelic.mapper.TitularMapper;
import com.example.sigelic.model.Titular;
import com.example.sigelic.service.TitularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestión de titulares
 */
@RestController
@RequestMapping("/api/titulares")
@CrossOrigin(origins = "*")
public class TitularController {

    @Autowired
    private TitularService titularService;

    @Autowired
    private TitularMapper titularMapper;

    /**
     * Obtiene todos los titulares
     */
    @GetMapping
    public ResponseEntity<List<TitularResponseDTO>> obtenerTitulares() {
        List<Titular> titulares = titularService.findAll();
        List<TitularResponseDTO> dtos = titularMapper.toResponseDTOList(titulares);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Busca titulares por nombre
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<TitularResponseDTO>> buscarTitularesPorNombre(@RequestParam String nombre) {
        List<Titular> titulares = titularService.findByNombre(nombre);
        List<TitularResponseDTO> dtos = titularMapper.toResponseDTOList(titulares);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Busca titulares por nombre completo
     */
    @GetMapping("/buscar/nombre-completo")
    public ResponseEntity<List<TitularResponseDTO>> buscarTitularesPorNombreCompleto(@RequestParam String nombreCompleto) {
        List<Titular> titulares = titularService.findByNombreCompleto(nombreCompleto);
        List<TitularResponseDTO> dtos = titularMapper.toResponseDTOList(titulares);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene un titular por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TitularResponseDTO> obtenerTitularPorId(@PathVariable Long id) {
        Optional<Titular> titularOpt = titularService.findById(id);
        if (titularOpt.isPresent()) {
            TitularResponseDTO dto = titularMapper.toResponseDTOWithDetails(titularOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Busca un titular por DNI
     */
    @GetMapping("/dni/{dni}")
    public ResponseEntity<TitularResponseDTO> obtenerTitularPorDni(@PathVariable String dni) {
        Optional<Titular> titularOpt = titularService.findByDni(dni);
        if (titularOpt.isPresent()) {
            TitularResponseDTO dto = titularMapper.toResponseDTOWithDetails(titularOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Crea un nuevo titular
     */
    @PostMapping
    public ResponseEntity<TitularResponseDTO> crearTitular(@Valid @RequestBody TitularRequestDTO titularRequest) {
        Titular titular = titularMapper.toEntity(titularRequest);
        Titular titularGuardado = titularService.save(titular);
        TitularResponseDTO dto = titularMapper.toResponseDTO(titularGuardado);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Actualiza un titular existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<TitularResponseDTO> actualizarTitular(
            @PathVariable Long id,
            @Valid @RequestBody TitularRequestDTO titularRequest) {
        
        Optional<Titular> titularOpt = titularService.findById(id);
        if (titularOpt.isPresent()) {
            Titular titularExistente = titularOpt.get();
            titularMapper.updateEntity(titularExistente, titularRequest);
            Titular titularActualizado = titularService.update(titularExistente);
            TitularResponseDTO dto = titularMapper.toResponseDTO(titularActualizado);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Elimina un titular
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTitular(@PathVariable Long id) {
        Optional<Titular> titularOpt = titularService.findById(id);
        if (titularOpt.isPresent()) {
            titularService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene titulares con inhabilitaciones activas
     */
    @GetMapping("/inhabilitados")
    public ResponseEntity<List<TitularResponseDTO>> obtenerTitularesInhabilitados() {
        List<Titular> titulares = titularService.getTitularesConInhabilitacionesActivas();
        List<TitularResponseDTO> dtos = titularMapper.toResponseDTOList(titulares);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Verifica si un titular puede iniciar un trámite
     */
    @GetMapping("/{id}/puede-iniciar-tramite")
    public ResponseEntity<Boolean> puedeIniciarTramite(@PathVariable Long id) {
        boolean puede = titularService.puedeIniciarTramite(id);
        return ResponseEntity.ok(puede);
    }

    /**
     * Verifica si existe un titular con el DNI especificado
     */
    @GetMapping("/existe/dni/{dni}")
    public ResponseEntity<Boolean> existePorDni(@PathVariable String dni) {
        boolean existe = titularService.existsByDni(dni);
        return ResponseEntity.ok(existe);
    }

    /**
     * Verifica si existe un titular con el email especificado
     */
    @GetMapping("/existe/email/{email}")
    public ResponseEntity<Boolean> existePorEmail(@PathVariable String email) {
        boolean existe = titularService.existsByEmail(email);
        return ResponseEntity.ok(existe);
    }
}
