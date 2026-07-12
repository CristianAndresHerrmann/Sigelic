package com.example.sigelic.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.example.sigelic.config.TestSecurityConfig;
import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;
import com.example.sigelic.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Authentication principal;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero1");
        usuario.setEmail("cajero1@sigelic.gov.ar");
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setRol(RolSistema.CAJERO);
        usuario.setActivo(true);
        usuario.setCuentaBloqueada(false);

        principal = new UsernamePasswordAuthenticationToken("admin", "N/A", java.util.List.of());
    }

    @Test
    @DisplayName("Debe crear un usuario exitosamente")
    void debeCrearUsuarioExitosamente() throws Exception {
        when(usuarioService.crearUsuario(anyString(), anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(), any(RolSistema.class), eq(true), anyString())).thenReturn(usuario);

        String body = """
                {"username":"cajero1","password":"password123","email":"cajero1@sigelic.gov.ar",
                "nombre":"Juan","apellido":"Pérez","rol":"CAJERO","cambioPasswordRequerido":true}
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(principal))
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("cajero1"));
    }

    @Test
    @DisplayName("Debe listar usuarios paginados sin filtros")
    void debeListarUsuariosSinFiltros() throws Exception {
        Page<Usuario> pagina = new PageImpl<>(List.of(usuario));
        when(usuarioService.obtenerTodosLosUsuarios(any())).thenReturn(pagina);

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("cajero1"));
    }

    @Test
    @DisplayName("Debe listar usuarios filtrando por búsqueda")
    void debeListarUsuariosConBusqueda() throws Exception {
        Page<Usuario> pagina = new PageImpl<>(List.of(usuario));
        when(usuarioService.buscarUsuarios(eq("cajero"), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/usuarios").param("busqueda", "cajero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("cajero1"));
    }

    @Test
    @DisplayName("Debe listar usuarios filtrando por rol")
    void debeListarUsuariosConRol() throws Exception {
        Page<Usuario> pagina = new PageImpl<>(List.of(usuario));
        when(usuarioService.obtenerUsuariosPorRol(eq(RolSistema.CAJERO), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/usuarios").param("rol", "CAJERO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe listar usuarios filtrando por estado activo")
    void debeListarUsuariosConEstadoActivo() throws Exception {
        Page<Usuario> pagina = new PageImpl<>(List.of(usuario));
        when(usuarioService.obtenerUsuariosPorEstado(eq(true), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/usuarios").param("activo", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe obtener un usuario por ID")
    void debeObtenerUsuarioPorId() throws Exception {
        when(usuarioService.obtenerUsuario(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("cajero1"));
    }

    @Test
    @DisplayName("Debe obtener el perfil del usuario autenticado")
    void debeObtenerPerfil() throws Exception {
        when(usuarioService.obtenerUsuarioPorUsername("cajero1")).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/perfil")
                        .with(authentication(new UsernamePasswordAuthenticationToken("cajero1", "N/A", java.util.List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("cajero1"));
    }

    @Test
    @DisplayName("Debe actualizar un usuario sin cambiar estado ni bloqueo")
    void debeActualizarUsuarioSinCambiosDeEstado() throws Exception {
        when(usuarioService.actualizarUsuario(any(), any(), any(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(usuario);

        String body = """
                {"email":"nuevo@sigelic.gov.ar","nombre":"Juan","apellido":"Pérez","rol":"CAJERO"}
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(principal))
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe actualizar un usuario activándolo y bloqueando la cuenta")
    void debeActualizarUsuarioActivandoYBloqueando() throws Exception {
        when(usuarioService.actualizarUsuario(any(), any(), any(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(usuario);
        when(usuarioService.activarUsuario(eq(1L), anyString())).thenReturn(usuario);
        when(usuarioService.bloquearCuenta(eq(1L), anyString())).thenReturn(usuario);

        String body = """
                {"email":"nuevo@sigelic.gov.ar","nombre":"Juan","apellido":"Pérez","rol":"CAJERO",
                "activo":true,"cuentaBloqueada":true}
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(principal))
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe eliminar un usuario")
    void debeEliminarUsuario() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1").with(authentication(principal)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Debe cambiar la contraseña propia cuando la confirmación coincide")
    void debeCambiarPasswordPropia() throws Exception {
        String body = """
                {"passwordActual":"actual123","nuevaPassword":"nuevaPassword1","confirmarPassword":"nuevaPassword1"}
                """;

        mockMvc.perform(post("/api/usuarios/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(new UsernamePasswordAuthenticationToken("cajero1", "N/A", java.util.List.of())))
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe rechazar el cambio de contraseña si la confirmación no coincide")
    void debeRechazarCambioPasswordSiNoCoincide() throws Exception {
        String body = """
                {"passwordActual":"actual123","nuevaPassword":"nuevaPassword1","confirmarPassword":"otraDistinta"}
                """;

        mockMvc.perform(post("/api/usuarios/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(new UsernamePasswordAuthenticationToken("cajero1", "N/A", java.util.List.of())))
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe resetear la contraseña de un usuario")
    void debeResetearPassword() throws Exception {
        when(usuarioService.resetearPassword(eq(1L), anyString())).thenReturn("Temp1234");

        mockMvc.perform(post("/api/usuarios/1/resetear-password").with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("Temp1234"));
    }

    @Test
    @DisplayName("Debe activar un usuario")
    void debeActivarUsuario() throws Exception {
        when(usuarioService.activarUsuario(eq(1L), anyString())).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/1/activar").with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe desactivar un usuario")
    void debeDesactivarUsuario() throws Exception {
        when(usuarioService.desactivarUsuario(eq(1L), anyString())).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/1/desactivar").with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe bloquear la cuenta de un usuario")
    void debeBloquearCuentaUsuario() throws Exception {
        when(usuarioService.bloquearCuenta(eq(1L), anyString())).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/1/bloquear").with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe desbloquear la cuenta de un usuario")
    void debeDesbloquearCuentaUsuario() throws Exception {
        when(usuarioService.desbloquearCuenta(eq(1L), anyString())).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/1/desbloquear").with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe obtener la lista de roles disponibles")
    void debeObtenerRoles() throws Exception {
        mockMvc.perform(get("/api/usuarios/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasItem("CAJERO")));
    }

    @Test
    @DisplayName("Debe obtener los permisos de un rol")
    void debeObtenerPermisosDeRol() throws Exception {
        mockMvc.perform(get("/api/usuarios/roles/CAJERO/permisos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe obtener usuarios inactivos")
    void debeObtenerUsuariosInactivos() throws Exception {
        when(usuarioService.obtenerUsuariosInactivos(anyInt())).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/inactivos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("cajero1"));
    }
}
