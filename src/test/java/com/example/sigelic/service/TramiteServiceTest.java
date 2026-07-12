package com.example.sigelic.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.dto.request.AptoMedicoRequestDTO;
import com.example.sigelic.dto.response.AptoMedicoResponseDTO;
import com.example.sigelic.model.AptoMedico;
import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoLicencia;
import com.example.sigelic.model.EstadoPago;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.MotivoDuplicacion;
import com.example.sigelic.model.Pago;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.AptoMedicoRepository;
import com.example.sigelic.repository.ExamenPracticoRepository;
import com.example.sigelic.repository.ExamenTeoricoRepository;
import com.example.sigelic.repository.PagoRepository;
import com.example.sigelic.repository.TramiteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TramiteService - Reglas de Negocio")
class TramiteServiceTest {

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private TitularService titularService;

    @Mock
    private ExamenTeoricoRepository examenTeoricoRepository;

    @Mock
    private ExamenPracticoRepository examenPracticoRepository;

    @Mock
    private AptoMedicoRepository aptoMedicoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private LicenciaService licenciaService;

    @InjectMocks
    private TramiteService tramiteService;

    private Titular titular;
    private Tramite tramite;

    @BeforeEach
    void setUp() {
        // Crear titular de 25 años
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");
        titular.setFechaNacimiento(LocalDate.now().minusYears(25));
        titular.setEmail("juan@example.com");

        // Crear trámite
        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTitular(titular);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setClaseSolicitada(ClaseLicencia.B);
        tramite.setEstado(EstadoTramite.INICIADO);
    }

    @Nested
    @DisplayName("Consultas de Trámites")
    class ConsultasTramites {

        @Test
        @DisplayName("Debe encontrar trámite por ID")
        void debeEncontrarTramitePorId() {
            // Given
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));

            // When
            Optional<Tramite> resultado = tramiteService.findById(1L);

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get()).isEqualTo(tramite);
            verify(tramiteRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe retornar empty cuando trámite no existe")
        void debeRetornarEmptyCuandoTramiteNoExiste() {
            // Given
            when(tramiteRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Tramite> resultado = tramiteService.findById(999L);

            // Then
            assertThat(resultado).isEmpty();
            verify(tramiteRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Verificación de Requisitos")
    class VerificacionRequisitos {

        @Test
        @DisplayName("Debe requerir examen teórico para emisión")
        void debeRequerirExamenTeoricoPauraEmision() {
            // Given
            tramite.setTipo(TipoTramite.EMISION);

            // When & Then
            assertThat(tramite.requiereExamenTeorico()).isTrue();
        }

        @Test
        @DisplayName("Debe requerir examen práctico para emisión")
        void debeRequerirExamenPracticoParaEmision() {
            // Given
            tramite.setTipo(TipoTramite.EMISION);

            // When & Then
            assertThat(tramite.requiereExamenPractico()).isTrue();
        }

        @Test
        @DisplayName("No debe requerir examen teórico para duplicado")
        void noDebeRequerirExamenTeoricoPauraduplicado() {
            // Given
            tramite.setTipo(TipoTramite.DUPLICADO);

            // When & Then
            assertThat(tramite.requiereExamenTeorico()).isFalse();
        }

        @Test
        @DisplayName("Debe requerir apto médico para emisión")
        void debeRequerirAptoMedicoParaEmision() {
            // Given
            tramite.setTipo(TipoTramite.EMISION);

            // When & Then
            assertThat(tramite.requiereAptoMedico()).isTrue();
        }

        @Test
        @DisplayName("No debe requerir apto médico para duplicado")
        void noDebeRequerirAptoMedicoParaDuplicado() {
            // Given
            tramite.setTipo(TipoTramite.DUPLICADO);

            // When & Then
            assertThat(tramite.requiereAptoMedico()).isFalse();
        }

        @Test
        @DisplayName("Todos los requisitos cumplidos para emisión completa")
        void todosLosRequisitosCumplidosParaEmisionCompleta() {
            // Given
            tramite.setTipo(TipoTramite.EMISION);
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);
            tramite.setExamenPracticoAprobado(true);
            tramite.setPagoAcreditado(true);

            // When & Then
            assertThat(tramite.todosLosRequisitosCumplidos()).isTrue();
        }

        @Test
        @DisplayName("Requisitos no cumplidos cuando falta pago")
        void requisitosNoCumplidosCuandoFaltaPago() {
            // Given
            tramite.setTipo(TipoTramite.EMISION);
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);
            tramite.setExamenPracticoAprobado(true);
            tramite.setPagoAcreditado(false); // Falta pago

            // When & Then
            assertThat(tramite.todosLosRequisitosCumplidos()).isFalse();
        }

        @Test
        @DisplayName("Todos los requisitos cumplidos para duplicado")
        void todosLosRequisitosCumplidosParaDuplicado() {
            // Given
            tramite.setTipo(TipoTramite.DUPLICADO);
            tramite.setDocumentacionValidada(true);
            tramite.setPagoAcreditado(true);
            // No requiere exámenes ni apto médico

            // When & Then
            assertThat(tramite.todosLosRequisitosCumplidos()).isTrue();
        }
    }

    @Nested
    @DisplayName("Inicio de Trámite - Motivo de Duplicación")
    class InicioTramiteDuplicado {

        private void prepararTitularConLicenciaVigenteClaseB() {
            Licencia licenciaVigente = new Licencia();
            licenciaVigente.setClase(ClaseLicencia.B);
            licenciaVigente.setEstado(EstadoLicencia.VIGENTE);
            licenciaVigente.setFechaEmision(LocalDate.now().minusYears(1));
            licenciaVigente.setFechaVencimiento(LocalDate.now().plusYears(4));

            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(titularService.puedeIniciarTramite(1L)).thenReturn(true);
            when(tramiteRepository.existsByTitularAndEstadoIn(any(Titular.class), anyList())).thenReturn(false);
            when(licenciaService.findByTitular(titular)).thenReturn(List.of(licenciaVigente));
        }

        @Test
        @DisplayName("Debe rechazar un duplicado sin motivo de duplicación")
        void debeRechazarDuplicadoSinMotivo() {
            // Given
            prepararTitularConLicenciaVigenteClaseB();

            // When & Then
            assertThatThrownBy(() -> tramiteService.iniciarTramite(1L, TipoTramite.DUPLICADO, ClaseLicencia.B))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("motivo de duplicación");
        }

        @Test
        @DisplayName("Debe persistir el motivo de duplicación en un duplicado")
        void debePersistirMotivoDeDuplicacion() {
            // Given
            prepararTitularConLicenciaVigenteClaseB();
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Tramite resultado = tramiteService.iniciarTramite(
                    1L, TipoTramite.DUPLICADO, ClaseLicencia.B, MotivoDuplicacion.ROBO);

            // Then
            ArgumentCaptor<Tramite> captor = ArgumentCaptor.forClass(Tramite.class);
            verify(tramiteRepository).save(captor.capture());
            assertThat(captor.getValue().getMotivoDuplicacion()).isEqualTo(MotivoDuplicacion.ROBO);
            assertThat(resultado.getTipo()).isEqualTo(TipoTramite.DUPLICADO);
        }

        @Test
        @DisplayName("No debe persistir motivo de duplicación en trámites que no son duplicado")
        void noDebePersistirMotivoEnTramitesNoDuplicado() {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(titularService.puedeIniciarTramite(1L)).thenReturn(true);
            when(tramiteRepository.existsByTitularAndEstadoIn(any(Titular.class), anyList())).thenReturn(false);
            when(licenciaService.findByTitular(titular)).thenReturn(List.of());
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            // When: una emisión con motivo indicado por error
            Tramite resultado = tramiteService.iniciarTramite(
                    1L, TipoTramite.EMISION, ClaseLicencia.B, MotivoDuplicacion.ROBO);

            // Then: el motivo se descarta porque no aplica al tipo
            assertThat(resultado.getMotivoDuplicacion()).isNull();
        }
    }

    @Nested
    @DisplayName("Actualización de Estados")
    class ActualizacionEstados {

        @Test
        @DisplayName("Debe actualizar estado a DOCS_OK cuando solo documentación está validada")
        void debeActualizarEstadoADocsOK() {
            // Given
            tramite.setDocumentacionValidada(true);

            // When
            tramite.actualizarEstado();

            // Then
            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.DOCS_OK);
        }

        @Test
        @DisplayName("Debe actualizar estado a APTO_MED cuando apto médico está vigente")
        void debeActualizarEstadoAAptoMed() {
            // Given
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);

            // When
            tramite.actualizarEstado();

            // Then
            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.APTO_MED);
        }

        @Test
        @DisplayName("Debe actualizar estado a EX_TEO_OK cuando examen teórico aprobado")
        void debeActualizarEstadoAExTeoOK() {
            // Given
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);

            // When
            tramite.actualizarEstado();

            // Then
            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.EX_TEO_OK);
        }

        @Test
        @DisplayName("Debe actualizar estado a PAGO_OK cuando todos los requisitos están cumplidos")
        void debeActualizarEstadoAPagoOK() {
            // Given
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);
            tramite.setExamenPracticoAprobado(true);
            tramite.setPagoAcreditado(true);

            // When
            tramite.actualizarEstado();

            // Then
            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.PAGO_OK);
        }

        @Test
        @DisplayName("No debe cambiar estados finales")
        void noDebeCambiarEstadosFinales() {
            // Given
            tramite.setEstado(EstadoTramite.EMITIDA);
            tramite.setDocumentacionValidada(true);

            // When
            tramite.actualizarEstado();

            // Then
            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.EMITIDA);
        }
    }

    @Nested
    @DisplayName("Validación de documentación")
    class ValidacionDocumentacion {

        @Test
        @DisplayName("Debe validar documentación de un trámite en estado INICIADO")
        void debeValidarDocumentacion() {
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.validarDocumentacion(1L, "agente1");

            assertThat(resultado.getDocumentacionValidada()).isTrue();
            assertThat(resultado.getAgenteResponsable()).isEqualTo("agente1");
        }

        @Test
        @DisplayName("Debe usar 'Sistema' como agente por defecto en la validación simplificada")
        void debeValidarDocumentacionConAgentePorDefecto() {
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.validarDocumentacion(1L);

            assertThat(resultado.getAgenteResponsable()).isEqualTo("Sistema");
        }

        @Test
        @DisplayName("Debe rechazar validar documentación si el trámite no está INICIADO")
        void debeRechazarValidarDocumentacionEnEstadoInvalido() {
            tramite.setEstado(EstadoTramite.DOCS_OK);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));

            assertThatThrownBy(() -> tramiteService.validarDocumentacion(1L, "agente1"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("INICIADO");
        }
    }

    @Nested
    @DisplayName("Registro de exámenes y apto médico")
    class RegistroExamenes {

        @Test
        @DisplayName("Debe registrar un examen teórico aprobado y avanzar el estado")
        void debeRegistrarExamenTeoricoAprobado() {
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            ExamenTeorico examen = new ExamenTeorico();
            examen.setAprobado(true);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarExamenTeorico(1L, examen);

            assertThat(resultado.getExamenTeoricoAprobado()).isTrue();
            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_TEO_OK);
            verify(examenTeoricoRepository).save(examen);
        }

        @Test
        @DisplayName("Debe registrar un examen teórico desaprobado y marcar el trámite como rechazado")
        void debeRegistrarExamenTeoricoDesaprobado() {
            ExamenTeorico examen = new ExamenTeorico();
            examen.setAprobado(false);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarExamenTeorico(1L, examen);

            assertThat(resultado.getExamenTeoricoAprobado()).isFalse();
            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_TEO_RECHAZADO);
        }

        @Test
        @DisplayName("Debe rechazar registrar examen teórico si el tipo de trámite no lo requiere")
        void debeRechazarExamenTeoricoSiNoRequerido() {
            tramite.setTipo(TipoTramite.DUPLICADO);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));

            assertThatThrownBy(() -> tramiteService.registrarExamenTeorico(1L, new ExamenTeorico()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no requiere examen teórico");
        }

        @Test
        @DisplayName("Debe registrar un examen práctico aprobado y avanzar el estado")
        void debeRegistrarExamenPracticoAprobado() {
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);
            ExamenPractico examen = new ExamenPractico();
            examen.setAprobado(true);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarExamenPractico(1L, examen);

            assertThat(resultado.getExamenPracticoAprobado()).isTrue();
            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_PRA_OK);
            verify(examenPracticoRepository).save(examen);
        }

        @Test
        @DisplayName("Debe registrar un examen práctico desaprobado")
        void debeRegistrarExamenPracticoDesaprobado() {
            ExamenPractico examen = new ExamenPractico();
            examen.setAprobado(false);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarExamenPractico(1L, examen);

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_PRA_RECHAZADO);
        }

        @Test
        @DisplayName("Debe rechazar registrar examen práctico si el tipo de trámite no lo requiere")
        void debeRechazarExamenPracticoSiNoRequerido() {
            tramite.setTipo(TipoTramite.CAMBIO_DOMICILIO);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));

            assertThatThrownBy(() -> tramiteService.registrarExamenPractico(1L, new ExamenPractico()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no requiere examen práctico");
        }

        @Test
        @DisplayName("Debe registrar un apto médico favorable")
        void debeRegistrarAptoMedicoFavorable() {
            AptoMedico apto = new AptoMedico();
            apto.setApto(true);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarAptoMedico(1L, apto);

            assertThat(resultado.getAptoMedicoVigente()).isTrue();
            verify(aptoMedicoRepository).save(apto);
        }

        @Test
        @DisplayName("Debe registrar un apto médico desfavorable sin avanzar el estado")
        void debeRegistrarAptoMedicoDesfavorable() {
            AptoMedico apto = new AptoMedico();
            apto.setApto(false);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarAptoMedico(1L, apto);

            assertThat(resultado.getAptoMedicoVigente()).isNotEqualTo(true);
        }

        @Test
        @DisplayName("Debe rechazar registrar apto médico si el tipo de trámite no lo requiere")
        void debeRechazarAptoMedicoSiNoRequerido() {
            tramite.setTipo(TipoTramite.DUPLICADO);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));

            assertThatThrownBy(() -> tramiteService.registrarAptoMedico(1L, new AptoMedico()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no requiere apto médico");
        }

        @Test
        @DisplayName("Debe registrar un apto médico mediante DTO calculando vencimiento")
        void debeRegistrarAptoMedicoConDTO() {
            AptoMedicoRequestDTO request = new AptoMedicoRequestDTO();
            request.setMedicoExaminador("Dra. Lopez");
            request.setFechaExamen(LocalDateTime.now());
            request.setApto(true);
            request.setMesesValidez(12);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));
            when(aptoMedicoRepository.save(any(AptoMedico.class))).thenAnswer(inv -> inv.getArgument(0));

            AptoMedicoResponseDTO resultado = tramiteService.registrarAptoMedico(1L, request);

            assertThat(resultado.getProfesional()).isEqualTo("Dra. Lopez");
            assertThat(resultado.getApto()).isTrue();
            assertThat(tramite.getAptoMedicoVigente()).isTrue();
        }

        @Test
        @DisplayName("Debe finalizar el trámite cuando el apto médico por DTO es desfavorable")
        void debeFinalizarTramiteConAptoMedicoDesfavorablePorDTO() {
            AptoMedicoRequestDTO request = new AptoMedicoRequestDTO();
            request.setMedicoExaminador("Dra. Lopez");
            request.setFechaExamen(LocalDateTime.now());
            request.setApto(false);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));
            when(aptoMedicoRepository.save(any(AptoMedico.class))).thenAnswer(inv -> inv.getArgument(0));

            tramiteService.registrarAptoMedico(1L, request);

            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.APTO_MED_RECHAZADO);
        }

        @Test
        @DisplayName("Debe obtener el apto médico vigente de un trámite")
        void debeObtenerAptoMedico() {
            AptoMedico apto = new AptoMedico();
            apto.setApto(true);
            apto.setFecha(LocalDateTime.now());
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(aptoMedicoRepository.findUltimoAptoVigente(tramite)).thenReturn(Optional.of(apto));

            Optional<AptoMedicoResponseDTO> resultado = tramiteService.obtenerAptoMedico(1L);

            assertThat(resultado).isPresent();
        }

        @Test
        @DisplayName("Debe obtener los aptos médicos próximos a vencer")
        void debeObtenerAptosProximosAVencer() {
            AptoMedico apto = new AptoMedico();
            apto.setApto(true);
            apto.setFecha(LocalDateTime.now());
            when(aptoMedicoRepository.findAptosProximosAVencer(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(apto));

            List<AptoMedicoResponseDTO> resultado = tramiteService.obtenerAptosProximosAVencer();

            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Registro de pagos y emisión de licencias")
    class RegistroPagosYLicencias {

        @Test
        @DisplayName("Debe acreditar un pago y avanzar el estado del trámite")
        void debeAcreditarPago() {
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);
            tramite.setExamenPracticoAprobado(true);
            Pago pago = new Pago();
            pago.setEstado(EstadoPago.ACREDITADO);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarPago(1L, pago);

            assertThat(resultado.getPagoAcreditado()).isTrue();
            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.PAGO_OK);
            verify(pagoRepository).save(pago);
        }

        @Test
        @DisplayName("No debe cambiar el estado del trámite si el pago es rechazado")
        void noDebeCambiarEstadoSiPagoRechazado() {
            Pago pago = new Pago();
            pago.setEstado(EstadoPago.RECHAZADO);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.registrarPago(1L, pago);

            assertThat(resultado.getPagoAcreditado()).isFalse();
            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.INICIADO);
        }

        @Test
        @DisplayName("No debe cambiar el estado del trámite si el pago está vencido")
        void noDebeCambiarEstadoSiPagoVencido() {
            Pago pago = new Pago();
            pago.setEstado(EstadoPago.VENCIDO);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            tramiteService.registrarPago(1L, pago);

            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.INICIADO);
        }

        @Test
        @DisplayName("Debe emitir una licencia cuando todos los requisitos están cumplidos")
        void debeEmitirLicencia() {
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);
            tramite.setExamenPracticoAprobado(true);
            tramite.setPagoAcreditado(true);
            Licencia licencia = new Licencia();
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(licenciaService.emitirLicencia(tramite)).thenReturn(licencia);

            Licencia resultado = tramiteService.emitirLicencia(1L);

            assertThat(resultado).isEqualTo(licencia);
            assertThat(tramite.getEstado()).isEqualTo(EstadoTramite.EMITIDA);
        }

        @Test
        @DisplayName("Debe rechazar emitir licencia si no se cumplen todos los requisitos")
        void debeRechazarEmisionSinRequisitos() {
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));

            assertThatThrownBy(() -> tramiteService.emitirLicencia(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("requisitos");
            verify(licenciaService, never()).emitirLicencia(any());
        }

        @Test
        @DisplayName("Debe rechazar emitir licencia si el trámite ya está emitido")
        void debeRechazarEmisionSiYaEmitido() {
            tramite.setDocumentacionValidada(true);
            tramite.setAptoMedicoVigente(true);
            tramite.setExamenTeoricoAprobado(true);
            tramite.setExamenPracticoAprobado(true);
            tramite.setPagoAcreditado(true);
            tramite.setEstado(EstadoTramite.EMITIDA);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));

            assertThatThrownBy(() -> tramiteService.emitirLicencia(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ya tiene una licencia emitida");
        }
    }

    @Nested
    @DisplayName("Rechazos y reintentos")
    class RechazosYReintentos {

        @Test
        @DisplayName("Rechazar un trámite INICIADO rechaza su documentación")
        void debeRechazarTramiteIniciado() {
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarTramite(1L, "Documentación incompleta");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.DOCS_RECHAZADAS);
            assertThat(resultado.getDocumentacionValidada()).isFalse();
        }

        @Test
        @DisplayName("Rechazar un trámite en DOCS_OK que requiere examen teórico lo rechaza")
        void debeRechazarTramiteEnDocsOkConExamenTeorico() {
            tramite.setEstado(EstadoTramite.DOCS_OK);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarTramite(1L, "No cumple requisitos");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_TEO_RECHAZADO);
        }

        @Test
        @DisplayName("Rechazar un trámite en EX_TEO_OK que requiere examen práctico lo rechaza")
        void debeRechazarTramiteEnExTeoOkConExamenPractico() {
            tramite.setEstado(EstadoTramite.EX_TEO_OK);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarTramite(1L, "No aprobó práctico");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_PRA_RECHAZADO);
        }

        @Test
        @DisplayName("Rechazar un trámite de duplicado (sin exámenes) en DOCS_OK lo rechaza genéricamente")
        void debeRechazarGenericamenteTramiteSinExamenes() {
            tramite.setTipo(TipoTramite.DUPLICADO);
            tramite.setEstado(EstadoTramite.DOCS_OK);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarTramite(1L, "Motivo genérico");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.RECHAZADA);
            assertThat(resultado.getObservaciones()).isEqualTo("Motivo genérico");
        }

        @Test
        @DisplayName("Rechazar un trámite en un estado sin manejo específico lo rechaza genéricamente")
        void debeRechazarGenericamenteEnEstadoNoManejado() {
            tramite.setEstado(EstadoTramite.PAGO_OK);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarTramite(1L, "Motivo genérico");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.RECHAZADA);
        }

        @Test
        @DisplayName("Debe rechazar explícitamente la documentación de un trámite")
        void debeRechazarDocumentacionExplicitamente() {
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarDocumentacion(1L, "Falta DNI");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.DOCS_RECHAZADAS);
            assertThat(resultado.getObservaciones()).isEqualTo("Falta DNI");
        }

        @Test
        @DisplayName("Debe rechazar explícitamente el examen teórico de un trámite")
        void debeRechazarExamenTeoricoExplicitamente() {
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarExamenTeorico(1L, "Puntaje insuficiente");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_TEO_RECHAZADO);
        }

        @Test
        @DisplayName("Debe rechazar explícitamente el examen práctico de un trámite")
        void debeRechazarExamenPracticoExplicitamente() {
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.rechazarExamenPractico(1L, "Faltas graves");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.EX_PRA_RECHAZADO);
        }

        @Test
        @DisplayName("Debe permitir el reintento de un trámite con examen teórico rechazado")
        void debePermitirReintentoDesdeExamenTeoricoRechazado() {
            tramite.setEstado(EstadoTramite.EX_TEO_RECHAZADO);
            tramite.setDocumentacionValidada(true);
            when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
            when(tramiteRepository.save(any(Tramite.class))).thenAnswer(inv -> inv.getArgument(0));

            Tramite resultado = tramiteService.permitirReintento(1L, "Autorizado por supervisor");

            assertThat(resultado.getEstado()).isEqualTo(EstadoTramite.DOCS_OK);
            assertThat(resultado.getObservaciones()).isEqualTo("Autorizado por supervisor");
        }
    }

    @Nested
    @DisplayName("Consultas de trámite activo y estadísticas")
    class ConsultasActivoYEstadisticas {

        @Test
        @DisplayName("Debe obtener el trámite activo de un titular")
        void debeObtenerTramiteActivo() {
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(tramiteRepository.findTramiteActivoByTitular(any(Titular.class), anyList()))
                    .thenReturn(Optional.of(tramite));

            Optional<Tramite> resultado = tramiteService.getTramiteActivo(1L);

            assertThat(resultado).contains(tramite);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el titular no existe al buscar trámite activo")
        void debeLanzarExcepcionSiTitularNoExisteAlBuscarActivo() {
            when(titularService.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tramiteService.getTramiteActivo(99L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Debe contar trámites por tipo en un período")
        void debeContarPorTipoEnPeriodo() {
            LocalDateTime desde = LocalDateTime.now().minusDays(30);
            LocalDateTime hasta = LocalDateTime.now();
            when(tramiteRepository.countByTipoEnPeriodo(TipoTramite.EMISION, desde, hasta)).thenReturn(7L);

            Long resultado = tramiteService.getCountByTipoEnPeriodo(TipoTramite.EMISION, desde, hasta);

            assertThat(resultado).isEqualTo(7L);
        }

        @Test
        @DisplayName("Debe contar trámites por estado")
        void debeContarPorEstado() {
            when(tramiteRepository.countByEstado(EstadoTramite.INICIADO)).thenReturn(5L);

            Long resultado = tramiteService.getCountByEstado(EstadoTramite.INICIADO);

            assertThat(resultado).isEqualTo(5L);
        }

        @Test
        @DisplayName("Debe sumar los trámites activos en todos los estados intermedios")
        void debeContarTramitesActivos() {
            when(tramiteRepository.countByEstado(any(EstadoTramite.class))).thenReturn(2L);

            long resultado = tramiteService.countTramitesActivos();

            assertThat(resultado).isEqualTo(12L); // 6 estados activos * 2
        }
    }
}
