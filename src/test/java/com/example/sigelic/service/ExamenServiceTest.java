package com.example.sigelic.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.ExamenPracticoRepository;
import com.example.sigelic.repository.ExamenTeoricoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ExamenService")
class ExamenServiceTest {

    @Mock
    private ExamenTeoricoRepository examenTeoricoRepository;

    @Mock
    private ExamenPracticoRepository examenPracticoRepository;

    @InjectMocks
    private ExamenService examenService;

    private Tramite tramite;
    private ExamenTeorico examenTeorico;
    private ExamenPractico examenPractico;

    @BeforeEach
    void setUp() {
        tramite = new Tramite();
        tramite.setId(1L);

        examenTeorico = new ExamenTeorico();
        examenTeorico.setId(1L);
        examenTeorico.setTramite(tramite);

        examenPractico = new ExamenPractico();
        examenPractico.setId(1L);
        examenPractico.setTramite(tramite);
    }

    @Test
    @DisplayName("Debe listar todos los exámenes teóricos")
    void debeListarTodosLosTeoricos() {
        when(examenTeoricoRepository.findAllWithTramite()).thenReturn(List.of(examenTeorico));

        List<ExamenTeorico> resultado = examenService.findAllTeoricos();

        assertThat(resultado).containsExactly(examenTeorico);
    }

    @Test
    @DisplayName("Debe listar todos los exámenes prácticos")
    void debeListarTodosLosPracticos() {
        when(examenPracticoRepository.findAllWithTramite()).thenReturn(List.of(examenPractico));

        List<ExamenPractico> resultado = examenService.findAllPracticos();

        assertThat(resultado).containsExactly(examenPractico);
    }

    @Test
    @DisplayName("Debe guardar un examen teórico")
    void debeGuardarExamenTeorico() {
        when(examenTeoricoRepository.save(examenTeorico)).thenReturn(examenTeorico);

        ExamenTeorico resultado = examenService.saveExamenTeorico(examenTeorico);

        assertThat(resultado).isEqualTo(examenTeorico);
        verify(examenTeoricoRepository).save(examenTeorico);
    }

    @Test
    @DisplayName("Debe guardar un examen práctico")
    void debeGuardarExamenPractico() {
        when(examenPracticoRepository.save(examenPractico)).thenReturn(examenPractico);

        ExamenPractico resultado = examenService.saveExamenPractico(examenPractico);

        assertThat(resultado).isEqualTo(examenPractico);
        verify(examenPracticoRepository).save(examenPractico);
    }

    @Test
    @DisplayName("Debe buscar exámenes teóricos por trámite")
    void debeBuscarTeoricosPorTramite() {
        when(examenTeoricoRepository.findByTramite(tramite)).thenReturn(List.of(examenTeorico));

        List<ExamenTeorico> resultado = examenService.findTeoricosByTramite(tramite);

        assertThat(resultado).containsExactly(examenTeorico);
    }

    @Test
    @DisplayName("Debe buscar exámenes prácticos por trámite")
    void debeBuscarPracticosPorTramite() {
        when(examenPracticoRepository.findByTramite(tramite)).thenReturn(List.of(examenPractico));

        List<ExamenPractico> resultado = examenService.findPracticosByTramite(tramite);

        assertThat(resultado).containsExactly(examenPractico);
    }

    @Test
    @DisplayName("Debe buscar exámenes teóricos por examinador")
    void debeBuscarTeoricosPorExaminador() {
        when(examenTeoricoRepository.findByExaminador("Dr. Gomez")).thenReturn(List.of(examenTeorico));

        List<ExamenTeorico> resultado = examenService.findTeoricosByExaminador("Dr. Gomez");

        assertThat(resultado).containsExactly(examenTeorico);
    }

    @Test
    @DisplayName("Debe buscar exámenes teóricos en un período")
    void debeBuscarTeoricosPorPeriodo() {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now();
        when(examenTeoricoRepository.findExamenesEnPeriodo(desde, hasta)).thenReturn(List.of(examenTeorico));

        List<ExamenTeorico> resultado = examenService.findTeoricosByPeriodo(desde, hasta);

        assertThat(resultado).containsExactly(examenTeorico);
    }

    @Test
    @DisplayName("Debe obtener el último examen teórico aprobado")
    void debeObtenerUltimoTeoricoAprobado() {
        when(examenTeoricoRepository.findUltimoExamenAprobado(tramite)).thenReturn(Optional.of(examenTeorico));

        Optional<ExamenTeorico> resultado = examenService.findUltimoTeoricoAprobado(tramite);

        assertThat(resultado).contains(examenTeorico);
    }

    @Test
    @DisplayName("Debe calcular el promedio de puntajes en un período")
    void debeCalcularPromedioTeoricos() {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now();
        when(examenTeoricoRepository.findPuntajePromedioEnPeriodo(desde, hasta)).thenReturn(85.5);

        Double resultado = examenService.calcularPromedioTeoricos(desde, hasta);

        assertThat(resultado).isEqualTo(85.5);
    }

    @Test
    @DisplayName("Debe contar teóricos aprobados en un período")
    void debeContarTeoricosAprobados() {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now();
        when(examenTeoricoRepository.countAprobadosEnPeriodo(desde, hasta)).thenReturn(10L);

        Long resultado = examenService.contarTeoricosAprobados(desde, hasta);

        assertThat(resultado).isEqualTo(10L);
    }

    @Test
    @DisplayName("Debe contar el total de teóricos en un período")
    void debeContarTeoricosTotal() {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now();
        when(examenTeoricoRepository.countTotalEnPeriodo(desde, hasta)).thenReturn(15L);

        Long resultado = examenService.contarTeoricosTotal(desde, hasta);

        assertThat(resultado).isEqualTo(15L);
    }

    @Test
    @DisplayName("Debe eliminar un examen teórico")
    void debeEliminarExamenTeorico() {
        examenService.deleteExamenTeorico(1L);

        verify(examenTeoricoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe eliminar un examen práctico")
    void debeEliminarExamenPractico() {
        examenService.deleteExamenPractico(1L);

        verify(examenPracticoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe buscar un examen teórico por ID")
    void debeBuscarExamenTeoricoPorId() {
        when(examenTeoricoRepository.findById(1L)).thenReturn(Optional.of(examenTeorico));

        Optional<ExamenTeorico> resultado = examenService.findExamenTeoricoById(1L);

        assertThat(resultado).contains(examenTeorico);
    }

    @Test
    @DisplayName("Debe buscar un examen práctico por ID")
    void debeBuscarExamenPracticoPorId() {
        when(examenPracticoRepository.findById(1L)).thenReturn(Optional.of(examenPractico));

        Optional<ExamenPractico> resultado = examenService.findExamenPracticoById(1L);

        assertThat(resultado).contains(examenPractico);
    }

    @Test
    @DisplayName("Debe contar los exámenes pendientes sumando teóricos y prácticos")
    void debeContarExamenesPendientes() {
        when(examenTeoricoRepository.countByAprobadoFalseOrNull()).thenReturn(3L);
        when(examenPracticoRepository.countByAprobadoFalseOrNull()).thenReturn(2L);

        long resultado = examenService.countExamenesPendientes();

        assertThat(resultado).isEqualTo(5L);
    }
}
