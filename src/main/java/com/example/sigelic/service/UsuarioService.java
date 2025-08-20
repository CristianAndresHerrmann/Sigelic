package com.example.sigelic.service;

import com.example.sigelic.model.Permiso;
import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;
import com.example.sigelic.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios del sistema
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final int MAX_INTENTOS_FALLIDOS = 3;
    private static final int DIAS_INACTIVIDAD = 90;

    /**
     * Busca un usuario por ID
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por username
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Busca un usuario por email
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Obtiene todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene usuarios activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> findUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Busca usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<Usuario> findByRol(RolSistema rol) {
        return usuarioRepository.findByRol(rol);
    }

    /**
     * Crea un nuevo usuario
     */
    public Usuario crearUsuario(Usuario usuario) {
        log.info("Creando nuevo usuario: {}", usuario.getUsername());
        
        // Validar que no exista el username
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el username: " + usuario.getUsername());
        }

        // Validar que no exista el email (si se proporciona)
        if (usuario.getEmail() != null && usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + usuario.getEmail());
        }

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Establecer valores por defecto
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setActivo(true);
        usuario.setCuentaBloqueada(false);
        usuario.setIntentosFallidos(0);
        usuario.setCambioPasswordRequerido(true); // Primer login requiere cambio
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente: {}", usuarioGuardado.getUsername());
        
        return usuarioGuardado;
    }

    /**
     * Actualiza un usuario existente
     */
    public Usuario actualizarUsuario(Usuario usuario) {
        log.info("Actualizando usuario: {}", usuario.getUsername());
        
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuario.getId()));

        // Validar username único (excluyendo el usuario actual)
        if (usuarioRepository.existsByUsernameAndIdNot(usuario.getUsername(), usuario.getId())) {
            throw new IllegalArgumentException("Ya existe otro usuario con el username: " + usuario.getUsername());
        }

        // Validar email único (excluyendo el usuario actual)
        if (usuario.getEmail() != null && 
            usuarioRepository.existsByEmailAndIdNot(usuario.getEmail(), usuario.getId())) {
            throw new IllegalArgumentException("Ya existe otro usuario con el email: " + usuario.getEmail());
        }

        // Actualizar campos modificables
        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setApellido(usuario.getApellido());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setTelefono(usuario.getTelefono());
        usuarioExistente.setDni(usuario.getDni());
        usuarioExistente.setDireccion(usuario.getDireccion());
        usuarioExistente.setRol(usuario.getRol());
        usuarioExistente.setFechaActualizacion(LocalDateTime.now());
        
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        log.info("Usuario actualizado exitosamente: {}", usuarioActualizado.getUsername());
        
        return usuarioActualizado;
    }

    /**
     * Cambia la contraseña de un usuario
     */
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) {
        log.info("Cambiando contraseña para usuario ID: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Validar nueva contraseña
        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuario.setCambioPasswordRequerido(false);
        usuario.setFechaActualizacion(LocalDateTime.now());
        
        usuarioRepository.save(usuario);
        log.info("Contraseña cambiada exitosamente para usuario: {}", usuario.getUsername());
    }

    /**
     * Resetea la contraseña de un usuario (solo para administradores)
     */
    public String resetearPassword(Long usuarioId) {
        log.info("Reseteando contraseña para usuario ID: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Generar contraseña temporal
        String passwordTemporal = generarPasswordTemporal();
        
        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuario.setCambioPasswordRequerido(true);
        usuario.setFechaActualizacion(LocalDateTime.now());
        
        usuarioRepository.save(usuario);
        log.info("Contraseña reseteada para usuario: {}", usuario.getUsername());
        
        return passwordTemporal;
    }

    /**
     * Activa o desactiva un usuario
     */
    public Usuario cambiarEstadoActivo(Long usuarioId, boolean activo) {
        log.info("Cambiando estado activo del usuario ID: {} a {}", usuarioId, activo);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(activo);
        usuario.setFechaActualizacion(LocalDateTime.now());
        
        if (!activo) {
            // Si se desactiva, también desbloquear por si estaba bloqueado
            usuario.desbloquearCuenta();
        }
        
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Estado del usuario {} cambiado a: {}", usuario.getUsername(), activo ? "ACTIVO" : "INACTIVO");
        
        return usuarioActualizado;
    }

    /**
     * Bloquea o desbloquea una cuenta de usuario
     */
    public Usuario cambiarBloqueo(Long usuarioId, boolean bloquear) {
        log.info("Cambiando bloqueo del usuario ID: {} a {}", usuarioId, bloquear);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (bloquear) {
            usuario.bloquearCuenta();
        } else {
            usuario.desbloquearCuenta();
        }
        
        usuario.setFechaActualizacion(LocalDateTime.now());
        
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Usuario {} {}", usuario.getUsername(), bloquear ? "BLOQUEADO" : "DESBLOQUEADO");
        
        return usuarioActualizado;
    }

    /**
     * Asigna un rol a un usuario
     */
    public Usuario asignarRol(Long usuarioId, RolSistema rol) {
        log.info("Asignando rol al usuario ID: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setRol(rol);
        usuario.setFechaActualizacion(LocalDateTime.now());
        
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Rol asignado al usuario {}: {}", usuario.getUsername(), rol);
        
        return usuarioActualizado;
    }

    /**
     * Registra un intento de login fallido
     */
    public void registrarIntentoFallido(String username) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.incrementarIntentosFallidos();
            
            // Bloquear cuenta si supera el límite
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
                usuario.bloquearCuenta();
                log.warn("Cuenta bloqueada por exceder intentos fallidos: {}", username);
            }
            
            usuarioRepository.save(usuario);
        }
    }

    /**
     * Registra un login exitoso
     */
    public void registrarLoginExitoso(String username) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.resetearIntentosFallidos();
            usuario.actualizarUltimoAcceso();
            
            usuarioRepository.save(usuario);
            log.info("Login exitoso registrado para: {}", username);
        }
    }

    /**
     * Verifica si un usuario tiene un permiso específico
     */
    @Transactional(readOnly = true)
    public boolean tienePermiso(Long usuarioId, Permiso permiso) {
        return usuarioRepository.findById(usuarioId)
                .map(usuario -> usuario.tienePermiso(permiso.getAuthority()))
                .orElse(false);
    }

    /**
     * Obtiene usuarios inactivos (sin acceso reciente)
     */
    @Transactional(readOnly = true)
    public List<Usuario> getUsuariosInactivos() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(DIAS_INACTIVIDAD);
        return usuarioRepository.findUsuariosInactivos(fechaLimite);
    }

    /**
     * Busca usuarios por término de búsqueda
     */
    @Transactional(readOnly = true)
    public List<Usuario> buscarUsuarios(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return findAll();
        }
        return usuarioRepository.findByNombreOrApellidoContainingIgnoreCase(termino.trim());
    }

    /**
     * Busca usuarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<Usuario> buscarUsuarios(String termino, Pageable pageable) {
        if (termino == null || termino.trim().isEmpty()) {
            return usuarioRepository.findAll(pageable);
        }
        return usuarioRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                termino.trim(), termino.trim(), termino.trim(), pageable);
    }

    /**
     * Obtiene todos los usuarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<Usuario> obtenerTodosLosUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    /**
     * Obtiene usuarios por rol con paginación
     */
    @Transactional(readOnly = true)
    public Page<Usuario> obtenerUsuariosPorRol(RolSistema rol, Pageable pageable) {
        return usuarioRepository.findByRol(rol, pageable);
    }

    /**
     * Obtiene usuarios por estado con paginación
     */
    @Transactional(readOnly = true)
    public Page<Usuario> obtenerUsuariosPorEstado(Boolean activo, Pageable pageable) {
        return usuarioRepository.findByActivo(activo, pageable);
    }

    /**
     * Obtiene usuario por ID
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Obtiene usuario por username
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    /**
     * Crea un nuevo usuario con parámetros individuales
     */
    public Usuario crearUsuario(String username, String password, String email, String nombre, 
                               String apellido, String telefono, String dni, String direccion,
                               RolSistema rol, boolean cambioPasswordRequerido, String creadoPor) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password); // Se encriptará en crearUsuario(Usuario)
        usuario.setEmail(email);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setTelefono(telefono);
        usuario.setDni(dni);
        usuario.setDireccion(direccion);
        usuario.setRol(rol);
        usuario.setCambioPasswordRequerido(cambioPasswordRequerido);
        usuario.setCreadoPor(creadoPor);
        
        return crearUsuario(usuario);
    }

    /**
     * Actualiza un usuario con parámetros individuales
     */
    public Usuario actualizarUsuario(Long id, String email, String nombre, String apellido,
                                   String telefono, String dni, String direccion, RolSistema rol,
                                   String actualizadoPor) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setEmail(email);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setTelefono(telefono);
        usuario.setDni(dni);
        usuario.setDireccion(direccion);
        usuario.setRol(rol);
        usuario.setActualizadoPor(actualizadoPor);
        
        return actualizarUsuario(usuario);
    }

    /**
     * Elimina un usuario
     */
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerUsuario(id);
        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado: {}", usuario.getUsername());
    }

    /**
     * Activa un usuario
     */
    public Usuario activarUsuario(Long id, String actualizadoPor) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setActivo(true);
        usuario.setActualizadoPor(actualizadoPor);
        usuario.setFechaActualizacion(LocalDateTime.now());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Usuario activado: {} por: {}", usuario.getUsername(), actualizadoPor);
        return usuarioActualizado;
    }

    /**
     * Desactiva un usuario
     */
    public Usuario desactivarUsuario(Long id, String actualizadoPor) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setActivo(false);
        usuario.setActualizadoPor(actualizadoPor);
        usuario.setFechaActualizacion(LocalDateTime.now());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Usuario desactivado: {} por: {}", usuario.getUsername(), actualizadoPor);
        return usuarioActualizado;
    }

    /**
     * Bloquea la cuenta de un usuario
     */
    public Usuario bloquearCuenta(Long id, String actualizadoPor) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setCuentaBloqueada(true);
        usuario.setActualizadoPor(actualizadoPor);
        usuario.setFechaActualizacion(LocalDateTime.now());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Cuenta bloqueada: {} por: {}", usuario.getUsername(), actualizadoPor);
        return usuarioActualizado;
    }

    /**
     * Desbloquea la cuenta de un usuario
     */
    public Usuario desbloquearCuenta(Long id, String actualizadoPor) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setCuentaBloqueada(false);
        usuario.setIntentosFallidos(0);
        usuario.setActualizadoPor(actualizadoPor);
        usuario.setFechaActualizacion(LocalDateTime.now());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Cuenta desbloqueada: {} por: {}", usuario.getUsername(), actualizadoPor);
        return usuarioActualizado;
    }

    /**
     * Cambia la contraseña de un usuario por username
     */
    public void cambiarPassword(String username, String passwordActual, String passwordNueva) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        cambiarPassword(usuario.getId(), passwordActual, passwordNueva);
    }

    /**
     * Resetea la contraseña de un usuario
     */
    public String resetearPassword(Long usuarioId, String actualizadoPor) {
        String nuevaPassword = resetearPassword(usuarioId);
        Usuario usuario = obtenerUsuario(usuarioId);
        usuario.setActualizadoPor(actualizadoPor);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        return nuevaPassword;
    }

    /**
     * Obtiene usuarios inactivos
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosInactivos(int diasInactividad) {
        LocalDateTime fechaCorte = LocalDateTime.now().minusDays(diasInactividad);
        return usuarioRepository.findUsuariosInactivos(fechaCorte);
    }

    /**
     * Genera una contraseña temporal
     */
    private String generarPasswordTemporal() {
        return "Temp" + System.currentTimeMillis() % 10000;
    }

    /**
     * Cuenta usuarios por rol
     */
    @Transactional(readOnly = true)
    public Long contarUsuariosPorRol(RolSistema rol) {
        return usuarioRepository.countByRol(rol);
    }

    /**
     * Verifica si existe un usuario con el username dado
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    /**
     * Verifica si existe un usuario con el email dado
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}
