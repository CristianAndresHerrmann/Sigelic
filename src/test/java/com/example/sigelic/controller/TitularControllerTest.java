package com.example.sigelic.controller;

import com.example.sigelic.config.TestSecurityConfig;
import com.example.sigelic.dto.request.TitularRequestDTO;
import com.example.sigelic.dto.response.TitularResponseDTO;
import com.example.sigelic.mapper.TitularMapper;
import com.example.sigelic.model.Titular;
import com.example.sigelic.service.TitularService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TitularController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de TitularController")
class TitularControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TitularService titularService;

    @MockitoBean
    private TitularMapper titularMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Titular titular;
    private TitularRequestDTO titularRequestDTO;
    private TitularResponseDTO titularResponseDTO;

    @BeforeEach
    void setUp() {
        // Crear titular
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");
        titular.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        titular.setDomicilio("Calle Falsa 123");
        titular.setEmail("juan@example.com");
        titular.setTelefono("123456789");

        // Crear DTO de request
        titularRequestDTO = new TitularRequestDTO();
        titularRequestDTO.setNombre("Juan");
        titularRequestDTO.setApellido("Pérez");
        titularRequestDTO.setDni("12345678");
        titularRequestDTO.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        titularRequestDTO.setDomicilio("Calle Falsa 123");
        titularRequestDTO.setEmail("juan@example.com");
        titularRequestDTO.setTelefono("123456789");

        // Crear DTO de response
        titularResponseDTO = new TitularResponseDTO();
        titularResponseDTO.setId(1L);
        titularResponseDTO.setNombre("Juan");
        titularResponseDTO.setApellido("Pérez");
        titularResponseDTO.setDni("12345678");
        titularResponseDTO.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        titularResponseDTO.setDomicilio("Calle Falsa 123");
        titularResponseDTO.setEmail("juan@example.com");
        titularResponseDTO.setTelefono("123456789");
    }

    @Nested
    @DisplayName("GET /api/titulares")
    class ObtenerTitulares {

        @Test
        @DisplayName("Debe retornar lista de titulares exitosamente")
        void debeRetornarListaDeTitularesExitosamente() throws Exception {
            // Given
            List<Titular> titulares = Arrays.asList(titular);
            List<TitularResponseDTO> dtos = Arrays.asList(titularResponseDTO);
            
            when(titularService.findAll()).thenReturn(titulares);
            when(titularMapper.toResponseDTOList(titulares)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/titulares")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].nombre").value("Juan"))
                    .andExpect(jsonPath("$[0].apellido").value("Pérez"));

            verify(titularService).findAll();
            verify(titularMapper).toResponseDTOList(titulares);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay titulares")
        void debeRetornarListaVaciaCuandoNoHayTitulares() throws Exception {
            // Given
            when(titularService.findAll()).thenReturn(Arrays.asList());
            when(titularMapper.toResponseDTOList(any())).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/titulares")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(titularService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/titulares/{id}")
    class ObtenerTitularPorId {

        @Test
        @DisplayName("Debe retornar titular por ID exitosamente")
        void debeRetornarTitularPorIdExitosamente() throws Exception {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(titularMapper.toResponseDTOWithDetails(titular)).thenReturn(titularResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/titulares/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.apellido").value("Pérez"))
                    .andExpect(jsonPath("$.dni").value("12345678"));

            verify(titularService).findById(1L);
            verify(titularMapper).toResponseDTOWithDetails(titular);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando titular no existe")
        void debeRetornar404CuandoTitularNoExiste() throws Exception {
            // Given
            when(titularService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/titulares/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(titularService).findById(999L);
            verifyNoInteractions(titularMapper);
        }
    }

    @Nested
    @DisplayName("POST /api/titulares")
    class CrearTitular {

        @Test
        @DisplayName("Debe crear titular exitosamente")
        void debeCrearTitularExitosamente() throws Exception {
            // Given
            when(titularMapper.toEntity(any(TitularRequestDTO.class))).thenReturn(titular);
            when(titularService.save(any(Titular.class))).thenReturn(titular);
            when(titularMapper.toResponseDTO(titular)).thenReturn(titularResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/titulares")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(titularRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.apellido").value("Pérez"));

            verify(titularMapper).toEntity(any(TitularRequestDTO.class));
            verify(titularService).save(any(Titular.class));
            verify(titularMapper).toResponseDTO(titular);
        }

        @Test
        @DisplayName("Debe retornar 400 con datos inválidos")
        void debeRetornar400ConDatosInvalidos() throws Exception {
            // Given - DTO con datos vacíos
            TitularRequestDTO dtoInvalido = new TitularRequestDTO();

            // When & Then
            mockMvc.perform(post("/api/titulares")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/titulares/{id}")
    class ActualizarTitular {

        @Test
        @DisplayName("Debe actualizar titular exitosamente")
        void debeActualizarTitularExitosamente() throws Exception {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            doNothing().when(titularMapper).updateEntity(eq(titular), any(TitularRequestDTO.class));
            when(titularService.update(titular)).thenReturn(titular);
            when(titularMapper.toResponseDTO(titular)).thenReturn(titularResponseDTO);

            // When & Then
            mockMvc.perform(put("/api/titulares/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(titularRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Juan"));

            verify(titularService).findById(1L);
            verify(titularMapper).updateEntity(eq(titular), any(TitularRequestDTO.class));
            verify(titularService).update(titular);
        }

        @Test
        @DisplayName("Debe retornar 404 al actualizar titular inexistente")
        void debeRetornar404AlActualizarTitularInexistente() throws Exception {
            // Given
            when(titularService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(put("/api/titulares/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(titularRequestDTO)))
                    .andExpect(status().isNotFound());

            verify(titularService).findById(999L);
            verifyNoMoreInteractions(titularService, titularMapper);
        }
    }

    @Nested
    @DisplayName("DELETE /api/titulares/{id}")
    class EliminarTitular {

        @Test
        @DisplayName("Debe eliminar titular exitosamente")
        void debeEliminarTitularExitosamente() throws Exception {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            doNothing().when(titularService).delete(1L);

            // When & Then
            mockMvc.perform(delete("/api/titulares/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(titularService).findById(1L);
            verify(titularService).delete(1L);
        }

        @Test
        @DisplayName("Debe retornar 404 al eliminar titular inexistente")
        void debeRetornar404AlEliminarTitularInexistente() throws Exception {
            // Given
            when(titularService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(delete("/api/titulares/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(titularService).findById(999L);
            verifyNoMoreInteractions(titularService);
        }
    }

    @Nested
    @DisplayName("GET /api/titulares/buscar/nombre")
    class BuscarTitularesPorNombre {

        @Test
        @DisplayName("Debe buscar titulares por nombre exitosamente")
        void debeBuscarTitularesPorNombreExitosamente() throws Exception {
            // Given
            List<Titular> titulares = Arrays.asList(titular);
            List<TitularResponseDTO> dtos = Arrays.asList(titularResponseDTO);
            
            when(titularService.findByNombre("Juan")).thenReturn(titulares);
            when(titularMapper.toResponseDTOList(titulares)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/titulares/buscar/nombre")
                    .param("nombre", "Juan")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].nombre").value("Juan"));

            verify(titularService).findByNombre("Juan");
            verify(titularMapper).toResponseDTOList(titulares);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no encuentra titulares por nombre")
        void debeRetornarListaVaciaCuandoNoEncuentraTitularesPorNombre() throws Exception {
            // Given
            when(titularService.findByNombre("Inexistente")).thenReturn(Arrays.asList());
            when(titularMapper.toResponseDTOList(any())).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/titulares/buscar/nombre")
                    .param("nombre", "Inexistente")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(titularService).findByNombre("Inexistente");
        }
    }
}
