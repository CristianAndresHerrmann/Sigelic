package com.example.sigelic.repository;

import com.example.sigelic.model.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Configuracion
 */
@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {

    /**
     * Busca una configuración por su clave
     */
    Optional<Configuracion> findByClave(String clave);

    /**
     * Busca configuraciones por categoría
     */
    List<Configuracion> findByCategoria(String categoria);

    /**
     * Busca configuraciones por categoría ordenadas por clave
     */
    List<Configuracion> findByCategoriaOrderByClave(String categoria);

    /**
     * Busca todas las configuraciones modificables
     */
    List<Configuracion> findByModificableTrue();

    /**
     * Busca configuraciones modificables por categoría
     */
    List<Configuracion> findByCategoriaAndModificableTrue(String categoria);

    /**
     * Verifica si existe una configuración con la clave especificada
     */
    boolean existsByClave(String clave);

    /**
     * Obtiene el valor de una configuración por su clave
     */
    @Query("SELECT c.valor FROM Configuracion c WHERE c.clave = :clave")
    Optional<String> findValorByClave(@Param("clave") String clave);

    /**
     * Obtiene todas las configuraciones agrupadas por categoría
     */
    @Query("SELECT c FROM Configuracion c ORDER BY c.categoria, c.clave")
    List<Configuracion> findAllOrderedByCategoriaAndClave();
}
