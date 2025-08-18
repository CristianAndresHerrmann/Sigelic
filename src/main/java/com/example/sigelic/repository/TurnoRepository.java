package com.example.sigelic.repository;

import com.example.sigelic.model.Turno;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Recurso;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.model.EstadoTurno;
import com.example.sigelic.model.TipoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Turno
 */
@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    List<Turno> findByTitular(Titular titular);
    
    List<Turno> findByTitularAndEstado(Titular titular, EstadoTurno estado);
    
    List<Turno> findByRecurso(Recurso recurso);
    
    List<Turno> findByTramite(Tramite tramite);
    
    List<Turno> findByTipoAndEstado(TipoTurno tipo, EstadoTurno estado);
    
    @Query("SELECT t FROM Turno t WHERE t.inicio BETWEEN :desde AND :hasta ORDER BY t.inicio ASC")
    List<Turno> findTurnosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT t FROM Turno t WHERE t.recurso = :recurso AND t.estado IN ('RESERVADO', 'CONFIRMADO') AND " +
           "((t.inicio <= :fin AND t.fin >= :inicio))")
    List<Turno> findTurnosConflictivos(@Param("recurso") Recurso recurso, 
                                      @Param("inicio") LocalDateTime inicio, 
                                      @Param("fin") LocalDateTime fin);
    
    @Query("SELECT t FROM Turno t WHERE t.titular = :titular AND t.tipo = :tipo AND t.estado IN ('RESERVADO', 'CONFIRMADO') AND " +
           "((t.inicio <= :fin AND t.fin >= :inicio))")
    List<Turno> findTurnosSolapadosDelTitular(@Param("titular") Titular titular,
                                             @Param("tipo") TipoTurno tipo,
                                             @Param("inicio") LocalDateTime inicio,
                                             @Param("fin") LocalDateTime fin);
    
    @Query("SELECT t FROM Turno t WHERE t.profesionalAsignado = :profesional AND t.inicio BETWEEN :desde AND :hasta")
    List<Turno> findTurnosByProfesionalEnPeriodo(@Param("profesional") String profesional, 
                                                @Param("desde") LocalDateTime desde, 
                                                @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.estado = :estado AND t.inicio BETWEEN :desde AND :hasta")
    Long countByEstadoEnPeriodo(@Param("estado") EstadoTurno estado, 
                               @Param("desde") LocalDateTime desde, 
                               @Param("hasta") LocalDateTime hasta);
}
