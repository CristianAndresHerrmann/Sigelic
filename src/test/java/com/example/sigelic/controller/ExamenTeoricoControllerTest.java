package com.example.sigelic.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.sigelic.config.TestSecurityConfig;
import com.example.sigelic.dto.request.ExamenTeoricoRequestDTO;
import com.example.sigelic.dto.response.ExamenTeoricoResponseDTO;
import com.example.sigelic.dto.response.TramiteResponseDTO;
import com.example.sigelic.mapper.ExamenTeoricoMapper;
import com.example.sigelic.mapper.TramiteMapper;
import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.ExamenService;
import com.example.sigelic.service.TramiteService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests para ExamenTeoricoController
 */
@WebMvcTest(ExamenTeoricoController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de ExamenTeoricoController")
class ExamenTeoricoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TramiteService tramiteService;

    @MockBean
    private ExamenService examenService;

    @MockBean
    private ExamenTeoricoMapper examenTeoricoMapper;

    @MockBean
    private TramiteMapper tramiteMapper;

    private Tramite tramite;
    private ExamenTeorico examenTeorico;
    private ExamenTeoricoRequestDTO examenRequestDTO;
    private ExamenTeoricoResponseDTO examenResponseDTO;
    private TramiteResponseDTO tramiteResponseDTO;

    @BeforeEach
    void setUp() {
        // Crear titular de prueba
        Titular titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");

        // Crear trámite de prueba
        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setClaseSolicitada(ClaseLicencia.B);
        tramite.setEstado(EstadoTramite.APTO_MED);
        tramite.setTitular(titular);

        // Crear examen teórico de prueba
        examenTeorico = new ExamenTeorico();
        examenTeorico.setId(1L);
        examenTeorico.setFecha(LocalDateTime.now());
        examenTeorico.setCantidadPreguntas(30);
        examenTeorico.setRespuestasCorrectas(25);
        examenTeorico.setExaminador("Dr. García");
        examenTeorico.setObservaciones("Buen desempeño");
        examenTeorico.setTramite(tramite);

        // Crear DTOs de prueba
        examenRequestDTO = new ExamenTeoricoRequestDTO();
        examenRequestDTO.setTramiteId(1L);
        examenRequestDTO.setFecha(LocalDateTime.now());
        examenRequestDTO.setCantidadPreguntas(30);
        examenRequestDTO.setRespuestasCorrectas(25);
        examenRequestDTO.setExaminador("Dr. García");
        examenRequestDTO.setObservaciones("Buen desempeño");

        examenResponseDTO = new ExamenTeoricoResponseDTO();
        examenResponseDTO.setId(1L);
        examenResponseDTO.setTramiteId(1L);
        examenResponseDTO.setFecha(LocalDateTime.now());
        examenResponseDTO.setCantidadPreguntas(30);
        examenResponseDTO.setRespuestasCorrectas(25);
        examenResponseDTO.setPuntaje(83);
        examenResponseDTO.setAprobado(true);
        examenResponseDTO.setExaminador("Dr. García");
        examenResponseDTO.setObservaciones("Buen desempeño");
        examenResponseDTO.setTitularNombre("Juan");
        examenResponseDTO.setTitularApellido("Pérez");
        examenResponseDTO.setTitularDni("12345678");

        tramiteResponseDTO = new TramiteResponseDTO();
        tramiteResponseDTO.setId(1L);
        tramiteResponseDTO.setTipo(TipoTramite.EMISION);
        tramiteResponseDTO.setClaseSolicitada(ClaseLicencia.B);
        tramiteResponseDTO.setEstado(EstadoTramite.EX_TEO_OK);
    }

    @Nested
    @DisplayName("POST /api/examenes-teoricos/tramite/{tramiteId}")
    class RegistrarExamenTeorico {

        @Test
        @DisplayName("Debe registrar examen teórico exitosamente")
        void debeRegistrarExamenTeoricoExitosamente() throws Exception {
            // Given
            tramite.setEstado(EstadoTramite.EX_TEO_OK);
            
            when(examenTeoricoMapper.toEntity(any(ExamenTeoricoRequestDTO.class))).thenReturn(examenTeorico);
            when(tramiteService.registrarExamenTeorico(eq(1L), any(ExamenTeorico.class))).thenReturn(tramite);
            when(tramiteMapper.toResponseDTO(tramite)).thenReturn(tramiteResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/examenes-teoricos/tramite/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(examenRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.estado", is("EX_TEO_OK")));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando hay error en el servicio")
        void debeRetornar400CuandoHayErrorEnElServicio() throws Exception {
            // Given
            when(examenTeoricoMapper.toEntity(any(ExamenTeoricoRequestDTO.class))).thenReturn(examenTeorico);
            when(tramiteService.registrarExamenTeorico(eq(1L), any(ExamenTeorico.class)))
                    .thenThrow(new RuntimeException("Error en el servicio"));

            // When & Then
            mockMvc.perform(post("/api/examenes-teoricos/tramite/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(examenRequestDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/examenes-teoricos/tramite/{tramiteId}/rechazar")
    class RechazarExamenTeorico {

        @Test
        @DisplayName("Debe rechazar examen teórico exitosamente")
        void debeRechazarExamenTeoricoExitosamente() throws Exception {
            // Given
            tramite.setEstado(EstadoTramite.EX_TEO_RECHAZADO);
            tramiteResponseDTO.setEstado(EstadoTramite.EX_TEO_RECHAZADO);
            
            when(tramiteService.rechazarExamenTeorico(1L, "No cumple requisitos")).thenReturn(tramite);
            when(tramiteMapper.toResponseDTO(tramite)).thenReturn(tramiteResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/examenes-teoricos/tramite/1/rechazar")
                    .param("motivo", "No cumple requisitos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.estado", is("EX_TEO_RECHAZADO")));
        }
    }

    @Nested
    @DisplayName("GET /api/examenes-teoricos")
    class ObtenerTodosLosExamenes {

        @Test
        @DisplayName("Debe retornar todos los exámenes teóricos")
        void debeRetornarTodosLosExamenesTeóricos() throws Exception {
            // Given
            List<ExamenTeorico> examenes = Arrays.asList(examenTeorico);
            List<ExamenTeoricoResponseDTO> dtos = Arrays.asList(examenResponseDTO);
            
            when(examenService.findAllTeoricos()).thenReturn(examenes);
            when(examenTeoricoMapper.toResponseDTOList(examenes)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/examenes-teoricos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].aprobado", is(true)));
        }
    }

    @Nested
    @DisplayName("GET /api/examenes-teoricos/{id}")
    class ObtenerExamenPorId {

        @Test
        @DisplayName("Debe retornar examen teórico por ID exitosamente")
        void debeRetornarExamenTeoricoPorIdExitosamente() throws Exception {
            // Given
            when(examenService.findExamenTeoricoById(1L)).thenReturn(Optional.of(examenTeorico));
            when(examenTeoricoMapper.toResponseDTO(examenTeorico)).thenReturn(examenResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/examenes-teoricos/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.examinador", is("Dr. García")));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando examen no existe")
        void debeRetornar404CuandoExamenNoExiste() throws Exception {
            // Given
            when(examenService.findExamenTeoricoById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/examenes-teoricos/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
