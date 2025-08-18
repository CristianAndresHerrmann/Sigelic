package com.example.sigelic.repository;

import com.example.sigelic.model.Recurso;
import com.example.sigelic.model.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Recurso
 */
@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long> {
    
    List<Recurso> findByTipo(TipoRecurso tipo);
    
    List<Recurso> findByActivo(Boolean activo);
    
    Optional<Recurso> findByNombre(String nombre);
    
    @Query("SELECT r FROM Recurso r WHERE r.tipo = :tipo AND r.activo = true")
    List<Recurso> findRecursosActivosPorTipo(@Param("tipo") TipoRecurso tipo);
    
    @Query("SELECT r FROM Recurso r WHERE r.activo = true ORDER BY r.nombre ASC")
    List<Recurso> findAllActivos();
    
    List<Recurso> findByUbicacionContainingIgnoreCase(String ubicacion);
    
    boolean existsByNombre(String nombre);
}
