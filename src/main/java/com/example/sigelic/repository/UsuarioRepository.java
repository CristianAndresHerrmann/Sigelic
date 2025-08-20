package com.example.sigelic.repository;

import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de usuarios del sistema
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por rol
     */
    List<Usuario> findByRol(RolSistema rol);

    /**
     * Busca usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Busca usuarios bloqueados
     */
    List<Usuario> findByCuentaBloqueadaTrue();

    /**
     * Busca usuarios por nombre o apellido (búsqueda parcial)
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Usuario> findByNombreOrApellidoContainingIgnoreCase(@Param("termino") String termino);

    /**
     * Busca usuarios que requieren cambio de contraseña
     */
    List<Usuario> findByCambioPasswordRequeridoTrue();

    /**
     * Busca usuarios con intentos fallidos mayor a un número específico
     */
    List<Usuario> findByIntentosFallidosGreaterThan(Integer intentos);

    /**
     * Busca usuarios que no han accedido desde una fecha específica
     */
    @Query("SELECT u FROM Usuario u WHERE u.ultimoAcceso < :fecha OR u.ultimoAcceso IS NULL")
    List<Usuario> findUsuariosInactivos(@Param("fecha") LocalDateTime fecha);

    /**
     * Cuenta usuarios por rol (adaptado para el modelo único de rol)
     */
    Long countByRol(RolSistema rol);

    /**
     * Busca usuarios por estado activo
     */
    List<Usuario> findByActivo(Boolean activo);

    /**
     * Busca usuarios por estado activo con paginación
     */
    Page<Usuario> findByActivo(Boolean activo, Pageable pageable);

    /**
     * Busca usuarios por rol con paginación
     */
    Page<Usuario> findByRol(RolSistema rol, Pageable pageable);

    /**
     * Busca usuarios por nombre, apellido o username con paginación
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Usuario> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            @Param("nombre") String nombre, @Param("apellido") String apellido, 
            @Param("username") String username, Pageable pageable);

    /**
     * Verifica si existe un usuario con el username (excluyendo un ID específico)
     */
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.username = :username AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);

    /**
     * Verifica si existe un usuario con el email (excluyendo un ID específico)
     */
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}
