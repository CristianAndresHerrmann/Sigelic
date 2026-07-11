package com.example.sigelic.service;

import java.time.LocalDate;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoLicencia;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.MotivoDuplicacion;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.TramiteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TramiteService - Reglas de Negocio")
class TramiteServiceTest {

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private TitularService titularService;

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
}
