package com.example.sigelic.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.Tramite;

/**
 * Repositorio para la entidad ExamenPractico
 */
@Repository
public interface ExamenPracticoRepository extends JpaRepository<ExamenPractico, Long> {
    
    @Query("SELECT e FROM ExamenPractico e LEFT JOIN FETCH e.tramite t LEFT JOIN FETCH t.titular ORDER BY e.fecha DESC")
    List<ExamenPractico> findAllWithTramite();
    
    List<ExamenPractico> findByTramite(Tramite tramite);
    
    Optional<ExamenPractico> findByTramiteAndAprobado(Tramite tramite, Boolean aprobado);
    
    @Query("SELECT e FROM ExamenPractico e WHERE e.tramite = :tramite AND e.aprobado = true ORDER BY e.fecha DESC")
    Optional<ExamenPractico> findUltimoExamenAprobado(@Param("tramite") Tramite tramite);
    
    @Query("SELECT e FROM ExamenPractico e WHERE e.fecha BETWEEN :desde AND :hasta")
    List<ExamenPractico> findExamenesEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(e) FROM ExamenPractico e WHERE e.aprobado = true AND e.fecha BETWEEN :desde AND :hasta")
    Long countAprobadosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(e) FROM ExamenPractico e WHERE e.fecha BETWEEN :desde AND :hasta")
    Long countTotalEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT AVG(e.faltasLeves) FROM ExamenPractico e WHERE e.fecha BETWEEN :desde AND :hasta")
    Double findPromedioFaltasLevesEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    List<ExamenPractico> findByExaminador(String examinador);
    
    List<ExamenPractico> findByPistaUtilizada(String pistaUtilizada);
    
    @Query("SELECT COUNT(e) FROM ExamenPractico e WHERE e.aprobado = false OR e.aprobado IS NULL")
    Long countByAprobadoFalseOrNull();
}
