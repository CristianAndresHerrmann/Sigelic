package com.example.sigelic.service;

import java.time.LocalDate;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoLicencia;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.LicenciaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de LicenciaService - Reglas de Negocio")
class LicenciaServiceTest {

    @Mock
    private LicenciaRepository licenciaRepository;

    @InjectMocks
    private LicenciaService licenciaService;

    private Titular titular;
    private Tramite tramite;
    private Licencia licenciaVigente;

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
        titular.setDomicilio("Calle Falsa 123");

        // Crear trámite completo
        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTitular(titular);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setClaseSolicitada(ClaseLicencia.B);
        tramite.setEstado(EstadoTramite.PAGO_OK);
        tramite.setDocumentacionValidada(true);
        tramite.setAptoMedicoVigente(true);
        tramite.setExamenTeoricoAprobado(true);
        tramite.setExamenPracticoAprobado(true);
        tramite.setPagoAcreditado(true);

        // Crear licencia vigente
        licenciaVigente = new Licencia();
        licenciaVigente.setId(1L);
        licenciaVigente.setTitular(titular);
        licenciaVigente.setClase(ClaseLicencia.B);
        licenciaVigente.setFechaEmision(LocalDate.now().minusYears(1));
        licenciaVigente.setFechaVencimiento(LocalDate.now().plusYears(4));
        licenciaVigente.setEstado(EstadoLicencia.VIGENTE);
        licenciaVigente.setNumeroLicencia("12345678");
    }

    @Nested
    @DisplayName("Emisión de Licencias")
    class EmisionLicencias {

        @Test
        @DisplayName("Debe emitir licencia cuando todos los requisitos están cumplidos")
        void debeEmitirLicenciaCuandoRequisitosCumplidos() {
            // Given
            when(licenciaRepository.save(any(Licencia.class))).thenAnswer(i -> {
                Licencia licencia = i.getArgument(0);
                licencia.setId(1L);
                return licencia;
            });

            // When
            Licencia resultado = licenciaService.emitirLicencia(tramite);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTitular()).isEqualTo(titular);
            assertThat(resultado.getClase()).isEqualTo(ClaseLicencia.B);
            assertThat(resultado.getEstado()).isEqualTo(EstadoLicencia.VIGENTE);
            assertThat(resultado.getFechaEmision()).isEqualTo(LocalDate.now());
            assertThat(resultado.getNumeroLicencia()).isNotNull();
            
            verify(licenciaRepository).save(any(Licencia.class));
        }

        @Test
        @DisplayName("No debe emitir licencia cuando faltan requisitos")
        void noDebeEmitirLicenciaCuandoFaltanRequisitos() {
            // Given
            tramite.setPagoAcreditado(false); // Falta pago

            // When & Then
            assertThatThrownBy(() -> licenciaService.emitirLicencia(tramite))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No se puede emitir licencia sin cumplir todos los requisitos");
            
            verify(licenciaRepository, never()).save(any(Licencia.class));
        }

        @Test
        @DisplayName("Debe calcular vigencia correcta para primera emisión (25 años)")
        void debeCalcularVigenciaCorrectaPrimeraEmision() {
            // Given
            when(licenciaRepository.save(any(Licencia.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Licencia resultado = licenciaService.emitirLicencia(tramite);

            // Then
            // Para 25 años primera vez = 5 años de vigencia
            LocalDate fechaEsperada = LocalDate.now().plusYears(5);
            assertThat(resultado.getFechaVencimiento()).isEqualTo(fechaEsperada);
        }

        @Test
        @DisplayName("Debe calcular vigencia correcta para titular joven (19 años)")
        void debeCalcularVigenciaCorrectaTitularJoven() {
            // Given
            titular.setFechaNacimiento(LocalDate.now().minusYears(19));
            when(licenciaRepository.save(any(Licencia.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Licencia resultado = licenciaService.emitirLicencia(tramite);

            // Then
            // Para 19 años primera vez = 1 año de vigencia
            LocalDate fechaEsperada = LocalDate.now().plusYears(1);
            assertThat(resultado.getFechaVencimiento()).isEqualTo(fechaEsperada);
        }

        @Test
        @DisplayName("Debe calcular vigencia correcta para titular mayor (65 años)")
        void debeCalcularVigenciaCorrectaTitularMayor() {
            // Given
            titular.setFechaNacimiento(LocalDate.now().minusYears(65));
            when(licenciaRepository.save(any(Licencia.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Licencia resultado = licenciaService.emitirLicencia(tramite);

            // Then
            // Para 65 años = 3 años de vigencia
            LocalDate fechaEsperada = LocalDate.now().plusYears(3);
            assertThat(resultado.getFechaVencimiento()).isEqualTo(fechaEsperada);
        }
    }

    @Nested
    @DisplayName("Duplicación de Licencias")
    class DuplicacionLicencias {

        @Test
        @DisplayName("Debe duplicar licencia vigente correctamente")
        void debeDuplicarLicenciaVigente() {
            // Given
            tramite.setTipo(TipoTramite.DUPLICADO);
            when(licenciaRepository.save(any(Licencia.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Licencia duplicado = licenciaService.duplicarLicencia(licenciaVigente, tramite);

            // Then
            assertThat(duplicado).isNotNull();
            assertThat(duplicado.getFechaVencimiento()).isEqualTo(licenciaVigente.getFechaVencimiento());
            assertThat(duplicado.getEstado()).isEqualTo(EstadoLicencia.VIGENTE);
            assertThat(duplicado.getObservaciones()).contains("Duplicado de licencia");
            
            // Verificar que la licencia original fue marcada como duplicada
            assertThat(licenciaVigente.getEstado()).isEqualTo(EstadoLicencia.DUPLICADA);
            
            verify(licenciaRepository, times(2)).save(any(Licencia.class));
        }

        @Test
        @DisplayName("No debe duplicar licencia no vigente")
        void noDebeDuplicarLicenciaNoVigente() {
            // Given
            licenciaVigente.setEstado(EstadoLicencia.VENCIDA);
            tramite.setTipo(TipoTramite.DUPLICADO);

            // When & Then
            assertThatThrownBy(() -> licenciaService.duplicarLicencia(licenciaVigente, tramite))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Solo se pueden duplicar licencias vigentes");
            
            verify(licenciaRepository, never()).save(any(Licencia.class));
        }
    }

    @Nested
    @DisplayName("Renovación de Licencias")
    class RenovacionLicencias {

        @Test
        @DisplayName("Debe renovar licencia correctamente")
        void debeRenovarLicenciaCorrectamente() {
            // Given
            tramite.setTipo(TipoTramite.RENOVACION);
            when(licenciaRepository.save(any(Licencia.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Licencia renovada = licenciaService.renovarLicencia(licenciaVigente, tramite);

            // Then
            assertThat(renovada).isNotNull();
            assertThat(renovada.getFechaEmision()).isEqualTo(LocalDate.now());
            assertThat(renovada.getEstado()).isEqualTo(EstadoLicencia.VIGENTE);
            
            // La fecha de vencimiento debe ser nueva (no la de la licencia anterior)
            assertThat(renovada.getFechaVencimiento()).isAfter(licenciaVigente.getFechaVencimiento());
            
            // Verificar que la licencia anterior fue marcada como duplicada
            assertThat(licenciaVigente.getEstado()).isEqualTo(EstadoLicencia.DUPLICADA);
            
            verify(licenciaRepository, times(2)).save(any(Licencia.class));
        }
    }

    @Nested
    @DisplayName("Consultas de Licencias")
    class ConsultasLicencias {

        @Test
        @DisplayName("Debe encontrar licencia por ID")
        void debeEncontrarLicenciaPorId() {
            // Given
            when(licenciaRepository.findById(1L)).thenReturn(Optional.of(licenciaVigente));

            // When
            Optional<Licencia> resultado = licenciaService.findById(1L);

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get()).isEqualTo(licenciaVigente);
            verify(licenciaRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe encontrar licencias por titular")
        void debeEncontrarLicenciasPorTitular() {
            // Given
            List<Licencia> licencias = Arrays.asList(licenciaVigente);
            when(licenciaRepository.findByTitular(titular)).thenReturn(licencias);

            // When
            List<Licencia> resultado = licenciaService.findByTitular(titular);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0)).isEqualTo(licenciaVigente);
            verify(licenciaRepository).findByTitular(titular);
        }

        @Test
        @DisplayName("Debe encontrar licencias vigentes por titular")
        void debeEncontrarLicenciasVigentesPorTitular() {
            // Given
            List<Licencia> licenciasVigentes = Arrays.asList(licenciaVigente);
            when(licenciaRepository.findLicenciasVigentesByTitular(titular)).thenReturn(licenciasVigentes);

            // When
            List<Licencia> resultado = licenciaService.findLicenciasVigentesByTitular(titular);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getEstado()).isEqualTo(EstadoLicencia.VIGENTE);
            verify(licenciaRepository).findLicenciasVigentesByTitular(titular);
        }
    }

    @Nested
    @DisplayName("Actualización de Domicilio")
    class ActualizacionDomicilio {

        @Test
        @DisplayName("Debe actualizar domicilio en licencia vigente")
        void debeActualizarDomicilioEnLicenciaVigente() {
            // Given
            String nuevoDomicilio = "Nueva Dirección 456";
            when(licenciaRepository.save(any(Licencia.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Licencia resultado = licenciaService.actualizarDomicilio(licenciaVigente, nuevoDomicilio);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getEstado()).isEqualTo(EstadoLicencia.VIGENTE);
            
            // Verificar que la licencia anterior fue marcada como duplicada
            assertThat(licenciaVigente.getEstado()).isEqualTo(EstadoLicencia.DUPLICADA);
            
            verify(licenciaRepository, times(2)).save(any(Licencia.class));
        }

        @Test
        @DisplayName("No debe actualizar domicilio en licencia no vigente")
        void noDebeActualizarDomicilioEnLicenciaNoVigente() {
            // Given
            licenciaVigente.setEstado(EstadoLicencia.VENCIDA);
            String nuevoDomicilio = "Nueva Dirección 456";

            // When & Then
            assertThatThrownBy(() -> licenciaService.actualizarDomicilio(licenciaVigente, nuevoDomicilio))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Solo se puede actualizar el domicilio de licencias vigentes");
            
            verify(licenciaRepository, never()).save(any(Licencia.class));
        }
    }
}
