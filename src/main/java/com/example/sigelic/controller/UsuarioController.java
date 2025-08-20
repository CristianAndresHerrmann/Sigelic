package com.example.sigelic.controller;

import com.example.sigelic.dto.usuario.*;
import com.example.sigelic.model.Permiso;
import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;
import com.example.sigelic.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Crear un nuevo usuario
     */
    @PostMapping
    @PreAuthorize("hasAuthority('USUARIOS_CREAR')")
    public ResponseEntity<UsuarioDTO> crearUsuario(
            @Valid @RequestBody CrearUsuarioDTO crearUsuarioDTO,
            Principal principal) {
        
        log.info("Creando usuario: {} por: {}", crearUsuarioDTO.getUsername(), principal.getName());
        
        Usuario usuario = usuarioService.crearUsuario(
                crearUsuarioDTO.getUsername(),
                crearUsuarioDTO.getPassword(),
                crearUsuarioDTO.getEmail(),
                crearUsuarioDTO.getNombre(),
                crearUsuarioDTO.getApellido(),
                crearUsuarioDTO.getTelefono(),
                crearUsuarioDTO.getDni(),
                crearUsuarioDTO.getDireccion(),
                crearUsuarioDTO.getRol(),
                crearUsuarioDTO.isCambioPasswordRequerido(),
                principal.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(usuario));
    }

    /**
     * Obtener todos los usuarios con paginación
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USUARIOS_LEER')")
    public ResponseEntity<Page<UsuarioDTO>> obtenerUsuarios(
            Pageable pageable,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) RolSistema rol,
            @RequestParam(required = false) Boolean activo) {
        
        Page<Usuario> usuarios;
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            usuarios = usuarioService.buscarUsuarios(busqueda, pageable);
        } else if (rol != null) {
            usuarios = usuarioService.obtenerUsuariosPorRol(rol, pageable);
        } else if (activo != null) {
            usuarios = usuarioService.obtenerUsuariosPorEstado(activo, pageable);
        } else {
            usuarios = usuarioService.obtenerTodosLosUsuarios(pageable);
        }

        Page<UsuarioDTO> usuariosDTO = usuarios.map(this::convertirADTO);
        return ResponseEntity.ok(usuariosDTO);
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_LEER')")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuario(id);
        return ResponseEntity.ok(convertirADTO(usuario));
    }

    /**
     * Obtener perfil del usuario actual
     */
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> obtenerPerfil(Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(username);
        return ResponseEntity.ok(convertirADTO(usuario));
    }

    /**
     * Actualizar usuario
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioDTO actualizarUsuarioDTO,
            Principal principal) {
        
        log.info("Actualizando usuario ID: {} por: {}", id, principal.getName());
        
        Usuario usuario = usuarioService.actualizarUsuario(
                id,
                actualizarUsuarioDTO.getEmail(),
                actualizarUsuarioDTO.getNombre(),
                actualizarUsuarioDTO.getApellido(),
                actualizarUsuarioDTO.getTelefono(),
                actualizarUsuarioDTO.getDni(),
                actualizarUsuarioDTO.getDireccion(),
                actualizarUsuarioDTO.getRol(),
                principal.getName()
        );

        // Actualizar estado si se proporciona
        if (actualizarUsuarioDTO.getActivo() != null) {
            if (actualizarUsuarioDTO.getActivo()) {
                usuario = usuarioService.activarUsuario(id, principal.getName());
            } else {
                usuario = usuarioService.desactivarUsuario(id, principal.getName());
            }
        }

        // Actualizar bloqueo si se proporciona
        if (actualizarUsuarioDTO.getCuentaBloqueada() != null) {
            if (actualizarUsuarioDTO.getCuentaBloqueada()) {
                usuario = usuarioService.bloquearCuenta(id, principal.getName());
            } else {
                usuario = usuarioService.desbloquearCuenta(id, principal.getName());
            }
        }

        return ResponseEntity.ok(convertirADTO(usuario));
    }

    /**
     * Eliminar usuario
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_ELIMINAR')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id, Principal principal) {
        log.info("Eliminando usuario ID: {} por: {}", id, principal.getName());
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambiar contraseña del usuario actual
     */
    @PostMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody CambioPasswordDTO cambioPasswordDTO,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Cambio de contraseña para usuario: {}", username);
        
        if (!cambioPasswordDTO.getNuevaPassword().equals(cambioPasswordDTO.getConfirmarPassword())) {
            return ResponseEntity.badRequest().build();
        }

        usuarioService.cambiarPassword(
                username,
                cambioPasswordDTO.getPasswordActual(),
                cambioPasswordDTO.getNuevaPassword()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Resetear contraseña de un usuario (solo administradores)
     */
    @PostMapping("/{id}/resetear-password")
    @PreAuthorize("hasAuthority('USUARIOS_RESETEAR_PASSWORD')")
    public ResponseEntity<String> resetearPassword(@PathVariable Long id, Principal principal) {
        log.info("Reseteando contraseña para usuario ID: {} por: {}", id, principal.getName());
        
        String nuevaPassword = usuarioService.resetearPassword(id, principal.getName());
        return ResponseEntity.ok(nuevaPassword);
    }

    /**
     * Activar usuario
     */
    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
    public ResponseEntity<UsuarioDTO> activarUsuario(@PathVariable Long id, Principal principal) {
        log.info("Activando usuario ID: {} por: {}", id, principal.getName());
        
        Usuario usuario = usuarioService.activarUsuario(id, principal.getName());
        return ResponseEntity.ok(convertirADTO(usuario));
    }

    /**
     * Desactivar usuario
     */
    @PostMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
    public ResponseEntity<UsuarioDTO> desactivarUsuario(@PathVariable Long id, Principal principal) {
        log.info("Desactivando usuario ID: {} por: {}", id, principal.getName());
        
        Usuario usuario = usuarioService.desactivarUsuario(id, principal.getName());
        return ResponseEntity.ok(convertirADTO(usuario));
    }

    /**
     * Bloquear cuenta de usuario
     */
    @PostMapping("/{id}/bloquear")
    @PreAuthorize("hasAuthority('USUARIOS_BLOQUEAR')")
    public ResponseEntity<UsuarioDTO> bloquearCuenta(@PathVariable Long id, Principal principal) {
        log.info("Bloqueando cuenta de usuario ID: {} por: {}", id, principal.getName());
        
        Usuario usuario = usuarioService.bloquearCuenta(id, principal.getName());
        return ResponseEntity.ok(convertirADTO(usuario));
    }

    /**
     * Desbloquear cuenta de usuario
     */
    @PostMapping("/{id}/desbloquear")
    @PreAuthorize("hasAuthority('USUARIOS_BLOQUEAR')")
    public ResponseEntity<UsuarioDTO> desbloquearCuenta(@PathVariable Long id, Principal principal) {
        log.info("Desbloqueando cuenta de usuario ID: {} por: {}", id, principal.getName());
        
        Usuario usuario = usuarioService.desbloquearCuenta(id, principal.getName());
        return ResponseEntity.ok(convertirADTO(usuario));
    }

    /**
     * Obtener roles disponibles
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('USUARIOS_LEER')")
    public ResponseEntity<List<RolSistema>> obtenerRoles() {
        List<RolSistema> roles = Arrays.asList(RolSistema.values());
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtener permisos de un rol
     */
    @GetMapping("/roles/{rol}/permisos")
    @PreAuthorize("hasAuthority('SEGURIDAD_GESTIONAR_ROLES')")
    public ResponseEntity<List<String>> obtenerPermisosRol(@PathVariable RolSistema rol) {
        List<String> permisos = rol.getPermisos().stream()
                .map(Permiso::getAuthority)
                .collect(Collectors.toList());
        return ResponseEntity.ok(permisos);
    }

    /**
     * Obtener usuarios inactivos para limpieza
     */
    @GetMapping("/inactivos")
    @PreAuthorize("hasAuthority('AUDITORIA_ACCEDER_LOGS')")
    public ResponseEntity<List<UsuarioDTO>> obtenerUsuariosInactivos(
            @RequestParam(defaultValue = "90") int diasInactividad) {
        
        List<Usuario> usuarios = usuarioService.obtenerUsuariosInactivos(diasInactividad);
        List<UsuarioDTO> usuariosDTO = usuarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usuariosDTO);
    }

    /**
     * Convertir Usuario a UsuarioDTO
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setTelefono(usuario.getTelefono());
        dto.setDni(usuario.getDni());
        dto.setDireccion(usuario.getDireccion());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.getActivo());
        dto.setCuentaBloqueada(usuario.getCuentaBloqueada());
        dto.setCambioPasswordRequerido(usuario.getCambioPasswordRequerido());
        dto.setIntentosFallidos(usuario.getIntentosFallidos());
        dto.setUltimoAcceso(usuario.getUltimoAcceso());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaActualizacion(usuario.getFechaActualizacion());
        dto.setCreadoPor(usuario.getCreadoPor());
        dto.setActualizadoPor(usuario.getActualizadoPor());
        return dto;
    }
}
