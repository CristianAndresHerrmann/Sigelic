package com.example.sigelic.controller;

import com.example.sigelic.config.TestSecurityConfig;
import com.example.sigelic.dto.response.LicenciaResponseDTO;
import com.example.sigelic.mapper.LicenciaMapper;
import com.example.sigelic.model.*;
import com.example.sigelic.service.LicenciaService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para LicenciaController
 */
@WebMvcTest(LicenciaController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de LicenciaController")
class LicenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LicenciaService licenciaService;

    @MockitoBean
    private TitularService titularService;

    @MockitoBean
    private LicenciaMapper licenciaMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Licencia licencia;
    private LicenciaResponseDTO licenciaResponseDTO;
    private Titular titular;

    @BeforeEach
    void setUp() {
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");
        titular.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        licencia = new Licencia();
        licencia.setId(1L);
        licencia.setTitular(titular);
        licencia.setClase(ClaseLicencia.B);
        licencia.setNumeroLicencia("B-123456789");
        licencia.setFechaEmision(LocalDate.now());
        licencia.setFechaVencimiento(LocalDate.now().plusYears(5));
        licencia.setEstado(EstadoLicencia.VIGENTE);

        licenciaResponseDTO = new LicenciaResponseDTO();
        licenciaResponseDTO.setId(1L);
        licenciaResponseDTO.setClase(ClaseLicencia.B);
        licenciaResponseDTO.setNumeroLicencia("B-123456789");
        licenciaResponseDTO.setEstado(EstadoLicencia.VIGENTE);
        licenciaResponseDTO.setFechaEmision(LocalDate.now());
        licenciaResponseDTO.setFechaVencimiento(LocalDate.now().plusYears(5));
    }

    @Nested
    @DisplayName("Obtener licencia por ID")
    class ObtenerLicenciaPorId {

        @Test
        @DisplayName("Debe retornar licencia por ID exitosamente")
        void debeRetornarLicenciaPorIdExitosamente() throws Exception {
            // Given
            when(licenciaService.findById(1L)).thenReturn(Optional.of(licencia));
            when(licenciaMapper.toResponseDTOWithDetails(licencia)).thenReturn(licenciaResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/licencias/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.numeroLicencia").value("B-123456789"))
                    .andExpect(jsonPath("$.clase").value("B"));

            verify(licenciaService).findById(1L);
            verify(licenciaMapper).toResponseDTOWithDetails(licencia);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando licencia no existe")
        void debeRetornar404CuandoLicenciaNoExiste() throws Exception {
            // Given
            when(licenciaService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/licencias/999"))
                    .andExpect(status().isNotFound());

            verify(licenciaService).findById(999L);
        }
    }

    @Nested
    @DisplayName("Obtener licencia por número")
    class ObtenerLicenciaPorNumero {

        @Test
        @DisplayName("Debe retornar licencia por número exitosamente")
        void debeRetornarLicenciaPorNumeroExitosamente() throws Exception {
            // Given
            when(licenciaService.findByNumero("B-123456789")).thenReturn(Optional.of(licencia));
            when(licenciaMapper.toResponseDTOWithDetails(licencia)).thenReturn(licenciaResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/licencias/numero/B-123456789"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.numeroLicencia").value("B-123456789"))
                    .andExpect(jsonPath("$.clase").value("B"));

            verify(licenciaService).findByNumero("B-123456789");
            verify(licenciaMapper).toResponseDTOWithDetails(licencia);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando licencia no existe")
        void debeRetornar404CuandoLicenciaNoExiste() throws Exception {
            // Given
            when(licenciaService.findByNumero("INEXISTENTE")).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/licencias/numero/INEXISTENTE"))
                    .andExpect(status().isNotFound());

            verify(licenciaService).findByNumero("INEXISTENTE");
        }
    }

    @Nested
    @DisplayName("Obtener licencias por titular")
    class ObtenerLicenciasPorTitular {

        @Test
        @DisplayName("Debe retornar licencias del titular exitosamente")
        void debeRetornarLicenciasDelTitularExitosamente() throws Exception {
            // Given
            List<Licencia> licencias = Arrays.asList(licencia);
            List<LicenciaResponseDTO> dtos = Arrays.asList(licenciaResponseDTO);
            
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(licenciaService.findByTitular(titular)).thenReturn(licencias);
            when(licenciaMapper.toResponseDTOList(licencias)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/licencias/titular/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].clase").value("B"));

            verify(titularService).findById(1L);
            verify(licenciaService).findByTitular(titular);
            verify(licenciaMapper).toResponseDTOList(licencias);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando titular no existe")
        void debeRetornar404CuandoTitularNoExiste() throws Exception {
            // Given
            when(titularService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/licencias/titular/999"))
                    .andExpect(status().isNotFound());

            verify(titularService).findById(999L);
            verify(licenciaService, never()).findByTitular(any());
        }
    }

    @Nested
    @DisplayName("Obtener licencias vigentes por titular")
    class ObtenerLicenciasVigentesPorTitular {

        @Test
        @DisplayName("Debe retornar licencias vigentes del titular exitosamente")
        void debeRetornarLicenciasVigentesDelTitularExitosamente() throws Exception {
            // Given
            List<Licencia> licencias = Arrays.asList(licencia);
            List<LicenciaResponseDTO> dtos = Arrays.asList(licenciaResponseDTO);
            
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(licenciaService.findLicenciasVigentesByTitular(titular)).thenReturn(licencias);
            when(licenciaMapper.toResponseDTOList(licencias)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/licencias/titular/1/vigentes"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].estado").value("VIGENTE"));

            verify(titularService).findById(1L);
            verify(licenciaService).findLicenciasVigentesByTitular(titular);
        }
    }

    @Nested
    @DisplayName("Consultas especiales")
    class ConsultasEspeciales {

        @Test
        @DisplayName("Debe obtener licencias próximas a vencer exitosamente")
        void debeObtenerLicenciasProximasAVencerExitosamente() throws Exception {
            // Given
            List<Licencia> licencias = Arrays.asList(licencia);
            List<LicenciaResponseDTO> dtos = Arrays.asList(licenciaResponseDTO);
            
            when(licenciaService.getLicenciasProximasAVencer(30)).thenReturn(licencias);
            when(licenciaMapper.toResponseDTOList(licencias)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/licencias/proximas-vencer")
                    .param("dias", "30"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray());

            verify(licenciaService).getLicenciasProximasAVencer(30);
            verify(licenciaMapper).toResponseDTOList(licencias);
        }

        @Test
        @DisplayName("Debe obtener licencias vencidas exitosamente")
        void debeObtenerLicenciasVencidasExitosamente() throws Exception {
            // Given
            List<Licencia> licencias = Arrays.asList(licencia);
            List<LicenciaResponseDTO> dtos = Arrays.asList(licenciaResponseDTO);
            
            when(licenciaService.getLicenciasVencidas()).thenReturn(licencias);
            when(licenciaMapper.toResponseDTOList(licencias)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/licencias/vencidas"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray());

            verify(licenciaService).getLicenciasVencidas();
        }
    }

    @Nested
    @DisplayName("Acciones sobre licencias")
    class AccionesSobreLicencias {

        @Test
        @DisplayName("Debe suspender licencia exitosamente")
        void debeSuspenderLicenciaExitosamente() throws Exception {
            // Given
            licencia.setEstado(EstadoLicencia.SUSPENDIDA);
            licenciaResponseDTO.setEstado(EstadoLicencia.SUSPENDIDA);
            
            when(licenciaService.suspenderLicencia(1L, "Multas pendientes")).thenReturn(licencia);
            when(licenciaMapper.toResponseDTO(licencia)).thenReturn(licenciaResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/licencias/1/suspender")
                    .param("motivo", "Multas pendientes"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.estado").value("SUSPENDIDA"));

            verify(licenciaService).suspenderLicencia(1L, "Multas pendientes");
            verify(licenciaMapper).toResponseDTO(licencia);
        }

        @Test
        @DisplayName("Debe inhabilitar licencia exitosamente")
        void debeInhabilitarLicenciaExitosamente() throws Exception {
            // Given
            licencia.setEstado(EstadoLicencia.INHABILITADA);
            licenciaResponseDTO.setEstado(EstadoLicencia.INHABILITADA);
            
            when(licenciaService.inhabilitarLicencia(1L, "Documentación falsa")).thenReturn(licencia);
            when(licenciaMapper.toResponseDTO(licencia)).thenReturn(licenciaResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/licencias/1/inhabilitar")
                    .param("motivo", "Documentación falsa"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.estado").value("INHABILITADA"));

            verify(licenciaService).inhabilitarLicencia(1L, "Documentación falsa");
            verify(licenciaMapper).toResponseDTO(licencia);
        }

        @Test
        @DisplayName("Debe actualizar domicilio exitosamente")
        void debeActualizarDomicilioExitosamente() throws Exception {
            // Given
            when(licenciaService.findById(1L)).thenReturn(Optional.of(licencia));
            when(licenciaService.actualizarDomicilio(licencia, "Nueva dirección 123")).thenReturn(licencia);
            when(licenciaMapper.toResponseDTO(licencia)).thenReturn(licenciaResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/licencias/1/actualizar-domicilio")
                    .param("nuevoDomicilio", "Nueva dirección 123"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(licenciaService).findById(1L);
            verify(licenciaService).actualizarDomicilio(licencia, "Nueva dirección 123");
        }
    }

    @Nested
    @DisplayName("Procesos masivos")
    class ProcesosMasivos {

        @Test
        @DisplayName("Debe actualizar licencias vencidas exitosamente")
        void debeActualizarLicenciasVencidasExitosamente() throws Exception {
            // Given
            doNothing().when(licenciaService).actualizarLicenciasVencidas();

            // When & Then
            mockMvc.perform(post("/api/licencias/actualizar-vencidas"))
                    .andExpect(status().isOk());

            verify(licenciaService).actualizarLicenciasVencidas();
        }

        @Test
        @DisplayName("Debe obtener contador de licencias emitidas exitosamente")
        void debeObtenerContadorLicenciasEmitidasExitosamente() throws Exception {
            // Given
            LocalDate desde = LocalDate.of(2025, 1, 1);
            LocalDate hasta = LocalDate.of(2025, 12, 31);
            
            when(licenciaService.getCountLicenciasEmitidasEnPeriodo(desde, hasta)).thenReturn(150L);

            // When & Then
            mockMvc.perform(get("/api/licencias/contador/emitidas")
                    .param("desde", "2025-01-01")
                    .param("hasta", "2025-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").value(150));

            verify(licenciaService).getCountLicenciasEmitidasEnPeriodo(desde, hasta);
        }
    }
}
