package com.example.sigelic.controller;

import java.math.BigDecimal;
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
import com.example.sigelic.dto.request.PagoRequestDTO;
import com.example.sigelic.dto.response.PagoResponseDTO;
import com.example.sigelic.mapper.PagoMapper;
import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoPago;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.MedioPago;
import com.example.sigelic.model.Pago;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.PagoService;
import com.example.sigelic.service.TramiteService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests para PagoController
 */
@WebMvcTest(PagoController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de PagoController")
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    @MockitoBean
    private TramiteService tramiteService;

    @MockitoBean
    private PagoMapper pagoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Pago pago;
    private Tramite tramite;
    private PagoRequestDTO pagoRequestDTO;
    private PagoResponseDTO pagoResponseDTO;
    private Titular titular;

    @BeforeEach
    void setUp() {
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");

        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTitular(titular);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setClaseSolicitada(ClaseLicencia.B);
        tramite.setEstado(EstadoTramite.DOCS_OK);

        pago = new Pago();
        pago.setId(1L);
        pago.setTramite(tramite);
        pago.setMonto(new BigDecimal("1500.00"));
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMedio(MedioPago.CAJA);
        pago.setFecha(LocalDateTime.now());

        pagoRequestDTO = new PagoRequestDTO();
        pagoRequestDTO.setTramiteId(1L);
        pagoRequestDTO.setMedio(MedioPago.CAJA);
        pagoRequestDTO.setMonto(new BigDecimal("1500.00"));
        pagoRequestDTO.setNumeroComprobante("COMP-123");
        pagoRequestDTO.setCajero("Juan Cajero");

        pagoResponseDTO = new PagoResponseDTO();
        pagoResponseDTO.setId(1L);
        pagoResponseDTO.setMonto(new BigDecimal("1500.00"));
        pagoResponseDTO.setEstado(EstadoPago.PENDIENTE);
        pagoResponseDTO.setMedio(MedioPago.CAJA);
    }

    @Nested
    @DisplayName("Obtener pago por ID")
    class ObtenerPagoPorId {

        @Test
        @DisplayName("Debe retornar pago por ID exitosamente")
        void debeRetornarPagoPorIdExitosamente() throws Exception {
            // Given
            when(pagoService.findById(1L)).thenReturn(Optional.of(pago));
            when(pagoMapper.toResponseDTOWithDetails(pago)).thenReturn(pagoResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/pagos/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.monto").value(1500.00))
                    .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                    .andExpect(jsonPath("$.medio").value("CAJA"));

            verify(pagoService).findById(1L);
            verify(pagoMapper).toResponseDTOWithDetails(pago);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando pago no existe")
        void debeRetornar404CuandoPagoNoExiste() throws Exception {
            // Given
            when(pagoService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/pagos/999"))
                    .andExpect(status().isNotFound());

            verify(pagoService).findById(999L);
            verify(pagoMapper, never()).toResponseDTOWithDetails(any());
        }
    }

    @Nested
    @DisplayName("Crear orden de pago")
    class CrearOrdenPago {

        @Test
        @DisplayName("Debe crear orden de pago automática exitosamente")
        void debeCrearOrdenPagoAutomaticaExitosamente() throws Exception {
            // Given
            PagoRequestDTO requestSinMonto = new PagoRequestDTO();
            requestSinMonto.setTramiteId(1L);
            requestSinMonto.setMedio(MedioPago.CAJA);
            // Sin monto para que sea automática

            when(tramiteService.findById(1L)).thenReturn(Optional.of(tramite));
            when(pagoService.crearOrdenPago(tramite, MedioPago.CAJA)).thenReturn(pago);
            when(pagoMapper.toResponseDTO(pago)).thenReturn(pagoResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/pagos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestSinMonto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.monto").value(1500.00))
                    .andExpect(jsonPath("$.estado").value("PENDIENTE"));

            verify(tramiteService).findById(1L);
            verify(pagoService).crearOrdenPago(tramite, MedioPago.CAJA);
            verify(pagoMapper).toResponseDTO(pago);
        }

        @Test
        @DisplayName("Debe crear pago manual exitosamente")
        void debeCrearPagoManualExitosamente() throws Exception {
            // Given
            when(tramiteService.findById(1L)).thenReturn(Optional.of(tramite));
            when(pagoService.crearPagoManual(
                    tramite, 
                    new BigDecimal("1500.00"), 
                    "COMP-123", 
                    "Juan Cajero"
            )).thenReturn(pago);
            when(pagoMapper.toResponseDTO(pago)).thenReturn(pagoResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/pagos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pagoRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.monto").value(1500.00));

            verify(tramiteService).findById(1L);
            verify(pagoService).crearPagoManual(tramite, new BigDecimal("1500.00"), "COMP-123", "Juan Cajero");
        }

        @Test
        @DisplayName("Debe retornar 400 cuando trámite no existe")
        void debeRetornar400CuandoTramiteNoExiste() throws Exception {
            // Given
            when(tramiteService.findById(999L)).thenReturn(Optional.empty());
            
            PagoRequestDTO request = new PagoRequestDTO();
            request.setTramiteId(999L);
            request.setMedio(MedioPago.CAJA);

            // When & Then
            mockMvc.perform(post("/api/pagos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(tramiteService).findById(999L);
            verify(pagoService, never()).crearOrdenPago(any(), any());
            verify(pagoService, never()).crearPagoManual(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Acreditar pago")
    class AcreditarPago {

        @Test
        @DisplayName("Debe acreditar pago exitosamente")
        void debeAcreditarPagoExitosamente() throws Exception {
            // Given
            pago.setEstado(EstadoPago.ACREDITADO);
            pagoResponseDTO.setEstado(EstadoPago.ACREDITADO);
            
            when(pagoService.acreditarPago(1L, "COMP-123", "Juan Cajero")).thenReturn(pago);
            when(pagoMapper.toResponseDTO(pago)).thenReturn(pagoResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/pagos/1/acreditar")
                    .param("numeroComprobante", "COMP-123")
                    .param("cajero", "Juan Cajero"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.estado").value("ACREDITADO"));

            verify(pagoService).acreditarPago(1L, "COMP-123", "Juan Cajero");
            verify(pagoMapper).toResponseDTO(pago);
        }

        @Test
        @DisplayName("Debe acreditar pago sin parámetros opcionales")
        void debeAcreditarPagoSinParametrosOpcionales() throws Exception {
            // Given
            pago.setEstado(EstadoPago.ACREDITADO);
            pagoResponseDTO.setEstado(EstadoPago.ACREDITADO);
            
            when(pagoService.acreditarPago(1L, null, null)).thenReturn(pago);
            when(pagoMapper.toResponseDTO(pago)).thenReturn(pagoResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/pagos/1/acreditar"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.estado").value("ACREDITADO"));

            verify(pagoService).acreditarPago(1L, null, null);
        }

        @Test
        @DisplayName("Debe retornar 400 cuando hay error al acreditar")
        void debeRetornar400CuandoHayErrorAlAcreditar() throws Exception {
            // Given
            when(pagoService.acreditarPago(1L, "COMP-123", "Juan Cajero"))
                    .thenThrow(new IllegalStateException("Pago no está en estado PENDIENTE"));

            // When & Then
            mockMvc.perform(patch("/api/pagos/1/acreditar")
                    .param("numeroComprobante", "COMP-123")
                    .param("cajero", "Juan Cajero"))
                    .andExpect(status().isBadRequest());

            verify(pagoService).acreditarPago(1L, "COMP-123", "Juan Cajero");
        }
    }

    @Nested
    @DisplayName("Rechazar pago")
    class RechazarPago {

        @Test
        @DisplayName("Debe rechazar pago exitosamente")
        void debeRechazarPagoExitosamente() throws Exception {
            // Given
            pago.setEstado(EstadoPago.RECHAZADO);
            pagoResponseDTO.setEstado(EstadoPago.RECHAZADO);
            
            when(pagoService.rechazarPago(1L, "Comprobante inválido")).thenReturn(pago);
            when(pagoMapper.toResponseDTO(pago)).thenReturn(pagoResponseDTO);

            // When & Then
            mockMvc.perform(patch("/api/pagos/1/rechazar")
                    .param("motivo", "Comprobante inválido"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.estado").value("RECHAZADO"));

            verify(pagoService).rechazarPago(1L, "Comprobante inválido");
            verify(pagoMapper).toResponseDTO(pago);
        }

        @Test
        @DisplayName("Debe retornar 400 cuando hay error al rechazar")
        void debeRetornar400CuandoHayErrorAlRechazar() throws Exception {
            // Given
            when(pagoService.rechazarPago(1L, "Motivo"))
                    .thenThrow(new IllegalStateException("Pago ya está procesado"));

            // When & Then
            mockMvc.perform(patch("/api/pagos/1/rechazar")
                    .param("motivo", "Motivo"))
                    .andExpect(status().isBadRequest());

            verify(pagoService).rechazarPago(1L, "Motivo");
        }
    }

    @Nested
    @DisplayName("Consultas de pagos")
    class ConsultasPagos {

        @Test
        @DisplayName("Debe obtener pagos por trámite exitosamente")
        void debeObtenerPagosPorTramiteExitosamente() throws Exception {
            // Given
            List<Pago> pagos = Arrays.asList(pago);
            List<PagoResponseDTO> dtos = Arrays.asList(pagoResponseDTO);
            
            when(tramiteService.findById(1L)).thenReturn(Optional.of(tramite));
            when(pagoService.findByTramite(tramite)).thenReturn(pagos);
            when(pagoMapper.toResponseDTOList(pagos)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/pagos/tramite/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L));

            verify(tramiteService).findById(1L);
            verify(pagoService).findByTramite(tramite);
            verify(pagoMapper).toResponseDTOList(pagos);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando trámite no existe")
        void debeRetornar404CuandoTramiteNoExisteEnConsulta() throws Exception {
            // Given
            when(tramiteService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/pagos/tramite/999"))
                    .andExpect(status().isNotFound());

            verify(tramiteService).findById(999L);
            verify(pagoService, never()).findByTramite(any());
        }

        @Test
        @DisplayName("Debe obtener pagos por estado exitosamente")
        void debeObtenerPagosPorEstadoExitosamente() throws Exception {
            // Given
            List<Pago> pagos = Arrays.asList(pago);
            List<PagoResponseDTO> dtos = Arrays.asList(pagoResponseDTO);
            
            when(pagoService.findByEstado(EstadoPago.PENDIENTE)).thenReturn(pagos);
            when(pagoMapper.toResponseDTOList(pagos)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/pagos/estado/PENDIENTE"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));

            verify(pagoService).findByEstado(EstadoPago.PENDIENTE);
            verify(pagoMapper).toResponseDTOList(pagos);
        }

        @Test
        @DisplayName("Debe obtener pagos por fecha exitosamente")
        void debeObtenerPagosPorFechaExitosamente() throws Exception {
            // Given
            List<Pago> pagos = Arrays.asList(pago);
            List<PagoResponseDTO> dtos = Arrays.asList(pagoResponseDTO);
            LocalDateTime fechaDesde = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime fechaHasta = LocalDateTime.of(2025, 12, 31, 23, 59);
            
            when(pagoService.getPagosAcreditadosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(pagos);
            when(pagoMapper.toResponseDTOList(pagos)).thenReturn(dtos);

            // When & Then
            mockMvc.perform(get("/api/pagos/fecha")
                    .param("fechaDesde", "2025-01-01T00:00:00")
                    .param("fechaHasta", "2025-12-31T23:59:00"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L));

            verify(pagoService).getPagosAcreditadosEnPeriodo(fechaDesde, fechaHasta);
            verify(pagoMapper).toResponseDTOList(pagos);
        }
    }
}
