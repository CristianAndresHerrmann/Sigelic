package com.example.sigelic.repository;

import com.example.sigelic.model.CostoTramite;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.ClaseLicencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad CostoTramite
 */
@Repository
public interface CostoTramiteRepository extends JpaRepository<CostoTramite, Long> {
    
    List<CostoTramite> findByTipoTramite(TipoTramite tipoTramite);
    
    List<CostoTramite> findByClaseLicencia(ClaseLicencia claseLicencia);
    
    List<CostoTramite> findByActivo(Boolean activo);
    
    @Query("SELECT c FROM CostoTramite c WHERE c.tipoTramite = :tipo AND c.claseLicencia = :clase AND c.activo = true AND " +
           "c.fechaVigenciaDesde <= :fecha AND (c.fechaVigenciaHasta IS NULL OR c.fechaVigenciaHasta >= :fecha)")
    Optional<CostoTramite> findCostoVigente(@Param("tipo") TipoTramite tipo, 
                                           @Param("clase") ClaseLicencia clase, 
                                           @Param("fecha") LocalDate fecha);
    
    @Query("SELECT c FROM CostoTramite c WHERE c.activo = true AND " +
           "c.fechaVigenciaDesde <= CURRENT_DATE AND (c.fechaVigenciaHasta IS NULL OR c.fechaVigenciaHasta >= CURRENT_DATE)")
    List<CostoTramite> findCostosVigentes();
    
    @Query("SELECT c FROM CostoTramite c WHERE c.fechaVigenciaHasta < :fecha AND c.activo = true")
    List<CostoTramite> findCostosVencidos(@Param("fecha") LocalDate fecha);
}
