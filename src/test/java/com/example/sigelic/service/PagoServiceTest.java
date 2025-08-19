package com.example.sigelic.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.CostoTramite;
import com.example.sigelic.model.EstadoPago;
import com.example.sigelic.model.MedioPago;
import com.example.sigelic.model.Pago;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.CostoTramiteRepository;
import com.example.sigelic.repository.PagoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PagoService")
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private CostoTramiteRepository costoTramiteRepository;

    @InjectMocks
    private PagoService pagoService;

    private Tramite tramite;
    private Pago pago;
    private CostoTramite costoTramite;

    @BeforeEach
    void setUp() {
        // Crear trámite
        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setClaseSolicitada(ClaseLicencia.B);

        // Crear pago
        pago = new Pago();
        pago.setId(1L);
        pago.setTramite(tramite);
        pago.setMonto(new BigDecimal("1500.00"));
        pago.setMedio(MedioPago.CAJA);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setNumeroTransaccion("TXN-20250819100000-1234");

        // Crear costo de trámite
        costoTramite = new CostoTramite();
        costoTramite.setId(1L);
        costoTramite.setTipoTramite(TipoTramite.EMISION);
        costoTramite.setClaseLicencia(ClaseLicencia.B);
        costoTramite.setCosto(new BigDecimal("1500.00"));
        costoTramite.setFechaVigenciaDesde(LocalDate.now().minusDays(30));
        costoTramite.setFechaVigenciaHasta(LocalDate.now().plusDays(30));
    }

    @Nested
    @DisplayName("Búsqueda de pagos")
    class BusquedaPagos {

        @Test
        @DisplayName("Debe encontrar pago por ID exitosamente")
        void debeEncontrarPagoPorIdExitosamente() {
            // Given
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

            // When
            Optional<Pago> resultado = pagoService.findById(1L);

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getMonto()).isEqualTo(new BigDecimal("1500.00"));
            verify(pagoRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe obtener pagos por trámite")
        void debeObtenerPagosPorTramite() {
            // Given
            List<Pago> pagos = Arrays.asList(pago);
            when(pagoRepository.findByTramite(tramite)).thenReturn(pagos);

            // When
            List<Pago> resultado = pagoService.findByTramite(tramite);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTramite()).isEqualTo(tramite);
            verify(pagoRepository).findByTramite(tramite);
        }

        @Test
        @DisplayName("Debe obtener pagos por estado")
        void debeObtenerPagosPorEstado() {
            // Given
            List<Pago> pagos = Arrays.asList(pago);
            when(pagoRepository.findByEstado(EstadoPago.PENDIENTE)).thenReturn(pagos);

            // When
            List<Pago> resultado = pagoService.findByEstado(EstadoPago.PENDIENTE);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getEstado()).isEqualTo(EstadoPago.PENDIENTE);
            verify(pagoRepository).findByEstado(EstadoPago.PENDIENTE);
        }

        @Test
        @DisplayName("Debe encontrar pago por número de transacción")
        void debeEncontrarPagoPorNumeroTransaccion() {
            // Given
            when(pagoRepository.findByNumeroTransaccion("TXN-123")).thenReturn(Optional.of(pago));

            // When
            Optional<Pago> resultado = pagoService.findByNumeroTransaccion("TXN-123");

            // Then
            assertThat(resultado).isPresent();
            verify(pagoRepository).findByNumeroTransaccion("TXN-123");
        }

        @Test
        @DisplayName("Debe encontrar pago por número de comprobante")
        void debeEncontrarPagoPorNumeroComprobante() {
            // Given
            when(pagoRepository.findByNumeroComprobante("COMP-123")).thenReturn(Optional.of(pago));

            // When
            Optional<Pago> resultado = pagoService.findByNumeroComprobante("COMP-123");

            // Then
            assertThat(resultado).isPresent();
            verify(pagoRepository).findByNumeroComprobante("COMP-123");
        }
    }

    @Nested
    @DisplayName("Creación de órdenes de pago")
    class CreacionOrdenesPago {

        @Test
        @DisplayName("Debe crear orden de pago exitosamente")
        void debeCrearOrdenPagoExitosamente() {
            // Given
            when(pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.PENDIENTE))
                    .thenReturn(Optional.empty());
            when(costoTramiteRepository.findCostoVigente(eq(TipoTramite.EMISION), eq(ClaseLicencia.B), any(LocalDate.class)))
                    .thenReturn(Optional.of(costoTramite));
            when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

            // When
            Pago resultado = pagoService.crearOrdenPago(tramite, MedioPago.CAJA);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getMonto()).isEqualTo(new BigDecimal("1500.00"));
            assertThat(resultado.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
            verify(pagoRepository).save(any(Pago.class));
        }

        @Test
        @DisplayName("Debe fallar al crear orden cuando ya existe una pendiente")
        void debeFallarAlCrearOrdenCuandoYaExisteUnaPendiente() {
            // Given
            when(pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.PENDIENTE))
                    .thenReturn(Optional.of(pago));

            // When & Then
            assertThatThrownBy(() -> pagoService.crearOrdenPago(tramite, MedioPago.CAJA))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Ya existe una orden de pago pendiente para este trámite");

            verify(pagoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe usar costo por defecto cuando no hay configurado")
        void debeUsarCostoPorDefectoCuandoNoHayConfigurado() {
            // Given
            when(pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.PENDIENTE))
                    .thenReturn(Optional.empty());
            when(costoTramiteRepository.findCostoVigente(eq(TipoTramite.EMISION), eq(ClaseLicencia.B), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

            // When
            Pago resultado = pagoService.crearOrdenPago(tramite, MedioPago.CAJA);

            // Then
            assertThat(resultado).isNotNull();
            verify(pagoRepository).save(any(Pago.class));
        }
    }

    @Nested
    @DisplayName("Gestión de estados de pago")
    class GestionEstadosPago {

        @Test
        @DisplayName("Debe acreditar pago exitosamente")
        void debeAcreditarPagoExitosamente() {
            // Given
            pago.setEstado(EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
            when(pagoRepository.save(pago)).thenReturn(pago);

            // When
            Pago resultado = pagoService.acreditarPago(1L, "COMP-123", "Juan Cajero");

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoPago.ACREDITADO);
            assertThat(resultado.getNumeroComprobante()).isEqualTo("COMP-123");
            assertThat(resultado.getCajero()).isEqualTo("Juan Cajero");
            verify(pagoRepository).save(pago);
        }

        @Test
        @DisplayName("Debe fallar al acreditar pago que no está pendiente")
        void debeFallarAlAcreditarPagoQueNoEstaPendiente() {
            // Given
            pago.setEstado(EstadoPago.ACREDITADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

            // When & Then
            assertThatThrownBy(() -> pagoService.acreditarPago(1L, "COMP-123", "Juan Cajero"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Solo se pueden acreditar pagos en estado PENDIENTE");

            verify(pagoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar pago exitosamente")
        void debeRechazarPagoExitosamente() {
            // Given
            pago.setEstado(EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
            when(pagoRepository.save(pago)).thenReturn(pago);

            // When
            Pago resultado = pagoService.rechazarPago(1L, "Fondos insuficientes");

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
            verify(pagoRepository).save(pago);
        }

        @Test
        @DisplayName("Debe fallar al rechazar pago que no está pendiente")
        void debeFallarAlRechazarPagoQueNoEstaPendiente() {
            // Given
            pago.setEstado(EstadoPago.ACREDITADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

            // When & Then
            assertThatThrownBy(() -> pagoService.rechazarPago(1L, "Motivo"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Solo se pueden rechazar pagos en estado PENDIENTE");

            verify(pagoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pagos especiales")
    class PagosEspeciales {

        @Test
        @DisplayName("Debe crear pago manual exitosamente")
        void debeCrearPagoManualExitosamente() {
            // Given
            Pago pagoManual = new Pago();
            pagoManual.setEstado(EstadoPago.ACREDITADO);
            pagoManual.setMedio(MedioPago.CAJA);
            when(pagoRepository.save(any(Pago.class))).thenReturn(pagoManual);

            // When
            Pago resultado = pagoService.crearPagoManual(
                    tramite, 
                    new BigDecimal("1500.00"), 
                    "COMP-123", 
                    "Juan Cajero"
            );

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoPago.ACREDITADO);
            assertThat(resultado.getMedio()).isEqualTo(MedioPago.CAJA);
            verify(pagoRepository).save(any(Pago.class));
        }

        @Test
        @DisplayName("Debe procesar pago online exitoso")
        void debeProcesarPagoOnlineExitoso() {
            // Given
            pago.setMedio(MedioPago.PASARELA_ONLINE);
            pago.setEstado(EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
            when(pagoRepository.save(pago)).thenReturn(pago);

            // When
            Pago resultado = pagoService.procesarPagoOnline(1L, "EXT-TXN-123", true);

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoPago.ACREDITADO);
            assertThat(resultado.getObservaciones()).contains("EXT-TXN-123");
            verify(pagoRepository).save(pago);
        }

        @Test
        @DisplayName("Debe procesar pago online rechazado")
        void debeProcesarPagoOnlineRechazado() {
            // Given
            pago.setMedio(MedioPago.PASARELA_ONLINE);
            pago.setEstado(EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
            when(pagoRepository.save(pago)).thenReturn(pago);

            // When
            Pago resultado = pagoService.procesarPagoOnline(1L, "EXT-TXN-123", false);

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
            verify(pagoRepository).save(pago);
        }

        @Test
        @DisplayName("Debe fallar al procesar pago online que no es de pasarela")
        void debeFallarAlProcesarPagoOnlineQueNoEsDePasarela() {
            // Given
            pago.setMedio(MedioPago.CAJA);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

            // When & Then
            assertThatThrownBy(() -> pagoService.procesarPagoOnline(1L, "EXT-TXN-123", true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Este método solo aplica para pagos online");

            verify(pagoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consultas y estadísticas")
    class ConsultasEstadisticas {

        @Test
        @DisplayName("Debe obtener recaudación en período")
        void debeObtenerRecaudacionEnPeriodo() {
            // Given
            LocalDateTime desde = LocalDateTime.now().minusDays(30);
            LocalDateTime hasta = LocalDateTime.now();
            BigDecimal total = new BigDecimal("15000.00");
            when(pagoRepository.sumMontoAcreditadoEnPeriodo(desde, hasta)).thenReturn(total);

            // When
            BigDecimal resultado = pagoService.getRecaudacionEnPeriodo(desde, hasta);

            // Then
            assertThat(resultado).isEqualTo(total);
            verify(pagoRepository).sumMontoAcreditadoEnPeriodo(desde, hasta);
        }

        @Test
        @DisplayName("Debe retornar cero cuando no hay recaudación")
        void debeRetornarCeroCuandoNoHayRecaudacion() {
            // Given
            LocalDateTime desde = LocalDateTime.now().minusDays(30);
            LocalDateTime hasta = LocalDateTime.now();
            when(pagoRepository.sumMontoAcreditadoEnPeriodo(desde, hasta)).thenReturn(null);

            // When
            BigDecimal resultado = pagoService.getRecaudacionEnPeriodo(desde, hasta);

            // Then
            assertThat(resultado).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debe obtener pagos acreditados en período")
        void debeObtenerPagosAcreditadosEnPeriodo() {
            // Given
            LocalDateTime desde = LocalDateTime.now().minusDays(30);
            LocalDateTime hasta = LocalDateTime.now();
            List<Pago> pagos = Arrays.asList(pago);
            when(pagoRepository.findPagosAcreditadosEnPeriodo(desde, hasta)).thenReturn(pagos);

            // When
            List<Pago> resultado = pagoService.getPagosAcreditadosEnPeriodo(desde, hasta);

            // Then
            assertThat(resultado).hasSize(1);
            verify(pagoRepository).findPagosAcreditadosEnPeriodo(desde, hasta);
        }

        @Test
        @DisplayName("Debe obtener estadísticas por medio de pago")
        void debeObtenerEstadisticasPorMedioPago() {
            // Given
            LocalDateTime desde = LocalDateTime.now().minusDays(30);
            LocalDateTime hasta = LocalDateTime.now();
            when(pagoRepository.countByMedioEnPeriodo(MedioPago.CAJA, desde, hasta)).thenReturn(10L);

            // When
            Long resultado = pagoService.getCountByMedioEnPeriodo(MedioPago.CAJA, desde, hasta);

            // Then
            assertThat(resultado).isEqualTo(10L);
            verify(pagoRepository).countByMedioEnPeriodo(MedioPago.CAJA, desde, hasta);
        }
    }

    @Nested
    @DisplayName("Verificaciones de estado")
    class VerificacionesEstado {

        @Test
        @DisplayName("Debe verificar si trámite tiene pagos pendientes")
        void debeVerificarSiTramiteTienePagosPendientes() {
            // Given
            Pago pagoPendiente = new Pago();
            pagoPendiente.setEstado(EstadoPago.PENDIENTE);
            when(pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.PENDIENTE))
                    .thenReturn(Optional.of(pagoPendiente));

            // When
            boolean resultado = pagoService.tienePagosPendientes(tramite);

            // Then
            assertThat(resultado).isTrue();
            verify(pagoRepository).findByTramiteAndEstado(tramite, EstadoPago.PENDIENTE);
        }

        @Test
        @DisplayName("Debe verificar si trámite tiene pagos acreditados")
        void debeVerificarSiTramiteTienePagosAcreditados() {
            // Given
            when(pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.ACREDITADO))
                    .thenReturn(Optional.of(pago));

            // When
            boolean resultado = pagoService.tienePagosAcreditados(tramite);

            // Then
            assertThat(resultado).isTrue();
            verify(pagoRepository).findByTramiteAndEstado(tramite, EstadoPago.ACREDITADO);
        }

        @Test
        @DisplayName("Debe obtener costo de trámite")
        void debeObtenerCostoDeTramite() {
            // Given
            when(costoTramiteRepository.findCostoVigente(eq(TipoTramite.EMISION), eq(ClaseLicencia.B), any(LocalDate.class)))
                    .thenReturn(Optional.of(costoTramite));

            // When
            BigDecimal resultado = pagoService.obtenerCostoTramite(TipoTramite.EMISION, ClaseLicencia.B);

            // Then
            assertThat(resultado).isEqualTo(new BigDecimal("1500.00"));
            verify(costoTramiteRepository).findCostoVigente(eq(TipoTramite.EMISION), eq(ClaseLicencia.B), any(LocalDate.class));
        }
    }

    @Nested
    @DisplayName("Procesos automáticos")
    class ProcesosAutomaticos {

        @Test
        @DisplayName("Debe procesar pagos vencidos")
        void debeProcesarPagosVencidos() {
            // Given
            List<Pago> pagosVencidos = Arrays.asList(pago);
            when(pagoRepository.findPagosVencidos(any(LocalDateTime.class))).thenReturn(pagosVencidos);
            when(pagoRepository.save(pago)).thenReturn(pago);

            // When
            pagoService.procesarPagosVencidos();

            // Then
            verify(pagoRepository).findPagosVencidos(any(LocalDateTime.class));
            verify(pagoRepository).save(pago);
        }
    }

    @Nested
    @DisplayName("Manejo de errores")
    class ManejoErrores {

        @Test
        @DisplayName("Debe fallar con pago inexistente")
        void debeFallarConPagoInexistente() {
            // Given
            when(pagoRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> pagoService.acreditarPago(999L, "COMP-123", "Cajero"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Pago no encontrado con ID: 999");

            verify(pagoRepository).findById(999L);
        }
    }
}