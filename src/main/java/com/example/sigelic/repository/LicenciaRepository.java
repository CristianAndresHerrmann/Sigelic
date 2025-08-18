package com.example.sigelic.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoLicencia;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.Titular;

/**
 * Repositorio para la entidad Licencia
 */
@Repository
public interface LicenciaRepository extends JpaRepository<Licencia, Long> {
    
    List<Licencia> findByTitular(Titular titular);
    
    List<Licencia> findByTitularAndEstado(Titular titular, EstadoLicencia estado);
    
    Optional<Licencia> findByTitularAndClaseAndEstado(Titular titular, ClaseLicencia clase, EstadoLicencia estado);
    
    Optional<Licencia> findByNumeroLicencia(String numeroLicencia);
    
    @Query("SELECT l FROM Licencia l WHERE l.fechaVencimiento BETWEEN :desde AND :hasta AND l.estado = 'VIGENTE'")
    List<Licencia> findLicenciasProximasAVencer(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
    
    @Query("SELECT l FROM Licencia l WHERE l.fechaVencimiento < :fecha AND l.estado = 'VIGENTE'")
    List<Licencia> findLicenciasVencidas(@Param("fecha") LocalDate fecha);
    
    @Query("SELECT COUNT(l) FROM Licencia l WHERE l.fechaEmision BETWEEN :desde AND :hasta")
    Long countLicenciasEmitidasEnPeriodo(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
    
    @Query("SELECT l FROM Licencia l WHERE l.titular = :titular AND l.estado = 'VIGENTE' ORDER BY l.fechaEmision DESC")
    List<Licencia> findLicenciasVigentesByTitular(@Param("titular") Titular titular);
    
    boolean existsByNumeroLicencia(String numeroLicencia);
}
