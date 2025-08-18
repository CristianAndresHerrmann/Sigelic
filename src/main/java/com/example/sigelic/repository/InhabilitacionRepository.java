package com.example.sigelic.repository;

import com.example.sigelic.model.Inhabilitacion;
import com.example.sigelic.model.Titular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Inhabilitacion
 */
@Repository
public interface InhabilitacionRepository extends JpaRepository<Inhabilitacion, Long> {
    
    List<Inhabilitacion> findByTitular(Titular titular);
    
    @Query("SELECT i FROM Inhabilitacion i WHERE i.titular = :titular AND (i.fechaFin IS NULL OR i.fechaFin >= CURRENT_DATE)")
    List<Inhabilitacion> findInhabilitacionesActivasByTitular(@Param("titular") Titular titular);
    
    @Query("SELECT i FROM Inhabilitacion i WHERE i.fechaFin IS NULL OR i.fechaFin >= CURRENT_DATE")
    List<Inhabilitacion> findInhabilitacionesActivas();
    
    @Query("SELECT i FROM Inhabilitacion i WHERE i.fechaFin <= :fecha AND i.fechaFin IS NOT NULL")
    List<Inhabilitacion> findInhabilitacionesVencidas(@Param("fecha") LocalDate fecha);
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inhabilitacion i WHERE i.titular = :titular AND (i.fechaFin IS NULL OR i.fechaFin >= CURRENT_DATE)")
    boolean existsInhabilitacionActivaByTitular(@Param("titular") Titular titular);
    
    @Query("SELECT i FROM Inhabilitacion i WHERE i.fechaInicio BETWEEN :desde AND :hasta")
    List<Inhabilitacion> findInhabilitacionesEnPeriodo(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
    
    List<Inhabilitacion> findByAutoridad(String autoridad);
    
    List<Inhabilitacion> findByNumeroExpediente(String numeroExpediente);
}
