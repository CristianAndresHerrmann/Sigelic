package com.example.sigelic.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.Tramite;

/**
 * Repositorio para la entidad ExamenTeorico
 */
@Repository
public interface ExamenTeoricoRepository extends JpaRepository<ExamenTeorico, Long> {
    
    List<ExamenTeorico> findByTramite(Tramite tramite);
    
    Optional<ExamenTeorico> findByTramiteAndAprobado(Tramite tramite, Boolean aprobado);
    
    @Query("SELECT e FROM ExamenTeorico e WHERE e.tramite = :tramite AND e.aprobado = true ORDER BY e.fecha DESC")
    Optional<ExamenTeorico> findUltimoExamenAprobado(@Param("tramite") Tramite tramite);
    
    @Query("SELECT e FROM ExamenTeorico e WHERE e.fecha BETWEEN :desde AND :hasta")
    List<ExamenTeorico> findExamenesEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT AVG(e.puntaje) FROM ExamenTeorico e WHERE e.fecha BETWEEN :desde AND :hasta")
    Double findPuntajePromedioEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(e) FROM ExamenTeorico e WHERE e.aprobado = true AND e.fecha BETWEEN :desde AND :hasta")
    Long countAprobadosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(e) FROM ExamenTeorico e WHERE e.fecha BETWEEN :desde AND :hasta")
    Long countTotalEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    List<ExamenTeorico> findByExaminador(String examinador);
    
    @Query("SELECT COUNT(e) FROM ExamenTeorico e WHERE e.aprobado = false OR e.aprobado IS NULL")
    Long countByAprobadoFalseOrNull();
}
