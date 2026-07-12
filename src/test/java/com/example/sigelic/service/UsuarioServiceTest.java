package com.example.sigelic.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.sigelic.model.Permiso;
import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;
import com.example.sigelic.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero1");
        usuario.setPassword("hashActual");
        usuario.setEmail("cajero1@sigelic.gov.ar");
        usuario.setRol(RolSistema.CAJERO);
        usuario.setActivo(true);
        usuario.setCuentaBloqueada(false);
        usuario.setIntentosFallidos(0);
    }

    @Nested
    @DisplayName("Creación de usuarios")
    class Creacion {

        @Test
        @DisplayName("Debe crear un usuario nuevo encriptando la contraseña")
        void debeCrearUsuario() {
            Usuario nuevo = new Usuario();
            nuevo.setUsername("nuevo");
            nuevo.setPassword("plano123");
            nuevo.setEmail("nuevo@sigelic.gov.ar");
            nuevo.setRol(RolSistema.AGENTE);

            when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail("nuevo@sigelic.gov.ar")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plano123")).thenReturn("hashNuevo");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.crearUsuario("nuevo", "plano123", "nuevo@sigelic.gov.ar",
                    "Nuevo", "Usuario", "3425000000", "11111111", "Calle 1", RolSistema.AGENTE,
                    true, "admin");

            assertThat(resultado.getPassword()).isEqualTo("hashNuevo");
            assertThat(resultado.getActivo()).isTrue();
            assertThat(resultado.getIntentosFallidos()).isZero();
        }

        @Test
        @DisplayName("Debe rechazar un username duplicado")
        void debeRechazarUsernameDuplicado() {
            when(usuarioRepository.findByUsername("cajero1")).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> usuarioService.crearUsuario("cajero1", "pass1234", "otro@sigelic.gov.ar",
                    "N", "A", null, null, null, RolSistema.AGENTE, true, "admin"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username");
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar un email duplicado")
        void debeRechazarEmailDuplicado() {
            when(usuarioRepository.findByUsername("otro")).thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail("cajero1@sigelic.gov.ar")).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> usuarioService.crearUsuario("otro", "pass1234", "cajero1@sigelic.gov.ar",
                    "N", "A", null, null, null, RolSistema.AGENTE, true, "admin"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email");
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Actualización de usuarios")
    class Actualizacion {

        @Test
        @DisplayName("Debe actualizar los datos de un usuario existente")
        void debeActualizarUsuario() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByUsernameAndIdNot("cajero1", 1L)).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.actualizarUsuario(1L, "nuevo@sigelic.gov.ar", "Nuevo",
                    "Apellido", "3425000001", "22222222", "Calle 2", RolSistema.SUPERVISOR, "admin");

            assertThat(resultado.getEmail()).isEqualTo("nuevo@sigelic.gov.ar");
            assertThat(resultado.getRol()).isEqualTo(RolSistema.SUPERVISOR);
        }

        @Test
        @DisplayName("Debe rechazar actualizar a un username ya usado por otro usuario")
        void debeRechazarUsernameDuplicadoAlActualizar() {
            Usuario existente = new Usuario();
            existente.setId(1L);
            existente.setUsername("otro-username");
            existente.setEmail("cajero1@sigelic.gov.ar");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(usuarioRepository.existsByUsernameAndIdNot("otro-username", 1L)).thenReturn(true);

            existente.setUsername("otro-username");
            assertThatThrownBy(() -> usuarioService.actualizarUsuario(existente))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username");
        }

        @Test
        @DisplayName("Debe eliminar un usuario existente")
        void debeEliminarUsuario() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            usuarioService.eliminarUsuario(1L);

            verify(usuarioRepository).delete(usuario);
        }
    }

    @Nested
    @DisplayName("Gestión de contraseñas")
    class Passwords {

        @Test
        @DisplayName("Debe cambiar la contraseña verificando la actual")
        void debeCambiarPassword() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("actual", "hashActual")).thenReturn(true);
            when(passwordEncoder.encode("nuevaPassword")).thenReturn("hashNueva");

            usuarioService.cambiarPassword(1L, "actual", "nuevaPassword");

            assertThat(usuario.getPassword()).isEqualTo("hashNueva");
            assertThat(usuario.getCambioPasswordRequerido()).isFalse();
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Debe rechazar el cambio si la contraseña actual es incorrecta")
        void debeRechazarPasswordActualIncorrecta() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("incorrecta", "hashActual")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, "incorrecta", "nuevaPassword"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("incorrecta");
        }

        @Test
        @DisplayName("Debe rechazar una nueva contraseña demasiado corta")
        void debeRechazarPasswordCorta() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("actual", "hashActual")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, "actual", "corta"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("8 caracteres");
        }

        @Test
        @DisplayName("Debe resetear la contraseña generando una temporal")
        void debeResetearPassword() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode(any())).thenReturn("hashTemporal");

            String temporal = usuarioService.resetearPassword(1L);

            assertThat(temporal).startsWith("Temp");
            assertThat(usuario.getPassword()).isEqualTo("hashTemporal");
            assertThat(usuario.getCambioPasswordRequerido()).isTrue();
        }

        @AfterEach
        void limpiarContextoSeguridad() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Debe cambiar la contraseña propia del usuario autenticado")
        void debeCambiarPasswordPropia() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("cajero1", "hashActual", List.of()));
            when(usuarioRepository.findByUsername("cajero1")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("actual", "hashActual")).thenReturn(true);
            when(passwordEncoder.encode("nuevaPassword")).thenReturn("hashNueva");

            usuarioService.cambiarPasswordPropia("actual", "nuevaPassword");

            assertThat(usuario.getPassword()).isEqualTo("hashNueva");
        }

        @Test
        @DisplayName("Debe rechazar cambiar la contraseña propia sin autenticación")
        void debeRechazarCambiarPasswordPropiaSinAutenticacion() {
            SecurityContextHolder.clearContext();

            assertThatThrownBy(() -> usuarioService.cambiarPasswordPropia("actual", "nuevaPassword"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no autenticado");
        }
    }

    @Nested
    @DisplayName("Estado de cuenta")
    class EstadoCuenta {

        @Test
        @DisplayName("Debe desactivar un usuario y desbloquear su cuenta")
        void debeDesactivarUsuarioYDesbloquear() {
            usuario.setCuentaBloqueada(true);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.cambiarEstadoActivo(1L, false);

            assertThat(resultado.getActivo()).isFalse();
            assertThat(resultado.getCuentaBloqueada()).isFalse();
        }

        @Test
        @DisplayName("Debe bloquear la cuenta de un usuario")
        void debeBloquearCuenta() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.cambiarBloqueo(1L, true);

            assertThat(resultado.getCuentaBloqueada()).isTrue();
        }

        @Test
        @DisplayName("Debe desbloquear la cuenta de un usuario")
        void debeDesbloquearCuenta() {
            usuario.setCuentaBloqueada(true);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.cambiarBloqueo(1L, false);

            assertThat(resultado.getCuentaBloqueada()).isFalse();
        }

        @Test
        @DisplayName("Debe asignar un nuevo rol a un usuario")
        void debeAsignarRol() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.asignarRol(1L, RolSistema.SUPERVISOR);

            assertThat(resultado.getRol()).isEqualTo(RolSistema.SUPERVISOR);
        }

        @Test
        @DisplayName("Debe bloquear la cuenta al superar el máximo de intentos fallidos")
        void debeBloquearCuentaAlSuperarIntentosFallidos() {
            usuario.setIntentosFallidos(2);
            when(usuarioRepository.findByUsername("cajero1")).thenReturn(Optional.of(usuario));

            usuarioService.registrarIntentoFallido("cajero1");

            assertThat(usuario.getIntentosFallidos()).isEqualTo(3);
            assertThat(usuario.getCuentaBloqueada()).isTrue();
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("No debe bloquear la cuenta si no se alcanzó el máximo de intentos")
        void noDebeBloquearCuentaSiNoSeAlcanzaElMaximo() {
            usuario.setIntentosFallidos(0);
            when(usuarioRepository.findByUsername("cajero1")).thenReturn(Optional.of(usuario));

            usuarioService.registrarIntentoFallido("cajero1");

            assertThat(usuario.getIntentosFallidos()).isEqualTo(1);
            assertThat(usuario.getCuentaBloqueada()).isFalse();
        }

        @Test
        @DisplayName("No debe fallar si el username no existe al registrar intento fallido")
        void noDebeFallarSiUsernameNoExisteAlRegistrarIntento() {
            when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

            usuarioService.registrarIntentoFallido("inexistente");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe resetear los intentos fallidos en un login exitoso")
        void debeResetearIntentosEnLoginExitoso() {
            usuario.setIntentosFallidos(2);
            when(usuarioRepository.findByUsername("cajero1")).thenReturn(Optional.of(usuario));

            usuarioService.registrarLoginExitoso("cajero1");

            assertThat(usuario.getIntentosFallidos()).isZero();
            assertThat(usuario.getUltimoAcceso()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Consultas y permisos")
    class Consultas {

        @Test
        @DisplayName("Debe verificar si un usuario tiene un permiso específico")
        void debeVerificarPermiso() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            boolean tienePermiso = usuarioService.tienePermiso(1L, Permiso.PAGO_ACREDITAR);

            assertThat(tienePermiso).isEqualTo(usuario.tienePermiso(Permiso.PAGO_ACREDITAR.getAuthority()));
        }

        @Test
        @DisplayName("Debe retornar false si el usuario no existe al verificar permiso")
        void debeRetornarFalseSiUsuarioNoExisteAlVerificarPermiso() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            boolean tienePermiso = usuarioService.tienePermiso(99L, Permiso.PAGO_ACREDITAR);

            assertThat(tienePermiso).isFalse();
        }

        @Test
        @DisplayName("Debe buscar usuarios por término, delegando a findAll si está vacío")
        void debeBuscarUsuariosConTerminoVacioDelegaAFindAll() {
            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            List<Usuario> resultado = usuarioService.buscarUsuarios("  ");

            assertThat(resultado).containsExactly(usuario);
        }

        @Test
        @DisplayName("Debe buscar usuarios por término no vacío")
        void debeBuscarUsuariosConTermino() {
            when(usuarioRepository.findByNombreOrApellidoContainingIgnoreCase("caj")).thenReturn(List.of(usuario));

            List<Usuario> resultado = usuarioService.buscarUsuarios("caj");

            assertThat(resultado).containsExactly(usuario);
        }

        @Test
        @DisplayName("Debe contar usuarios por rol")
        void debeContarUsuariosPorRol() {
            when(usuarioRepository.countByRol(RolSistema.CAJERO)).thenReturn(4L);

            Long resultado = usuarioService.contarUsuariosPorRol(RolSistema.CAJERO);

            assertThat(resultado).isEqualTo(4L);
        }

        @Test
        @DisplayName("Debe obtener un usuario existente por ID")
        void debeObtenerUsuarioPorId() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            Usuario resultado = usuarioService.obtenerUsuario(1L);

            assertThat(resultado).isEqualTo(usuario);
        }

        @Test
        @DisplayName("Debe lanzar excepción al obtener un usuario inexistente")
        void debeLanzarExcepcionAlObtenerUsuarioInexistente() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.obtenerUsuario(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("no encontrado");
        }
    }
}
