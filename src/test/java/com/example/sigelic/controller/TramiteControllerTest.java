package com.example.sigelic.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.sigelic.config.TestSecurityConfig;
import com.example.sigelic.dto.request.TramiteRequestDTO;
import com.example.sigelic.dto.response.TramiteResponseDTO;
import com.example.sigelic.mapper.TramiteMapper;
import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TramiteController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de TramiteController")
class TramiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TramiteService tramiteService;

    @MockitoBean
    private TramiteMapper tramiteMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Tramite tramite;
    private TramiteRequestDTO tramiteRequestDTO;
    private TramiteResponseDTO tramiteResponseDTO;
    private Titular titular;

    @BeforeEach
    void setUp() {
        // Crear titular
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");
        titular.setFechaNacimiento(LocalDate.now().minusYears(25));

        // Crear trámite
        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTitular(titular);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setClaseSolicitada(ClaseLicencia.B);
        tramite.setEstado(EstadoTramite.INICIADO);
        tramite.setFechaCreacion(LocalDateTime.now());
        tramite.setFechaActualizacion(LocalDateTime.now());

        // Crear DTO de request
        tramiteRequestDTO = new TramiteRequestDTO();
        tramiteRequestDTO.setTitularId(1L);
        tramiteRequestDTO.setTipo(TipoTramite.EMISION);
        tramiteRequestDTO.setClaseSolicitada(ClaseLicencia.B);

        // Crear DTO de response
        tramiteResponseDTO = new TramiteResponseDTO();
        tramiteResponseDTO.setId(1L);
        tramiteResponseDTO.setTipo(TipoTramite.EMISION);
        tramiteResponseDTO.setClaseSolicitada(ClaseLicencia.B);
        tramiteResponseDTO.setEstado(EstadoTramite.INICIADO);
        tramiteResponseDTO.setFechaCreacion(LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/tramites/{id}")
    class ObtenerTramitePorId {

        @Test
        @DisplayName("Debe retornar trámite por ID exitosamente")
        void debeRetornarTramitePorIdExitosamente() throws Exception {
            // Given
            when(tramiteService.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteMapper.toResponseDTOWithDetails(tramite)).thenReturn(tramiteResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/tramites/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.tipo").value("EMISION"))
                    .andExpect(jsonPath("$.claseSolicitada").value("B"))
                    .andExpect(jsonPath("$.estado").value("INICIADO"));

            verify(tramiteService).findById(1L);
            verify(tramiteMapper).toResponseDTOWithDetails(tramite);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando trámite no existe")
        void debeRetornar404CuandoTramiteNoExiste() throws Exception {
            // Given
            when(tramiteService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/tramites/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(tramiteService).findById(999L);
            verify(tramiteMapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("POST /api/tramites")
    class IniciarTramite {

        @Test
        @DisplayName("Debe iniciar trámite exitosamente")
        void debeIniciarTramiteExitosamente() throws Exception {
            // Given
            when(tramiteService.iniciarTramite(1L, TipoTramite.EMISION, ClaseLicencia.B))
                    .thenReturn(tramite);
            when(tramiteMapper.toResponseDTO(tramite)).thenReturn(tramiteResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/tramites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tramiteRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.tipo").value("EMISION"))
                    .andExpect(jsonPath("$.claseSolicitada").value("B"))
                    .andExpect(jsonPath("$.estado").value("INICIADO"));

            verify(tramiteService).iniciarTramite(1L, TipoTramite.EMISION, ClaseLicencia.B);
            verify(tramiteMapper).toResponseDTO(tramite);
        }

        @Test
        @DisplayName("Debe retornar 400 con datos inválidos")
        void debeRetornar400ConDatosInvalidos() throws Exception {
            // Given
            TramiteRequestDTO invalidDTO = new TramiteRequestDTO();
            // DTO sin datos requeridos

            // When & Then
            mockMvc.perform(post("/api/tramites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());

            verify(tramiteService, never()).iniciarTramite(any(), any(), any());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando titular no puede iniciar trámite")
        void debeRetornar400CuandoTitularNoPuedeIniciarTramite() throws Exception {
            // Given
            when(tramiteService.iniciarTramite(1L, TipoTramite.EMISION, ClaseLicencia.B))
                    .thenThrow(new IllegalArgumentException("Titular no cumple requisitos de edad"));

            // When & Then
            mockMvc.perform(post("/api/tramites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tramiteRequestDTO)))
                    .andExpect(status().isBadRequest());

            verify(tramiteService).iniciarTramite(1L, TipoTramite.EMISION, ClaseLicencia.B);
        }
    }

    @Nested
    @DisplayName("PATCH /api/tramites/{id}/validar-documentacion")
    class ValidarDocumentacion {

        @Test
        @DisplayName("Debe validar documentación exitosamente")
        void debeValidarDocumentacionExitosamente() throws Exception {
            // Given
            tramite.setDocumentacionValidada(true);
            tramite.setEstado(EstadoTramite.DOCS_OK);
            tramite.setAgenteResponsable("Agente Test");
            
            tramiteResponseDTO.setEstado(EstadoTramite.DOCS_OK);
            
            when(tramiteService.validarDocumentacion(1L, "Agente Test")).thenReturn(tramite);
            when(tramiteMapper.toResponseDTO(tramite)).thenReturn(tramiteResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/tramites/1/validar-documentacion")
                    .param("agente", "Agente Test")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.estado").value("DOCS_OK"));

            verify(tramiteService).validarDocumentacion(1L, "Agente Test");
            verify(tramiteMapper).toResponseDTO(tramite);
        }

        @Test
        @DisplayName("Debe retornar 400 cuando trámite no existe")
        void debeRetornar400CuandoTramiteNoExiste() throws Exception {
            // Given
            when(tramiteService.validarDocumentacion(999L, "Agente Test"))
                    .thenThrow(new IllegalArgumentException("Trámite no encontrado"));

            // When & Then
            mockMvc.perform(patch("/api/tramites/999/validar-documentacion")
                    .param("agente", "Agente Test")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Trámite no encontrado"));
        }
    }

    @Nested
    @DisplayName("GET /api/tramites/titular/{titularId}")
    class ObtenerTramitesPorTitular {

        @Test
        @DisplayName("Debe retornar trámites del titular exitosamente")
        void debeRetornarTramitesDelTitularExitosamente() throws Exception {
            // Given
            List<Tramite> tramites = Arrays.asList(tramite);
            List<TramiteResponseDTO> dtos = Arrays.asList(tramiteResponseDTO);
            
            when(tramiteService.findByTitular(1L)).thenReturn(tramites);
            when(tramiteMapper.toResponseDTOList(tramites)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/tramites/titular/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(tramiteService).findByTitular(1L);
            verify(tramiteMapper).toResponseDTOList(tramites);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando titular no tiene trámites")
        void debeRetornarListaVaciaCuandoTitularNoTieneTramites() throws Exception {
            // Given
            when(tramiteService.findByTitular(1L)).thenReturn(Arrays.asList());
            when(tramiteMapper.toResponseDTOList(any())).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/tramites/titular/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/tramites/buscar")
    class BuscarTramites {

        @Test
        @DisplayName("Debe buscar trámites por estado exitosamente")
        void debeBuscarTramitesPorEstadoExitosamente() throws Exception {
            // Given
            List<Tramite> tramites = Arrays.asList(tramite);
            List<TramiteResponseDTO> dtos = Arrays.asList(tramiteResponseDTO);
            
            when(tramiteService.findByEstado(EstadoTramite.INICIADO)).thenReturn(tramites);
            when(tramiteMapper.toResponseDTOList(tramites)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/tramites/estado/INICIADO")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].estado").value("INICIADO"));

            verify(tramiteService).findByEstado(EstadoTramite.INICIADO);
            verify(tramiteMapper).toResponseDTOList(tramites);
        }
    }
}
