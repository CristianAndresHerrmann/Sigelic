package com.example.sigelic.service;

import com.example.sigelic.model.Inhabilitacion;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.repository.InhabilitacionRepository;
import com.example.sigelic.repository.TitularRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TitularService")
class TitularServiceTest {

    @Mock
    private TitularRepository titularRepository;

    @Mock
    private InhabilitacionRepository inhabilitacionRepository;

    @InjectMocks
    private TitularService titularService;

    private Titular titular;
    private Titular otroTitular;
    private Inhabilitacion inhabilitacion;

    @BeforeEach
    void setUp() {
        // Crear titular principal
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");
        titular.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        titular.setEmail("juan@example.com");
        titular.setDomicilio("Calle Falsa 123");
        titular.setTelefono("123456789");
        titular.setTramites(new ArrayList<>());

        // Crear otro titular para validaciones
        otroTitular = new Titular();
        otroTitular.setId(2L);
        otroTitular.setNombre("María");
        otroTitular.setApellido("González");
        otroTitular.setDni("87654321");
        otroTitular.setFechaNacimiento(LocalDate.of(1985, 5, 5));
        otroTitular.setEmail("maria@example.com");

        // Crear inhabilitación
        inhabilitacion = new Inhabilitacion();
        inhabilitacion.setId(1L);
        inhabilitacion.setMotivo("Alcoholemia");
        inhabilitacion.setFechaInicio(LocalDate.now().minusDays(30));
        inhabilitacion.setFechaFin(LocalDate.now().plusDays(30));
        inhabilitacion.setAutoridad("Policía Provincial");
        inhabilitacion.setTitular(titular);
    }

    @Nested
    @DisplayName("Búsqueda de titulares")
    class BusquedaTitulares {

        @Test
        @DisplayName("Debe encontrar titular por ID exitosamente")
        void debeEncontrarTitularPorIdExitosamente() {
            // Given
            when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));

            // When
            Optional<Titular> resultado = titularService.findById(1L);

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNombre()).isEqualTo("Juan");
            verify(titularRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe retornar vacío cuando titular no existe")
        void debeRetornarVacioCuandoTitularNoExiste() {
            // Given
            when(titularRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Titular> resultado = titularService.findById(999L);

            // Then
            assertThat(resultado).isEmpty();
            verify(titularRepository).findById(999L);
        }

        @Test
        @DisplayName("Debe encontrar titular por DNI exitosamente")
        void debeEncontrarTitularPorDniExitosamente() {
            // Given
            when(titularRepository.findByDni("12345678")).thenReturn(Optional.of(titular));

            // When
            Optional<Titular> resultado = titularService.findByDni("12345678");

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getDni()).isEqualTo("12345678");
            verify(titularRepository).findByDni("12345678");
        }

        @Test
        @DisplayName("Debe buscar titulares por nombre")
        void debeBuscarTitularesPorNombre() {
            // Given
            List<Titular> titulares = Arrays.asList(titular);
            when(titularRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("Juan", "Juan"))
                    .thenReturn(titulares);

            // When
            List<Titular> resultado = titularService.findByNombre("Juan");

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Juan");
            verify(titularRepository).findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("Juan", "Juan");
        }

        @Test
        @DisplayName("Debe buscar titulares por nombre completo")
        void debeBuscarTitularesPorNombreCompleto() {
            // Given
            List<Titular> titulares = Arrays.asList(titular);
            when(titularRepository.findByNombreCompletoContainingIgnoreCase("Juan Pérez"))
                    .thenReturn(titulares);

            // When
            List<Titular> resultado = titularService.findByNombreCompleto("Juan Pérez");

            // Then
            assertThat(resultado).hasSize(1);
            verify(titularRepository).findByNombreCompletoContainingIgnoreCase("Juan Pérez");
        }

        @Test
        @DisplayName("Debe obtener todos los titulares")
        void debeObtenerTodosLosTitulares() {
            // Given
            List<Titular> titulares = Arrays.asList(titular, otroTitular);
            when(titularRepository.findAll()).thenReturn(titulares);

            // When
            List<Titular> resultado = titularService.findAll();

            // Then
            assertThat(resultado).hasSize(2);
            verify(titularRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Gestión CRUD de titulares")
    class GestionCRUD {

        @Test
        @DisplayName("Debe guardar titular exitosamente")
        void debeGuardarTitularExitosamente() {
            // Given
            Titular nuevoTitular = new Titular();
            nuevoTitular.setNombre("Carlos");
            nuevoTitular.setApellido("López");
            nuevoTitular.setDni("11111111");
            nuevoTitular.setFechaNacimiento(LocalDate.of(1995, 3, 3));

            when(titularRepository.existsByDni("11111111")).thenReturn(false);
            when(titularRepository.save(nuevoTitular)).thenReturn(nuevoTitular);

            // When
            Titular resultado = titularService.save(nuevoTitular);

            // Then
            assertThat(resultado).isEqualTo(nuevoTitular);
            verify(titularRepository).existsByDni("11111111");
            verify(titularRepository).save(nuevoTitular);
        }

        @Test
        @DisplayName("Debe fallar al guardar titular con DNI existente")
        void debeFallarAlGuardarTitularConDniExistente() {
            // Given
            Titular nuevoTitular = new Titular();
            nuevoTitular.setNombre("Carlos");
            nuevoTitular.setApellido("López");
            nuevoTitular.setDni("12345678"); // DNI ya existente
            nuevoTitular.setFechaNacimiento(LocalDate.of(1995, 3, 3));

            when(titularRepository.existsByDni("12345678")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> titularService.save(nuevoTitular))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un titular con el DNI: 12345678");

            verify(titularRepository).existsByDni("12345678");
            verify(titularRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar titular exitosamente")
        void debeActualizarTitularExitosamente() {
            // Given
            titular.setTelefono("987654321");
            when(titularRepository.findByDni("12345678")).thenReturn(Optional.of(titular));
            when(titularRepository.save(titular)).thenReturn(titular);

            // When
            Titular resultado = titularService.update(titular);

            // Then
            assertThat(resultado.getTelefono()).isEqualTo("987654321");
            verify(titularRepository).save(titular);
        }

        @Test
        @DisplayName("Debe fallar al actualizar titular sin ID")
        void debeFallarAlActualizarTitularSinId() {
            // Given
            titular.setId(null);

            // When & Then
            assertThatThrownBy(() -> titularService.update(titular))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El titular debe tener un ID para ser actualizado");

            verify(titularRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe eliminar titular exitosamente")
        void debeEliminarTitularExitosamente() {
            // Given
            when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));

            // When
            titularService.delete(1L);

            // Then
            verify(titularRepository).findById(1L);
            verify(titularRepository).delete(titular);
        }

        @Test
        @DisplayName("Debe fallar al eliminar titular con trámites activos")
        void debeFallarAlEliminarTitularConTramitesActivos() {
            // Given
            Tramite tramiteActivo = new Tramite();
            tramiteActivo.setEstado(EstadoTramite.INICIADO);
            titular.getTramites().add(tramiteActivo);

            when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));

            // When & Then
            assertThatThrownBy(() -> titularService.delete(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No se puede eliminar un titular con trámites activos");

            verify(titularRepository).findById(1L);
            verify(titularRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Validaciones de datos")
    class ValidacionesDatos {

        @Test
        @DisplayName("Debe fallar con DNI vacío")
        void debeFallarConDniVacio() {
            // Given
            titular.setDni("");

            // When & Then
            assertThatThrownBy(() -> titularService.save(titular))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El DNI es obligatorio");
        }

        @Test
        @DisplayName("Debe fallar con nombre vacío")
        void debeFallarConNombreVacio() {
            // Given
            titular.setNombre("");

            // When & Then
            assertThatThrownBy(() -> titularService.save(titular))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre es obligatorio");
        }

        @Test
        @DisplayName("Debe fallar con apellido vacío")
        void debeFallarConApellidoVacio() {
            // Given
            titular.setApellido("");

            // When & Then
            assertThatThrownBy(() -> titularService.save(titular))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El apellido es obligatorio");
        }

        @Test
        @DisplayName("Debe fallar con fecha de nacimiento nula")
        void debeFallarConFechaNacimientoNula() {
            // Given
            titular.setFechaNacimiento(null);

            // When & Then
            assertThatThrownBy(() -> titularService.save(titular))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La fecha de nacimiento es obligatoria");
        }

        @Test
        @DisplayName("Debe fallar con email duplicado")
        void debeFallarConEmailDuplicado() {
            // Given
            Titular nuevoTitular = new Titular();
            nuevoTitular.setNombre("Carlos");
            nuevoTitular.setApellido("López");
            nuevoTitular.setDni("11111111");
            nuevoTitular.setFechaNacimiento(LocalDate.of(1995, 3, 3));
            nuevoTitular.setEmail("juan@example.com"); // Email ya existente

            when(titularRepository.existsByDni("11111111")).thenReturn(false);
            when(titularRepository.existsByEmail("juan@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> titularService.save(nuevoTitular))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un titular con el email: juan@example.com");
        }
    }

    @Nested
    @DisplayName("Gestión de inhabilitaciones")
    class GestionInhabilitaciones {

        @Test
        @DisplayName("Debe verificar que titular puede iniciar trámite")
        void debeVerificarQueTitularPuedeIniciarTramite() {
            // Given
            when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));

            // When
            boolean resultado = titularService.puedeIniciarTramite(1L);

            // Then
            assertThat(resultado).isTrue();
            verify(titularRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe obtener inhabilitaciones activas")
        void debeObtenerInhabilitacionesActivas() {
            // Given
            List<Inhabilitacion> inhabilitaciones = Arrays.asList(inhabilitacion);
            when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));
            when(inhabilitacionRepository.findInhabilitacionesActivasByTitular(titular))
                    .thenReturn(inhabilitaciones);

            // When
            List<Inhabilitacion> resultado = titularService.getInhabilitacionesActivas(1L);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getMotivo()).isEqualTo("Alcoholemia");
            verify(inhabilitacionRepository).findInhabilitacionesActivasByTitular(titular);
        }

        @Test
        @DisplayName("Debe agregar inhabilitación exitosamente")
        void debeAgregarInhabilitacionExitosamente() {
            // Given
            Inhabilitacion nuevaInhabilitacion = new Inhabilitacion();
            nuevaInhabilitacion.setMotivo("Exceso de velocidad");
            nuevaInhabilitacion.setFechaInicio(LocalDate.now());
            nuevaInhabilitacion.setFechaFin(LocalDate.now().plusDays(60));

            when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));
            when(inhabilitacionRepository.save(nuevaInhabilitacion)).thenReturn(nuevaInhabilitacion);

            // When
            Inhabilitacion resultado = titularService.agregarInhabilitacion(1L, nuevaInhabilitacion);

            // Then
            assertThat(resultado.getTitular()).isEqualTo(titular);
            assertThat(resultado.getMotivo()).isEqualTo("Exceso de velocidad");
            verify(inhabilitacionRepository).save(nuevaInhabilitacion);
        }

        @Test
        @DisplayName("Debe obtener titulares con inhabilitaciones activas")
        void debeObtenerTitularesConInhabilitacionesActivas() {
            // Given
            List<Titular> titularesInhabilitados = Arrays.asList(titular);
            when(titularRepository.findTitularesConInhabilitacionesActivas())
                    .thenReturn(titularesInhabilitados);

            // When
            List<Titular> resultado = titularService.getTitularesConInhabilitacionesActivas();

            // Then
            assertThat(resultado).hasSize(1);
            verify(titularRepository).findTitularesConInhabilitacionesActivas();
        }
    }

    @Nested
    @DisplayName("Verificaciones de existencia")
    class VerificacionesExistencia {

        @Test
        @DisplayName("Debe verificar existencia por DNI")
        void debeVerificarExistenciaPorDni() {
            // Given
            when(titularRepository.existsByDni("12345678")).thenReturn(true);

            // When
            boolean resultado = titularService.existsByDni("12345678");

            // Then
            assertThat(resultado).isTrue();
            verify(titularRepository).existsByDni("12345678");
        }

        @Test
        @DisplayName("Debe verificar existencia por email")
        void debeVerificarExistenciaPorEmail() {
            // Given
            when(titularRepository.existsByEmail("juan@example.com")).thenReturn(true);

            // When
            boolean resultado = titularService.existsByEmail("juan@example.com");

            // Then
            assertThat(resultado).isTrue();
            verify(titularRepository).existsByEmail("juan@example.com");
        }
    }
}
