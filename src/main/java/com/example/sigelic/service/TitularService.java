package com.example.sigelic.service;

import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Inhabilitacion;
import com.example.sigelic.repository.TitularRepository;
import com.example.sigelic.repository.InhabilitacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar titulares de licencias
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TitularService {

    private final TitularRepository titularRepository;
    private final InhabilitacionRepository inhabilitacionRepository;

    /**
     * Busca un titular por ID
     */
    @Transactional(readOnly = true)
    public Optional<Titular> findById(Long id) {
        return titularRepository.findById(id);
    }

    /**
     * Busca un titular por DNI
     */
    @Transactional(readOnly = true)
    public Optional<Titular> findByDni(String dni) {
        return titularRepository.findByDni(dni);
    }

    /**
     * Busca titulares por nombre o apellido
     */
    @Transactional(readOnly = true)
    public List<Titular> findByNombre(String nombre) {
        return titularRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(nombre, nombre);
    }

    /**
     * Busca titulares por nombre completo
     */
    @Transactional(readOnly = true)
    public List<Titular> findByNombreCompleto(String nombreCompleto) {
        return titularRepository.findByNombreCompletoContainingIgnoreCase(nombreCompleto);
    }

    /**
     * Obtiene todos los titulares
     */
    @Transactional(readOnly = true)
    public List<Titular> findAll() {
        return titularRepository.findAll();
    }

    /**
     * Guarda un titular
     */
    public Titular save(Titular titular) {
        validateTitular(titular);
        log.info("Guardando titular: {} {}", titular.getNombre(), titular.getApellido());
        return titularRepository.save(titular);
    }

    /**
     * Actualiza un titular
     */
    public Titular update(Titular titular) {
        if (titular.getId() == null) {
            throw new IllegalArgumentException("El titular debe tener un ID para ser actualizado");
        }
        validateTitular(titular);
        log.info("Actualizando titular: {} {}", titular.getNombre(), titular.getApellido());
        return titularRepository.save(titular);
    }

    /**
     * Elimina un titular
     */
    public void delete(Long id) {
        Titular titular = titularRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + id));
        
        // Verificar que no tenga trámites activos
        if (titular.getTramites().stream().anyMatch(t -> !t.getEstado().name().endsWith("EMITIDA") && !t.getEstado().name().endsWith("RECHAZADA"))) {
            throw new IllegalStateException("No se puede eliminar un titular con trámites activos");
        }
        
        log.info("Eliminando titular: {} {}", titular.getNombre(), titular.getApellido());
        titularRepository.delete(titular);
    }

    /**
     * Verifica si un titular puede iniciar un trámite
     */
    @Transactional(readOnly = true)
    public boolean puedeIniciarTramite(Long titularId) {
        Titular titular = titularRepository.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));
        
        return !titular.tieneInhabilitacionesActivas();
    }

    /**
     * Obtiene las inhabilitaciones activas de un titular
     */
    @Transactional(readOnly = true)
    public List<Inhabilitacion> getInhabilitacionesActivas(Long titularId) {
        Titular titular = titularRepository.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));
        
        return inhabilitacionRepository.findInhabilitacionesActivasByTitular(titular);
    }

    /**
     * Agrega una inhabilitación a un titular
     */
    public Inhabilitacion agregarInhabilitacion(Long titularId, Inhabilitacion inhabilitacion) {
        Titular titular = titularRepository.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));
        
        inhabilitacion.setTitular(titular);
        log.info("Agregando inhabilitación al titular: {} {}", titular.getNombre(), titular.getApellido());
        return inhabilitacionRepository.save(inhabilitacion);
    }

    /**
     * Verifica si existe un titular con el DNI dado
     */
    @Transactional(readOnly = true)
    public boolean existsByDni(String dni) {
        return titularRepository.existsByDni(dni);
    }

    /**
     * Verifica si existe un titular con el email dado
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return titularRepository.existsByEmail(email);
    }

    /**
     * Obtiene titulares con inhabilitaciones activas
     */
    @Transactional(readOnly = true)
    public List<Titular> getTitularesConInhabilitacionesActivas() {
        return titularRepository.findTitularesConInhabilitacionesActivas();
    }

    private void validateTitular(Titular titular) {
        if (titular.getDni() == null || titular.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        
        if (titular.getNombre() == null || titular.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        if (titular.getApellido() == null || titular.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        
        if (titular.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        
        // Verificar DNI único
        if (titular.getId() == null) {
            if (existsByDni(titular.getDni())) {
                throw new IllegalArgumentException("Ya existe un titular con el DNI: " + titular.getDni());
            }
        } else {
            Optional<Titular> existente = findByDni(titular.getDni());
            if (existente.isPresent() && !existente.get().getId().equals(titular.getId())) {
                throw new IllegalArgumentException("Ya existe otro titular con el DNI: " + titular.getDni());
            }
        }
        
        // Verificar email único si se proporciona
        if (titular.getEmail() != null && !titular.getEmail().trim().isEmpty()) {
            if (titular.getId() == null) {
                if (existsByEmail(titular.getEmail())) {
                    throw new IllegalArgumentException("Ya existe un titular con el email: " + titular.getEmail());
                }
            } else {
                Optional<Titular> existente = titularRepository.findByEmail(titular.getEmail());
                if (existente.isPresent() && !existente.get().getId().equals(titular.getId())) {
                    throw new IllegalArgumentException("Ya existe otro titular con el email: " + titular.getEmail());
                }
            }
        }
    }
}
