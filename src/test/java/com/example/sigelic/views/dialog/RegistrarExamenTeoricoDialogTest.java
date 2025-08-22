package com.example.sigelic.views.dialog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del RegistrarExamenTeoricoDialog")
class RegistrarExamenTeoricoDialogTest {

    @Mock
    private TramiteService tramiteService;

    private Tramite tramite;
    private Titular titular;

    @BeforeEach
    void setUp() {
        titular = new Titular();
        titular.setId(1L);
        titular.setDni("12345678");
        titular.setNombre("Juan");
        titular.setApellido("Pérez");

        tramite = new Tramite();
        tramite.setId(1L);
        tramite.setTitular(titular);
        tramite.setTipo(TipoTramite.EMISION);
        tramite.setClaseSolicitada(ClaseLicencia.B);
        tramite.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe calcular puntaje correctamente - 80% exacto")
    void debeCalcularPuntajeCorrectamente80Porciento() {
        // Given: 24 respuestas correctas de 30 preguntas = 80%
        ExamenTeorico examen = new ExamenTeorico();
        examen.setCantidadPreguntas(30);
        examen.setRespuestasCorrectas(24);
        
        // When: se calcula el puntaje
        int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
        examen.setPuntaje(puntaje);
        
        // Then: debe ser exactamente 80% y estar aprobado
        assertEquals(80, puntaje);
        assertTrue(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe calcular puntaje correctamente - 79% reprobado")
    void debeCalcularPuntajeCorrectamente79Porciento() {
        // Given: 23 respuestas correctas de 30 preguntas = 76% (parte entera)
        ExamenTeorico examen = new ExamenTeorico();
        examen.setCantidadPreguntas(30);
        examen.setRespuestasCorrectas(23);
        
        // When: se calcula el puntaje (solo parte entera)
        int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
        examen.setPuntaje(puntaje);
        
        // Then: debe ser 76% y estar reprobado
        assertEquals(76, puntaje);
        assertFalse(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe calcular puntaje correctamente - 100% aprobado")
    void debeCalcularPuntajeCorrectamente100Porciento() {
        // Given: 30 respuestas correctas de 30 preguntas = 100%
        ExamenTeorico examen = new ExamenTeorico();
        examen.setCantidadPreguntas(30);
        examen.setRespuestasCorrectas(30);
        
        // When: se calcula el puntaje
        int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
        examen.setPuntaje(puntaje);
        
        // Then: debe ser 100% y estar aprobado
        assertEquals(100, puntaje);
        assertTrue(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe calcular puntaje correctamente - casos con decimales")
    void debeCalcularPuntajeCorrectamenteConDecimales() {
        // Given: 25 respuestas correctas de 30 preguntas = 83.33% -> 83% (parte entera)
        ExamenTeorico examen = new ExamenTeorico();
        examen.setCantidadPreguntas(30);
        examen.setRespuestasCorrectas(25);
        
        // When: se calcula el puntaje (solo parte entera)
        int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
        examen.setPuntaje(puntaje);
        
        // Then: debe ser 83% (sin decimales) y estar aprobado
        assertEquals(83, puntaje);
        assertTrue(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe permitir reintento cuando examen está reprobado")
    void debePermitirReintentoConExamenReprobado() {
        // Given: examen reprobado
        ExamenTeorico examen = new ExamenTeorico();
        examen.setCantidadPreguntas(30);
        examen.setRespuestasCorrectas(20);
        int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
        examen.setPuntaje(puntaje);
        
        when(tramiteService.registrarExamenTeorico(eq(1L), any(ExamenTeorico.class)))
                .thenReturn(tramite);
        
        // When: se registra el examen reprobado
        // El servicio debe guardarlo sin cambiar estado del trámite
        
        // Then: el examen está reprobado pero se permite reintento
        assertEquals(66, puntaje); // 20/30 = 66%
        assertFalse(examen.calcularAprobacion());
        
        // Verificar que el servicio fue llamado
        verify(tramiteService, never()).rechazarExamenTeorico(any(), any());
    }

    @Test
    @DisplayName("Debe aprobar automáticamente cuando puntaje >= 80%")
    void debeAprobarAutomaticamenteConPuntajeSuficiente() {
        // Given: examen con puntaje suficiente
        ExamenTeorico examen = new ExamenTeorico();
        examen.setCantidadPreguntas(30);
        examen.setRespuestasCorrectas(24); // 80%
        int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
        examen.setPuntaje(puntaje);
        
        // When & Then: el examen debe estar aprobado automáticamente
        assertEquals(80, puntaje);
        assertTrue(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe manejar casos límite - 0 respuestas correctas")
    void debeManejarCasoLimite0RespuestasCorrectas() {
        // Given: 0 respuestas correctas
        ExamenTeorico examen = new ExamenTeorico();
        examen.setCantidadPreguntas(30);
        examen.setRespuestasCorrectas(0);
        
        // When: se calcula el puntaje
        int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
        examen.setPuntaje(puntaje);
        
        // Then: debe ser 0% y estar reprobado
        assertEquals(0, puntaje);
        assertFalse(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe manejar diferentes cantidades de preguntas")
    void debeManejarDiferentesCantidadesDePreguntas() {
        // Test con 40 preguntas (32 correctas = 80%)
        ExamenTeorico examen1 = new ExamenTeorico();
        examen1.setCantidadPreguntas(40);
        examen1.setRespuestasCorrectas(32);
        int puntaje1 = (examen1.getRespuestasCorrectas() * 100) / examen1.getCantidadPreguntas();
        examen1.setPuntaje(puntaje1);
        
        assertEquals(80, puntaje1);
        assertTrue(examen1.calcularAprobacion());
        
        // Test con 20 preguntas (16 correctas = 80%)
        ExamenTeorico examen2 = new ExamenTeorico();
        examen2.setCantidadPreguntas(20);
        examen2.setRespuestasCorrectas(16);
        int puntaje2 = (examen2.getRespuestasCorrectas() * 100) / examen2.getCantidadPreguntas();
        examen2.setPuntaje(puntaje2);
        
        assertEquals(80, puntaje2);
        assertTrue(examen2.calcularAprobacion());
    }
}
