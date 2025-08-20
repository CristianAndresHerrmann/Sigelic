package com.example.sigelic.service;

import com.example.sigelic.model.Usuario;
import com.example.sigelic.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementación de UserDetailsService para Spring Security
 * Proporciona los detalles del usuario para autenticación y autorización
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario: {}", username);
        
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new CustomUserDetails(usuario);
    }

    /**
     * Implementación personalizada de UserDetails
     */
    public static class CustomUserDetails implements UserDetails {
        
        private final Usuario usuario;

        public CustomUserDetails(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Convertir permisos a authorities de Spring Security
            return usuario.getPermisos().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        @Override
        public String getPassword() {
            return usuario.getPassword();
        }

        @Override
        public String getUsername() {
            return usuario.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            // Por ahora no manejamos expiración de cuentas
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return !usuario.getCuentaBloqueada();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            // Por ahora no manejamos expiración de credenciales
            return true;
        }

        @Override
        public boolean isEnabled() {
            return usuario.getActivo();
        }

        /**
         * Obtiene el usuario completo
         */
        public Usuario getUsuario() {
            return usuario;
        }

        /**
         * Verifica si el usuario requiere cambio de contraseña
         */
        public boolean requiereCambioPassword() {
            return usuario.getCambioPasswordRequerido();
        }
    }
}
