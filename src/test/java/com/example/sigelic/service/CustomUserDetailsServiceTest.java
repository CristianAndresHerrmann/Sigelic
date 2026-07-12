package com.example.sigelic.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;
import com.example.sigelic.repository.UsuarioRepository;
import com.example.sigelic.service.CustomUserDetailsService.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero1");
        usuario.setPassword("hash");
        usuario.setRol(RolSistema.CAJERO);
        usuario.setActivo(true);
        usuario.setCuentaBloqueada(false);
        usuario.setCambioPasswordRequerido(false);
    }

    @Test
    @DisplayName("Debe cargar un usuario existente con sus authorities")
    void debeCargarUsuarioExistente() {
        when(usuarioRepository.findByUsername("cajero1")).thenReturn(Optional.of(usuario));

        UserDetails resultado = customUserDetailsService.loadUserByUsername("cajero1");

        assertThat(resultado.getUsername()).isEqualTo("cajero1");
        assertThat(resultado.getPassword()).isEqualTo("hash");
        assertThat(resultado.getAuthorities())
                .extracting(Object::toString)
                .contains("ROLE_CAJERO");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void debeLanzarExcepcionCuandoNoExiste() {
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("inexistente"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("inexistente");
    }

    @Test
    @DisplayName("La cuenta bloqueada reporta isAccountNonLocked en false")
    void cuentaBloqueadaReportaNoLocked() {
        usuario.setCuentaBloqueada(true);

        CustomUserDetails details = new CustomUserDetails(usuario);

        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("La cuenta inactiva reporta isEnabled en false")
    void cuentaInactivaReportaDisabled() {
        usuario.setActivo(false);

        CustomUserDetails details = new CustomUserDetails(usuario);

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Refleja si el usuario requiere cambio de contraseña")
    void reflejaRequiereCambioPassword() {
        usuario.setCambioPasswordRequerido(true);

        CustomUserDetails details = new CustomUserDetails(usuario);

        assertThat(details.requiereCambioPassword()).isTrue();
        assertThat(details.getUsuario()).isEqualTo(usuario);
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }
}
