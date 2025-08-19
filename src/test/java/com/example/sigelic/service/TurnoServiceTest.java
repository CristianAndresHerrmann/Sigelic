package com.example.sigelic.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.EstadoTurno;
import com.example.sigelic.model.Recurso;
import com.example.sigelic.model.TipoRecurso;
import com.example.sigelic.model.TipoTurno;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Turno;
import com.example.sigelic.repository.RecursoRepository;
import com.example.sigelic.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TurnoService")
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private TitularService titularService;

    @InjectMocks
    private TurnoService turnoService;

    private Titular titular;
    private Recurso recurso;
    private Turno turno;
    private LocalDateTime inicioTurno;
    private LocalDateTime finTurno;

    @BeforeEach
    void setUp() {
        // Crear titular
        titular = new Titular();
        titular.setId(1L);
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");

        // Crear recurso
        recurso = new Recurso();
        recurso.setId(1L);
        recurso.setNombre("Oficina 1");
        recurso.setTipo(TipoRecurso.BOX);
        recurso.setActivo(true);
        recurso.setCapacidad(1);
        recurso.setHoraInicio(LocalTime.of(8, 0));
        recurso.setHoraFin(LocalTime.of(18, 0));

        // Crear turno
        inicioTurno = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        finTurno = inicioTurno.plusHours(1);

        turno = new Turno();
        turno.setId(1L);
        turno.setTitular(titular);
        turno.setTipo(TipoTurno.DOCUMENTACION);
        turno.setInicio(inicioTurno);
        turno.setFin(finTurno);
        turno.setRecurso(recurso);
        turno.setTipoRecurso(TipoRecurso.BOX);
        turno.setEstado(EstadoTurno.RESERVADO);
    }

    @Nested
    @DisplayName("Búsqueda de turnos")
    class BusquedaTurnos {

        @Test
        @DisplayName("Debe encontrar turno por ID exitosamente")
        void debeEncontrarTurnoPorIdExitosamente() {
            // Given
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

            // When
            Optional<Turno> resultado = turnoService.findById(1L);

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getTipo()).isEqualTo(TipoTurno.DOCUMENTACION);
            verify(turnoRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe obtener turnos por titular")
        void debeObtenerTurnosPorTitular() {
            // Given
            List<Turno> turnos = Arrays.asList(turno);
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(turnoRepository.findByTitular(titular)).thenReturn(turnos);

            // When
            List<Turno> resultado = turnoService.findByTitular(1L);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTitular()).isEqualTo(titular);
            verify(titularService).findById(1L);
            verify(turnoRepository).findByTitular(titular);
        }

        @Test
        @DisplayName("Debe fallar al buscar turnos de titular inexistente")
        void debeFallarAlBuscarTurnosDeTitularInexistente() {
            // Given
            when(titularService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> turnoService.findByTitular(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Titular no encontrado con ID: 999");

            verify(titularService).findById(999L);
            verifyNoInteractions(turnoRepository);
        }

        @Test
        @DisplayName("Debe obtener turnos por recurso")
        void debeObtenerTurnosPorRecurso() {
            // Given
            List<Turno> turnos = Arrays.asList(turno);
            when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
            when(turnoRepository.findByRecurso(recurso)).thenReturn(turnos);

            // When
            List<Turno> resultado = turnoService.findByRecurso(1L);

            // Then
            assertThat(resultado).hasSize(1);
            verify(recursoRepository).findById(1L);
            verify(turnoRepository).findByRecurso(recurso);
        }

        @Test
        @DisplayName("Debe obtener turnos en período")
        void debeObtenerTurnosEnPeriodo() {
            // Given
            LocalDateTime desde = LocalDateTime.now();
            LocalDateTime hasta = LocalDateTime.now().plusDays(7);
            List<Turno> turnos = Arrays.asList(turno);
            when(turnoRepository.findTurnosEnPeriodo(desde, hasta)).thenReturn(turnos);

            // When
            List<Turno> resultado = turnoService.findTurnosEnPeriodo(desde, hasta);

            // Then
            assertThat(resultado).hasSize(1);
            verify(turnoRepository).findTurnosEnPeriodo(desde, hasta);
        }
    }

    @Nested
    @DisplayName("Reserva de turnos")
    class ReservaTurnos {

        @Test
        @DisplayName("Debe reservar turno exitosamente")
        void debeReservarTurnoExitosamente() {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
            when(turnoRepository.findTurnosSolapadosDelTitular(titular, TipoTurno.DOCUMENTACION, inicioTurno, finTurno))
                    .thenReturn(new ArrayList<>());
            when(turnoRepository.findTurnosConflictivos(recurso, inicioTurno, finTurno))
                    .thenReturn(new ArrayList<>());
            when(turnoRepository.save(any(Turno.class))).thenReturn(turno);

            // When
            Turno resultado = turnoService.reservarTurno(1L, TipoTurno.DOCUMENTACION, inicioTurno, finTurno, 1L, null);

            // Then
            assertThat(resultado).isNotNull();
            verify(titularService).findById(1L);
            verify(recursoRepository).findById(1L);
            verify(turnoRepository).save(any(Turno.class));
        }

        @Test
        @DisplayName("Debe fallar al reservar turno con recurso inactivo")
        void debeFallarAlReservarTurnoConRecursoInactivo() {
            // Given
            recurso.setActivo(false);
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));

            // When & Then
            assertThatThrownBy(() -> turnoService.reservarTurno(1L, TipoTurno.DOCUMENTACION, inicioTurno, finTurno, 1L, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El recurso no está disponible");

            verify(titularService).findById(1L);
            verify(recursoRepository).findById(1L);
            verifyNoMoreInteractions(turnoRepository);
        }

        @Test
        @DisplayName("Debe fallar al reservar turno solapado para mismo titular")
        void debeFallarAlReservarTurnoSolapadoParaMismoTitular() {
            // Given
            Turno turnoExistente = new Turno();
            turnoExistente.setTitular(titular);
            turnoExistente.setTipo(TipoTurno.DOCUMENTACION);

            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
            when(turnoRepository.findTurnosSolapadosDelTitular(titular, TipoTurno.DOCUMENTACION, inicioTurno, finTurno))
                    .thenReturn(Arrays.asList(turnoExistente));

            // When & Then
            assertThatThrownBy(() -> turnoService.reservarTurno(1L, TipoTurno.DOCUMENTACION, inicioTurno, finTurno, 1L, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El titular ya tiene un turno del mismo tipo en ese horario");

            verify(turnoRepository).findTurnosSolapadosDelTitular(titular, TipoTurno.DOCUMENTACION, inicioTurno, finTurno);
        }

        @Test
        @DisplayName("Debe fallar al reservar turno con recurso ocupado")
        void debeFallarAlReservarTurnoConRecursoOcupado() {
            // Given
            Turno turnoConflictivo = new Turno();
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
            when(turnoRepository.findTurnosSolapadosDelTitular(titular, TipoTurno.DOCUMENTACION, inicioTurno, finTurno))
                    .thenReturn(new ArrayList<>());
            when(turnoRepository.findTurnosConflictivos(recurso, inicioTurno, finTurno))
                    .thenReturn(Arrays.asList(turnoConflictivo));

            // When & Then
            assertThatThrownBy(() -> turnoService.reservarTurno(1L, TipoTurno.DOCUMENTACION, inicioTurno, finTurno, 1L, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El recurso no está disponible en ese horario");
        }
    }

    @Nested
    @DisplayName("Gestión de estados de turno")
    class GestionEstados {

        @Test
        @DisplayName("Debe confirmar turno exitosamente")
        void debeConfirmarTurnoExitosamente() {
            // Given
            turno.setEstado(EstadoTurno.RESERVADO);
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
            when(turnoRepository.save(turno)).thenReturn(turno);

            // When
            Turno resultado = turnoService.confirmarTurno(1L);

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoTurno.CONFIRMADO);
            verify(turnoRepository).findById(1L);
            verify(turnoRepository).save(turno);
        }

        @Test
        @DisplayName("Debe fallar al confirmar turno que no está reservado")
        void debeFallarAlConfirmarTurnoQueNoEstaReservado() {
            // Given
            turno.setEstado(EstadoTurno.COMPLETADO);
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

            // When & Then
            assertThatThrownBy(() -> turnoService.confirmarTurno(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Solo se pueden confirmar turnos en estado RESERVADO");

            verify(turnoRepository).findById(1L);
            verify(turnoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe completar turno exitosamente")
        void debeCompletarTurnoExitosamente() {
            // Given
            turno.setEstado(EstadoTurno.CONFIRMADO);
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
            when(turnoRepository.save(turno)).thenReturn(turno);

            // When
            Turno resultado = turnoService.completarTurno(1L, "Trámite completado exitosamente");

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoTurno.COMPLETADO);
            assertThat(resultado.getObservaciones()).isEqualTo("Trámite completado exitosamente");
            verify(turnoRepository).save(turno);
        }

        @Test
        @DisplayName("Debe cancelar turno exitosamente")
        void debeCancelarTurnoExitosamente() {
            // Given
            turno.setEstado(EstadoTurno.RESERVADO);
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
            when(turnoRepository.save(turno)).thenReturn(turno);

            // When
            Turno resultado = turnoService.cancelarTurno(1L, "Cancelado por el titular");

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoTurno.CANCELADO);
            verify(turnoRepository).save(turno);
        }

        @Test
        @DisplayName("Debe fallar al cancelar turno completado")
        void debeFallarAlCancelarTurnoCompletado() {
            // Given
            turno.setEstado(EstadoTurno.COMPLETADO);
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

            // When & Then
            assertThatThrownBy(() -> turnoService.cancelarTurno(1L, "Motivo"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No se puede cancelar un turno completado");

            verify(turnoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe marcar turno como ausente")
        void debeMarcarTurnoComoAusente() {
            // Given
            turno.setEstado(EstadoTurno.CONFIRMADO);
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
            when(turnoRepository.save(turno)).thenReturn(turno);

            // When
            Turno resultado = turnoService.marcarAusente(1L);

            // Then
            assertThat(resultado.getEstado()).isEqualTo(EstadoTurno.AUSENTE);
            verify(turnoRepository).save(turno);
        }
    }

    @Nested
    @DisplayName("Gestión de profesionales")
    class GestionProfesionales {

        @Test
        @DisplayName("Debe asignar profesional exitosamente")
        void debeAsignarProfesionalExitosamente() {
            // Given
            when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
            when(turnoRepository.save(turno)).thenReturn(turno);

            // When
            Turno resultado = turnoService.asignarProfesional(1L, "Dr. García");

            // Then
            assertThat(resultado.getProfesionalAsignado()).isEqualTo("Dr. García");
            verify(turnoRepository).save(turno);
        }

        @Test
        @DisplayName("Debe obtener turnos de profesional en período")
        void debeObtenerTurnosDeProfesionalEnPeriodo() {
            // Given
            LocalDateTime desde = LocalDateTime.now();
            LocalDateTime hasta = LocalDateTime.now().plusDays(7);
            List<Turno> turnos = Arrays.asList(turno);
            when(turnoRepository.findTurnosByProfesionalEnPeriodo("Dr. García", desde, hasta))
                    .thenReturn(turnos);

            // When
            List<Turno> resultado = turnoService.getTurnosByProfesionalEnPeriodo("Dr. García", desde, hasta);

            // Then
            assertThat(resultado).hasSize(1);
            verify(turnoRepository).findTurnosByProfesionalEnPeriodo("Dr. García", desde, hasta);
        }
    }

    @Nested
    @DisplayName("Estadísticas y consultas")
    class EstadisticasConsultas {

        @Test
        @DisplayName("Debe obtener estadísticas por estado")
        void debeObtenerEstadisticasPorEstado() {
            // Given
            LocalDateTime desde = LocalDateTime.now();
            LocalDateTime hasta = LocalDateTime.now().plusDays(7);
            when(turnoRepository.countByEstadoEnPeriodo(EstadoTurno.COMPLETADO, desde, hasta))
                    .thenReturn(5L);

            // When
            Long resultado = turnoService.getCountByEstadoEnPeriodo(EstadoTurno.COMPLETADO, desde, hasta);

            // Then
            assertThat(resultado).isEqualTo(5L);
            verify(turnoRepository).countByEstadoEnPeriodo(EstadoTurno.COMPLETADO, desde, hasta);
        }

        @Test
        @DisplayName("Debe verificar si titular puede reservar turno")
        void debeVerificarSiTitularPuedeReservarTurno() {
            // Given
            when(titularService.puedeIniciarTramite(1L)).thenReturn(true);

            // When
            boolean resultado = turnoService.puedeReservarTurno(1L, TipoTurno.DOCUMENTACION);

            // Then
            assertThat(resultado).isTrue();
            verify(titularService).puedeIniciarTramite(1L);
        }

        @Test
        @DisplayName("Debe obtener próximos turnos del titular")
        void debeObtenerProximosTurnosDelTitular() {
            // Given
            List<Turno> turnos = Arrays.asList(turno);
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(turnoRepository.findByTitularAndEstado(titular, EstadoTurno.CONFIRMADO))
                    .thenReturn(turnos);

            // When
            List<Turno> resultado = turnoService.getProximosTurnos(1L);

            // Then
            assertThat(resultado).hasSize(1);
            verify(titularService).findById(1L);
            verify(turnoRepository).findByTitularAndEstado(titular, EstadoTurno.CONFIRMADO);
        }
    }

    @Nested
    @DisplayName("Manejo de errores")
    class ManejoErrores {

        @Test
        @DisplayName("Debe fallar con turno inexistente")
        void debeFallarConTurnoInexistente() {
            // Given
            when(turnoRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> turnoService.confirmarTurno(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Turno no encontrado con ID: 999");

            verify(turnoRepository).findById(999L);
        }

        @Test
        @DisplayName("Debe fallar con titular inexistente al reservar")
        void debeFallarConTitularInexistenteAlReservar() {
            // Given
            when(titularService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> turnoService.reservarTurno(999L, TipoTurno.DOCUMENTACION, inicioTurno, finTurno, 1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Titular no encontrado con ID: 999");

            verify(titularService).findById(999L);
        }

        @Test
        @DisplayName("Debe fallar con recurso inexistente al reservar")
        void debeFallarConRecursoInexistenteAlReservar() {
            // Given
            when(titularService.findById(1L)).thenReturn(Optional.of(titular));
            when(recursoRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> turnoService.reservarTurno(1L, TipoTurno.DOCUMENTACION, inicioTurno, finTurno, 999L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Recurso no encontrado con ID: 999");

            verify(titularService).findById(1L);
            verify(recursoRepository).findById(999L);
        }
    }
}
