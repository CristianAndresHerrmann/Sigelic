package com.example.sigelic.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.ExamenPracticoRepository;
import com.example.sigelic.repository.ExamenTeoricoRepository;
import com.example.sigelic.security.Authorities;

/**
 * Servicio para gestión de exámenes teóricos y prácticos
 */
@Service
@Transactional
public class ExamenService {

    @Autowired
    private ExamenTeoricoRepository examenTeoricoRepository;

    @Autowired
    private ExamenPracticoRepository examenPracticoRepository;

    // MÉTODOS PARA EXÁMENES TEÓRICOS

    /**
     * Obtiene todos los exámenes teóricos
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public List<ExamenTeorico> findAllTeoricos() {
        return examenTeoricoRepository.findAllWithTramite();
    }

    /**
     * Obtiene todos los exámenes prácticos
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public List<ExamenPractico> findAllPracticos() {
        return examenPracticoRepository.findAllWithTramite();
    }

    /**
     * Guarda un examen teórico
     */
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_TEO_REGISTRAR + "')")
    public ExamenTeorico saveExamenTeorico(ExamenTeorico examen) {
        return examenTeoricoRepository.save(examen);
    }

    /**
     * Guarda un examen práctico
     */
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_PRA_REGISTRAR + "')")
    public ExamenPractico saveExamenPractico(ExamenPractico examen) {
        return examenPracticoRepository.save(examen);
    }

    /**
     * Busca exámenes teóricos por trámite
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public List<ExamenTeorico> findTeoricosByTramite(Tramite tramite) {
        return examenTeoricoRepository.findByTramite(tramite);
    }

    /**
     * Busca exámenes prácticos por trámite
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public List<ExamenPractico> findPracticosByTramite(Tramite tramite) {
        return examenPracticoRepository.findByTramite(tramite);
    }

    /**
     * Busca exámenes teóricos por examinador
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public List<ExamenTeorico> findTeoricosByExaminador(String examinador) {
        return examenTeoricoRepository.findByExaminador(examinador);
    }

    /**
     * Busca exámenes teóricos en un período
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public List<ExamenTeorico> findTeoricosByPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.findExamenesEnPeriodo(desde, hasta);
    }

    /**
     * Obtiene el último examen teórico aprobado para un trámite
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public Optional<ExamenTeorico> findUltimoTeoricoAprobado(Tramite tramite) {
        return examenTeoricoRepository.findUltimoExamenAprobado(tramite);
    }

    /**
     * Calcula el promedio de puntajes en un período
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public Double calcularPromedioTeoricos(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.findPuntajePromedioEnPeriodo(desde, hasta);
    }

    /**
     * Cuenta exámenes teóricos aprobados en un período
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public Long contarTeoricosAprobados(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.countAprobadosEnPeriodo(desde, hasta);
    }

    /**
     * Cuenta todos los exámenes teóricos en un período
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public Long contarTeoricosTotal(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.countTotalEnPeriodo(desde, hasta);
    }

    /**
     * Elimina un examen teórico
     */
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_TEO_REGISTRAR + "')")
    public void deleteExamenTeorico(Long id) {
        examenTeoricoRepository.deleteById(id);
    }

    /**
     * Elimina un examen práctico
     */
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_PRA_REGISTRAR + "')")
    public void deleteExamenPractico(Long id) {
        examenPracticoRepository.deleteById(id);
    }

    /**
     * Busca un examen teórico por ID
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public Optional<ExamenTeorico> findExamenTeoricoById(Long id) {
        return examenTeoricoRepository.findById(id);
    }

    /**
     * Busca un examen práctico por ID
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.EXAMEN_VER + "')")
    public Optional<ExamenPractico> findExamenPracticoById(Long id) {
        return examenPracticoRepository.findById(id);
    }

    /**
     * Cuenta los exámenes pendientes (no aprobados)
     */
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public long countExamenesPendientes() {
        long teoricosPendientes = examenTeoricoRepository.countByAprobadoFalseOrNull();
        long practicosPendientes = examenPracticoRepository.countByAprobadoFalseOrNull();
        return teoricosPendientes + practicosPendientes;
    }
}
