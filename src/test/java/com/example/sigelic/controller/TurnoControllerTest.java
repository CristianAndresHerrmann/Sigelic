package com.example.sigelic.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.sigelic.dto.request.TurnoRequestDTO;
import com.example.sigelic.dto.response.TurnoResponseDTO;
import com.example.sigelic.mapper.TurnoMapper;
import com.example.sigelic.model.*;
import com.example.sigelic.service.TurnoService;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.service.TramiteService;
import com.example.sigelic.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebMvcTest(TurnoController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de TurnoController")
class TurnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TurnoService turnoService;

    @MockitoBean
    private TitularService titularService;

    @MockitoBean
    private TramiteService tramiteService;

    @MockitoBean
    private TurnoMapper turnoMapper;

    private ObjectMapper objectMapper;

    private Turno turno;
    private TurnoRequestDTO turnoRequestDTO;
    private TurnoResponseDTO turnoResponseDTO;
    private Titular titular;
    private Tramite tramite;
    private Recurso recurso;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Configurar modelos
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");

        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setTitular(titular);

        recurso = new Recurso();
        recurso.setId(1L);
        recurso.setTipo(TipoRecurso.BOX);
        recurso.setNombre("Box 1");
        recurso.setActivo(true);

        turno = new Turno();
        turno.setId(1L);
        turno.setTitular(titular);
        turno.setTramite(tramite);
        turno.setRecurso(recurso);
        turno.setTipo(TipoTurno.DOCUMENTACION);
        turno.setInicio(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        turno.setFin(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setTipoRecurso(TipoRecurso.BOX);

        // Configurar DTOs
        turnoRequestDTO = new TurnoRequestDTO();
        turnoRequestDTO.setTitularId(1L);
        turnoRequestDTO.setTipo(TipoTurno.DOCUMENTACION);
        turnoRequestDTO.setTramiteId(1L);
        turnoRequestDTO.setRecursoId(1L);
        turnoRequestDTO.setInicio(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        turnoRequestDTO.setFin(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));

        turnoResponseDTO = new TurnoResponseDTO();
        turnoResponseDTO.setId(1L);
        turnoResponseDTO.setTipo(TipoTurno.DOCUMENTACION);
        turnoResponseDTO.setTramiteId(1L);
        turnoResponseDTO.setInicio(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        turnoResponseDTO.setFin(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));
        turnoResponseDTO.setEstado(EstadoTurno.RESERVADO);
        turnoResponseDTO.setTitularNombre("Juan Pérez");
        turnoResponseDTO.setTitularDni("12345678");
        turnoResponseDTO.setTipoRecurso(TipoRecurso.BOX);
        turnoResponseDTO.setRecursoNombre("Box 1");
    }

    @Nested
    @DisplayName("Obtener turnos")
    class ObtenerTurnos {

        @Test
        @DisplayName("Debe obtener turno por id exitosamente")
        void debeObtenerTurnoPorIdExitosamente() throws Exception {
            // Given
            when(turnoService.findById(1L)).thenReturn(Optional.of(turno));
            when(turnoMapper.toResponseDTOWithDetails(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/turnos/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.estado", is("RESERVADO")))
                    .andExpect(jsonPath("$.tipo", is("DOCUMENTACION")))
                    .andExpect(jsonPath("$.tipoRecurso", is("BOX")));

            verify(turnoService).findById(1L);
            verify(turnoMapper).toResponseDTOWithDetails(turno);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando turno no existe")
        void debeRetornar404CuandoTurnoNoExiste() throws Exception {
            // Given
            when(turnoService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/turnos/999"))
                    .andExpect(status().isNotFound());

            verify(turnoService).findById(999L);
            verify(turnoMapper, never()).toResponseDTOWithDetails(any());
        }

        @Test
        @DisplayName("Debe obtener turnos por titular")
        void debeObtenerTurnosPorTitular() throws Exception {
            // Given
            List<Turno> turnos = Arrays.asList(turno);
            List<TurnoResponseDTO> turnoResponseDTOs = Arrays.asList(turnoResponseDTO);

            when(turnoService.findByTitular(1L)).thenReturn(turnos);
            when(turnoMapper.toResponseDTOList(turnos)).thenReturn(turnoResponseDTOs);

            // When & Then
            mockMvc.perform(get("/api/turnos/titular/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(1)));

            verify(turnoService).findByTitular(1L);
            verify(turnoMapper).toResponseDTOList(turnos);
        }

        @Test
        @DisplayName("Debe obtener turnos por fecha")
        void debeObtenerTurnosPorFecha() throws Exception {
            // Given
            LocalDateTime fechaDesde = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime fechaHasta = LocalDateTime.of(2024, 1, 31, 23, 59);
            List<Turno> turnos = Arrays.asList(turno);
            List<TurnoResponseDTO> turnoResponseDTOs = Arrays.asList(turnoResponseDTO);

            when(turnoService.findTurnosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(turnos);
            when(turnoMapper.toResponseDTOList(turnos)).thenReturn(turnoResponseDTOs);

            // When & Then
            mockMvc.perform(get("/api/turnos/fecha")
                            .param("fechaDesde", "2024-01-01T00:00:00")
                            .param("fechaHasta", "2024-01-31T23:59:00"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(turnoService).findTurnosEnPeriodo(fechaDesde, fechaHasta);
            verify(turnoMapper).toResponseDTOList(turnos);
        }

        @Test
        @DisplayName("Debe obtener horarios disponibles")
        void debeObtenerHorariosDisponibles() throws Exception {
            // Given
            LocalDateTime fechaDesde = LocalDateTime.of(2024, 1, 1, 8, 0);
            LocalDateTime fechaHasta = LocalDateTime.of(2024, 1, 1, 18, 0);
            List<LocalDateTime> horariosDisponibles = Arrays.asList(
                    LocalDateTime.of(2024, 1, 1, 10, 0),
                    LocalDateTime.of(2024, 1, 1, 11, 0)
            );

            when(turnoService.getHorariosDisponibles(TipoRecurso.BOX, fechaDesde, fechaHasta, 30))
                    .thenReturn(horariosDisponibles);

            // When & Then
            mockMvc.perform(get("/api/turnos/disponibles")
                            .param("tipoRecurso", "BOX")
                            .param("fechaDesde", "2024-01-01T08:00:00")
                            .param("fechaHasta", "2024-01-01T18:00:00")
                            .param("duracionMinutos", "30"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(turnoService).getHorariosDisponibles(TipoRecurso.BOX, fechaDesde, fechaHasta, 30);
        }

        @Test
        @DisplayName("Debe obtener próximos turnos de titular")
        void debeObtenerProximosTurnosDeTitular() throws Exception {
            // Given
            List<Turno> turnos = Arrays.asList(turno);
            List<TurnoResponseDTO> turnoResponseDTOs = Arrays.asList(turnoResponseDTO);

            when(turnoService.getProximosTurnos(1L)).thenReturn(turnos);
            when(turnoMapper.toResponseDTOList(turnos)).thenReturn(turnoResponseDTOs);

            // When & Then
            mockMvc.perform(get("/api/turnos/titular/1/proximos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(turnoService).getProximosTurnos(1L);
            verify(turnoMapper).toResponseDTOList(turnos);
        }
    }

    @Nested
    @DisplayName("Crear turnos")
    class CrearTurnos {

        @Test
        @DisplayName("Debe crear turno exitosamente")
        void debeCrearTurnoExitosamente() throws Exception {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(tramiteService.findById(1L)).thenReturn(Optional.of(tramite));
            when(turnoService.reservarTurno(
                    eq(1L),
                    eq(TipoTurno.DOCUMENTACION),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    eq(1L),
                    eq(1L)
            )).thenReturn(turno);
            when(turnoMapper.toResponseDTO(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/turnos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(turnoRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.estado", is("RESERVADO")));

            verify(titularService).findById(1L);
            verify(tramiteService).findById(1L);
            verify(turnoService).reservarTurno(
                    eq(1L),
                    eq(TipoTurno.DOCUMENTACION),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    eq(1L),
                    eq(1L)
            );
            verify(turnoMapper).toResponseDTO(turno);
        }

        @Test
        @DisplayName("Debe crear turno exitosamente sin trámite")
        void debeCrearTurnoExitosamenteSinTramite() throws Exception {
            // Given
            turnoRequestDTO.setTramiteId(null);
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(turnoService.reservarTurno(
                    eq(1L),
                    eq(TipoTurno.DOCUMENTACION),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    eq(1L),
                    eq(null)
            )).thenReturn(turno);
            when(turnoMapper.toResponseDTO(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/turnos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(turnoRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)));

            verify(titularService).findById(1L);
            verify(tramiteService, never()).findById(any());
            verify(turnoService).reservarTurno(
                    eq(1L),
                    eq(TipoTurno.DOCUMENTACION),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    eq(1L),
                    eq(null)
            );
        }

        @Test
        @DisplayName("Debe retornar 400 cuando trámite no existe")
        void debeRetornar400CuandoTramiteNoExiste() throws Exception {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(tramiteService.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(post("/api/turnos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(turnoRequestDTO)))
                    .andExpect(status().isBadRequest());

            verify(titularService).findById(1L);
            verify(tramiteService).findById(1L);
            verify(turnoService, never()).reservarTurno(anyLong(), any(), any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("Debe manejar errores de validación")
        void debeManejarErroresDeValidacion() throws Exception {
            // Given
            turnoRequestDTO.setTitularId(null); // Forzar error de validación

            // When & Then
            mockMvc.perform(post("/api/turnos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(turnoRequestDTO)))
                    .andExpect(status().isBadRequest());

            verify(turnoService, never()).reservarTurno(anyLong(), any(TipoTurno.class), 
                    any(LocalDateTime.class), any(LocalDateTime.class), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Actualizar turnos")
    class ActualizarTurnos {

        @Test
        @DisplayName("Debe actualizar turno exitosamente")
        void debeActualizarTurnoExitosamente() throws Exception {
            // Given
            when(turnoService.findById(1L)).thenReturn(Optional.of(turno));
            doNothing().when(turnoMapper).updateEntity(turno, turnoRequestDTO);
            when(turnoMapper.toResponseDTO(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(put("/api/turnos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(turnoRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)));

            verify(turnoService).findById(1L);
            verify(turnoMapper).updateEntity(turno, turnoRequestDTO);
            verify(turnoMapper).toResponseDTO(turno);
        }

        @Test
        @DisplayName("Debe retornar 404 al actualizar turno inexistente")
        void debeRetornar404AlActualizarTurnoInexistente() throws Exception {
            // Given
            when(turnoService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(put("/api/turnos/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(turnoRequestDTO)))
                    .andExpect(status().isNotFound());

            verify(turnoService).findById(999L);
        }

        @Test
        @DisplayName("Debe eliminar turno exitosamente")
        void debeEliminarTurnoExitosamente() throws Exception {
            // Given
            when(turnoService.findById(1L)).thenReturn(Optional.of(turno));
            when(turnoService.cancelarTurno(1L, "Eliminado por el usuario")).thenReturn(turno);

            // When & Then
            mockMvc.perform(delete("/api/turnos/1"))
                    .andExpect(status().isNoContent());

            verify(turnoService).findById(1L);
            verify(turnoService).cancelarTurno(1L, "Eliminado por el usuario");
        }

        @Test
        @DisplayName("Debe retornar 404 al eliminar turno inexistente")
        void debeRetornar404AlEliminarTurnoInexistente() throws Exception {
            // Given
            when(turnoService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(delete("/api/turnos/999"))
                    .andExpect(status().isNotFound());

            verify(turnoService).findById(999L);
        }
    }

    @Nested
    @DisplayName("Gestión de estados")
    class GestionEstados {

        @Test
        @DisplayName("Debe confirmar turno exitosamente")
        void debeConfirmarTurnoExitosamente() throws Exception {
            // Given
            turno.setEstado(EstadoTurno.CONFIRMADO);
            when(turnoService.confirmarTurno(1L)).thenReturn(turno);
            when(turnoMapper.toResponseDTO(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/turnos/1/confirmar"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)));

            verify(turnoService).confirmarTurno(1L);
            verify(turnoMapper).toResponseDTO(turno);
        }

        @Test
        @DisplayName("Debe retornar 404 al confirmar turno inexistente")
        void debeRetornar404AlConfirmarTurnoInexistente() throws Exception {
            // Given
            when(turnoService.confirmarTurno(999L)).thenThrow(new RuntimeException("Turno no encontrado"));

            // When & Then
            mockMvc.perform(patch("/api/turnos/999/confirmar"))
                    .andExpect(status().isBadRequest());

            verify(turnoService).confirmarTurno(999L);
        }

        @Test
        @DisplayName("Debe cancelar turno exitosamente")
        void debeCancelarTurnoExitosamente() throws Exception {
            // Given
            turno.setEstado(EstadoTurno.CANCELADO);
            when(turnoService.cancelarTurno(1L, "Motivo de cancelación")).thenReturn(turno);
            when(turnoMapper.toResponseDTO(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/turnos/1/cancelar")
                            .param("motivo", "Motivo de cancelación"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)));

            verify(turnoService).cancelarTurno(1L, "Motivo de cancelación");
            verify(turnoMapper).toResponseDTO(turno);
        }

        @Test
        @DisplayName("Debe cancelar turno sin motivo")
        void debeCancelarTurnoSinMotivo() throws Exception {
            // Given
            turno.setEstado(EstadoTurno.CANCELADO);
            when(turnoService.cancelarTurno(1L, null)).thenReturn(turno);
            when(turnoMapper.toResponseDTO(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/turnos/1/cancelar"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)));

            verify(turnoService).cancelarTurno(1L, null);
            verify(turnoMapper).toResponseDTO(turno);
        }

        @Test
        @DisplayName("Debe marcar turno como ausente exitosamente")
        void debeMarcarTurnoComoAusenteExitosamente() throws Exception {
            // Given
            turno.setEstado(EstadoTurno.AUSENTE);
            when(turnoService.marcarAusente(1L)).thenReturn(turno);
            when(turnoMapper.toResponseDTO(turno)).thenReturn(turnoResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/turnos/1/ausente"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)));

            verify(turnoService).marcarAusente(1L);
            verify(turnoMapper).toResponseDTO(turno);
        }

        @Test
        @DisplayName("Debe retornar 404 al marcar ausente turno inexistente")
        void debeRetornar404AlMarcarAusenteTurnoInexistente() throws Exception {
            // Given
            when(turnoService.marcarAusente(999L)).thenThrow(new RuntimeException("Turno no encontrado"));

            // When & Then
            mockMvc.perform(patch("/api/turnos/999/ausente"))
                    .andExpect(status().isBadRequest());

            verify(turnoService).marcarAusente(999L);
        }

        @Test
        @DisplayName("Debe manejar errores al confirmar turno")
        void debeManejarErroresAlConfirmarTurno() throws Exception {
            // Given
            when(turnoService.confirmarTurno(1L)).thenThrow(new IllegalStateException("Error"));

            // When & Then
            mockMvc.perform(patch("/api/turnos/1/confirmar"))
                    .andExpect(status().isBadRequest());

            verify(turnoService).confirmarTurno(1L);
            verify(turnoMapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Debe manejar errores al cancelar turno")
        void debeManejarErroresAlCancelarTurno() throws Exception {
            // Given
            when(turnoService.cancelarTurno(1L, "motivo")).thenThrow(new IllegalStateException("Error"));

            // When & Then
            mockMvc.perform(patch("/api/turnos/1/cancelar")
                            .param("motivo", "motivo"))
                    .andExpect(status().isBadRequest());

            verify(turnoService).cancelarTurno(1L, "motivo");
            verify(turnoMapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Debe manejar errores al marcar ausente")
        void debeManejarErroresAlMarcarAusente() throws Exception {
            // Given
            when(turnoService.marcarAusente(1L)).thenThrow(new IllegalStateException("Error"));

            // When & Then
            mockMvc.perform(patch("/api/turnos/1/ausente"))
                    .andExpect(status().isBadRequest());

            verify(turnoService).marcarAusente(1L);
            verify(turnoMapper, never()).toResponseDTO(any());
        }
    }
}
