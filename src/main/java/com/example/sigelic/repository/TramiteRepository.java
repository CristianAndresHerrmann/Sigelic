package com.example.sigelic.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;

/**
 * Repositorio para la entidad Tramite
 */
@Repository
public interface TramiteRepository extends JpaRepository<Tramite, Long> {
    
    @Query("SELECT t FROM Tramite t LEFT JOIN FETCH t.titular ORDER BY t.fechaCreacion DESC")
    List<Tramite> findAllWithTitular();
    
    List<Tramite> findByTitular(Titular titular);
    
    List<Tramite> findByEstado(EstadoTramite estado);
    
    List<Tramite> findByTipoAndEstado(TipoTramite tipo, EstadoTramite estado);
    
    @Query("SELECT t FROM Tramite t WHERE t.titular = :titular AND t.estado IN :estados ORDER BY t.fechaCreacion DESC")
    Optional<Tramite> findTramiteActivoByTitular(@Param("titular") Titular titular, @Param("estados") List<EstadoTramite> estados);
    
    @Query("SELECT t FROM Tramite t WHERE t.fechaCreacion BETWEEN :desde AND :hasta")
    List<Tramite> findTramitesEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(t) FROM Tramite t WHERE t.estado = :estado")
    Long countByEstado(@Param("estado") EstadoTramite estado);
    
    @Query("SELECT COUNT(t) FROM Tramite t WHERE t.tipo = :tipo AND t.fechaCreacion BETWEEN :desde AND :hasta")
    Long countByTipoEnPeriodo(@Param("tipo") TipoTramite tipo, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    boolean existsByTitularAndEstadoIn(Titular titular, List<EstadoTramite> estados);
    
    @Query("SELECT t FROM Tramite t WHERE t.estado = :estado ORDER BY t.fechaCreacion ASC")
    List<Tramite> findTramitesPendientesByEstado(@Param("estado") EstadoTramite estado);
}
