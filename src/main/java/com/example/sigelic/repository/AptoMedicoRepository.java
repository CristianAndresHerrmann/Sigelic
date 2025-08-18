package com.example.sigelic.repository;

import com.example.sigelic.model.AptoMedico;
import com.example.sigelic.model.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad AptoMedico
 */
@Repository
public interface AptoMedicoRepository extends JpaRepository<AptoMedico, Long> {
    
    List<AptoMedico> findByTramite(Tramite tramite);
    
    Optional<AptoMedico> findByTramiteAndApto(Tramite tramite, Boolean apto);
    
    @Query("SELECT a FROM AptoMedico a WHERE a.tramite = :tramite AND a.apto = true ORDER BY a.fecha DESC")
    Optional<AptoMedico> findUltimoAptoVigente(@Param("tramite") Tramite tramite);
    
    @Query("SELECT a FROM AptoMedico a WHERE a.fechaVencimiento BETWEEN :desde AND :hasta AND a.apto = true")
    List<AptoMedico> findAptosProximosAVencer(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
    
    @Query("SELECT a FROM AptoMedico a WHERE a.fechaVencimiento < :fecha AND a.apto = true")
    List<AptoMedico> findAptosVencidos(@Param("fecha") LocalDate fecha);
    
    List<AptoMedico> findByProfesional(String profesional);
    
    @Query("SELECT a FROM AptoMedico a WHERE a.fecha BETWEEN :desde AND :hasta")
    List<AptoMedico> findAptosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(a) FROM AptoMedico a WHERE a.apto = true AND a.fecha BETWEEN :desde AND :hasta")
    Long countAptosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT COUNT(a) FROM AptoMedico a WHERE a.fecha BETWEEN :desde AND :hasta")
    Long countTotalEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
