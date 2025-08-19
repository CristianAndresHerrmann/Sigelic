package com.example.sigelic.service;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoTramite;
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
