package com.example.sigelic.repository;

import com.example.sigelic.model.Pago;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.model.EstadoPago;
import com.example.sigelic.model.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Pago
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    List<Pago> findByTramite(Tramite tramite);
    
    Optional<Pago> findByTramiteAndEstado(Tramite tramite, EstadoPago estado);
    
    List<Pago> findByEstado(EstadoPago estado);
    
    List<Pago> findByMedio(MedioPago medio);
    
    Optional<Pago> findByNumeroTransaccion(String numeroTransaccion);
    
    Optional<Pago> findByNumeroComprobante(String numeroComprobante);
    
    @Query("SELECT p FROM Pago p WHERE p.fecha BETWEEN :desde AND :hasta AND p.estado = 'ACREDITADO'")
    List<Pago> findPagosAcreditadosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.fecha BETWEEN :desde AND :hasta AND p.estado = 'ACREDITADO'")
    BigDecimal sumMontoAcreditadoEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT p FROM Pago p WHERE p.fechaVencimiento < :fecha AND p.estado = 'PENDIENTE'")
    List<Pago> findPagosVencidos(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.medio = :medio AND p.fecha BETWEEN :desde AND :hasta")
    Long countByMedioEnPeriodo(@Param("medio") MedioPago medio, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
    
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.fechaAcreditacion BETWEEN :desde AND :hasta AND p.estado = 'ACREDITADO'")
    BigDecimal sumMontoByFechaPagoBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
