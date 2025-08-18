package com.example.sigelic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sigelic.model.Titular;

/**
 * Repositorio para la entidad Titular
 */
@Repository
public interface TitularRepository extends JpaRepository<Titular, Long> {
    
    Optional<Titular> findByDni(String dni);
    
    List<Titular> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
    
    @Query("SELECT t FROM Titular t WHERE t.email = :email")
    Optional<Titular> findByEmail(@Param("email") String email);
    
    @Query("SELECT t FROM Titular t WHERE EXISTS (SELECT i FROM Inhabilitacion i WHERE i.titular = t AND i.fechaFin IS NULL OR i.fechaFin >= CURRENT_DATE)")
    List<Titular> findTitularesConInhabilitacionesActivas();
    
    boolean existsByDni(String dni);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT t FROM Titular t WHERE LOWER(CONCAT(t.apellido, ', ', t.nombre)) LIKE LOWER(CONCAT('%', :nombreCompleto, '%'))")
    List<Titular> findByNombreCompletoContainingIgnoreCase(@Param("nombreCompleto") String nombreCompleto);
}
