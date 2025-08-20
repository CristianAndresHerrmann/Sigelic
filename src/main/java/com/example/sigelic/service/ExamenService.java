package com.example.sigelic.service;

import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.ExamenTeoricoRepository;
import com.example.sigelic.repository.ExamenPracticoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public List<ExamenTeorico> findAllTeoricos() {
        return examenTeoricoRepository.findAll();
    }

    /**
     * Obtiene todos los exámenes prácticos
     */
    @Transactional(readOnly = true)
    public List<ExamenPractico> findAllPracticos() {
        return examenPracticoRepository.findAll();
    }

    /**
     * Guarda un examen teórico
     */
    public ExamenTeorico saveExamenTeorico(ExamenTeorico examen) {
        return examenTeoricoRepository.save(examen);
    }

    /**
     * Guarda un examen práctico
     */
    public ExamenPractico saveExamenPractico(ExamenPractico examen) {
        return examenPracticoRepository.save(examen);
    }

    /**
     * Busca exámenes teóricos por trámite
     */
    @Transactional(readOnly = true)
    public List<ExamenTeorico> findTeoricosByTramite(Tramite tramite) {
        return examenTeoricoRepository.findByTramite(tramite);
    }

    /**
     * Busca exámenes prácticos por trámite
     */
    @Transactional(readOnly = true)
    public List<ExamenPractico> findPracticosByTramite(Tramite tramite) {
        return examenPracticoRepository.findByTramite(tramite);
    }

    /**
     * Busca exámenes teóricos por examinador
     */
    @Transactional(readOnly = true)
    public List<ExamenTeorico> findTeoricosByExaminador(String examinador) {
        return examenTeoricoRepository.findByExaminador(examinador);
    }

    /**
     * Busca exámenes teóricos en un período
     */
    @Transactional(readOnly = true)
    public List<ExamenTeorico> findTeoricosByPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.findExamenesEnPeriodo(desde, hasta);
    }

    /**
     * Obtiene el último examen teórico aprobado para un trámite
     */
    @Transactional(readOnly = true)
    public Optional<ExamenTeorico> findUltimoTeoricoAprobado(Tramite tramite) {
        return examenTeoricoRepository.findUltimoExamenAprobado(tramite);
    }

    /**
     * Calcula el promedio de puntajes en un período
     */
    @Transactional(readOnly = true)
    public Double calcularPromedioTeoricos(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.findPuntajePromedioEnPeriodo(desde, hasta);
    }

    /**
     * Cuenta exámenes teóricos aprobados en un período
     */
    @Transactional(readOnly = true)
    public Long contarTeoricosAprobados(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.countAprobadosEnPeriodo(desde, hasta);
    }

    /**
     * Cuenta todos los exámenes teóricos en un período
     */
    @Transactional(readOnly = true)
    public Long contarTeoricosTotal(LocalDateTime desde, LocalDateTime hasta) {
        return examenTeoricoRepository.countTotalEnPeriodo(desde, hasta);
    }

    /**
     * Elimina un examen teórico
     */
    public void deleteExamenTeorico(Long id) {
        examenTeoricoRepository.deleteById(id);
    }

    /**
     * Elimina un examen práctico
     */
    public void deleteExamenPractico(Long id) {
        examenPracticoRepository.deleteById(id);
    }

    /**
     * Busca un examen teórico por ID
     */
    @Transactional(readOnly = true)
    public Optional<ExamenTeorico> findExamenTeoricoById(Long id) {
        return examenTeoricoRepository.findById(id);
    }

    /**
     * Busca un examen práctico por ID
     */
    @Transactional(readOnly = true)
    public Optional<ExamenPractico> findExamenPracticoById(Long id) {
        return examenPracticoRepository.findById(id);
    }

    /**
     * Cuenta los exámenes pendientes (no aprobados)
     */
    @Transactional(readOnly = true)
    public long countExamenesPendientes() {
        long teoricosPendientes = examenTeoricoRepository.countByAprobadoFalseOrNull();
        long practicosPendientes = examenPracticoRepository.countByAprobadoFalseOrNull();
        return teoricosPendientes + practicosPendientes;
    }
}
