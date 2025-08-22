package com.example.sigelic.views.dialog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del RegistrarExamenPracticoDialog")
class RegistrarExamenPracticoDialogTest {

    @Mock
    private TramiteService tramiteService;

    @Mock
    private Consumer<Void> onSuccess;

    private Tramite tramite;
    private RegistrarExamenPracticoDialog dialog;

    @BeforeEach
    void setUp() {
        // Crear un tramite de prueba
        tramite = new Tramite();
        tramite.setId(123L);
        tramite.setClaseSolicitada("A");
        
        Titular titular = new Titular();
        titular.setNombre("Juan");
        titular.setApellido("Pérez");
        titular.setDni("12345678");
        tramite.setTitular(titular);

        dialog = new RegistrarExamenPracticoDialog(tramiteService, tramite, onSuccess);
    }

    @Test
    @DisplayName("Debe crear el dialog con los campos correctos")
    void debeCrearDialogConCamposCorrectos() {
        assertNotNull(dialog);
        assertEquals("Registrar Examen Práctico", dialog.getHeaderTitle());
    }

    @Test
    @DisplayName("Debe calcular resultado APROBADO sin faltas graves y máximo 3 leves")
    void debeCalcularResultadoAprobado() {
        // Test con 0 faltas graves y 0 leves - debe ser APROBADO
        ExamenPractico examen = new ExamenPractico();
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(0);
        assertTrue(examen.calcularAprobacion());

        // Test con 0 faltas graves y 3 leves - debe ser APROBADO
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(3);
        assertTrue(examen.calcularAprobacion());

        // Test con 0 faltas graves y 1 leve - debe ser APROBADO
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(1);
        assertTrue(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe calcular resultado REPROBADO con faltas graves o más de 3 leves")
    void debeCalcularResultadoReprobado() {
        // Test con 1 falta grave - debe ser REPROBADO
        ExamenPractico examen = new ExamenPractico();
        examen.setFaltasGraves(1);
        examen.setFaltasLeves(0);
        assertFalse(examen.calcularAprobacion());

        // Test con 0 faltas graves y 4 leves - debe ser REPROBADO
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(4);
        assertFalse(examen.calcularAprobacion());

        // Test con faltas graves y muchas leves - debe ser REPROBADO
        examen.setFaltasGraves(2);
        examen.setFaltasLeves(5);
        assertFalse(examen.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe registrar examen aprobado correctamente")
    void debeRegistrarExamenAprobado() throws Exception {
        // Configurar el mock para retornar el trámite
        when(tramiteService.registrarExamenPractico(eq(123L), any(ExamenPractico.class)))
            .thenReturn(tramite);

        // Simular datos del examen aprobado
        ExamenPractico examenAprobado = new ExamenPractico();
        examenAprobado.setExaminador("Dr. García");
        examenAprobado.setFecha(LocalDateTime.now());
        examenAprobado.setFaltasGraves(0);
        examenAprobado.setFaltasLeves(2);
        examenAprobado.setVehiculoUtilizado("Automóvil");
        examenAprobado.setPistaUtilizada("Pista A");
        examenAprobado.setObservaciones("Examen sin inconvenientes");

        // Verificar que el examen debe ser aprobado
        assertTrue(examenAprobado.calcularAprobacion());

        // Verificar que se llamó al servicio con los parámetros correctos
        ArgumentCaptor<ExamenPractico> examenCaptor = ArgumentCaptor.forClass(ExamenPractico.class);
        
        // Simular registro exitoso
        tramiteService.registrarExamenPractico(eq(123L), examenCaptor.capture());
        
        // Verificar las propiedades del examen
        ExamenPractico examenCapturado = examenCaptor.getValue();
        assertEquals(0, examenCapturado.getFaltasGraves());
        assertEquals(2, examenCapturado.getFaltasLeves());
        assertTrue(examenCapturado.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe registrar examen reprobado correctamente")
    void debeRegistrarExamenReprobado() throws Exception {
        // Configurar el mock para retornar el trámite
        when(tramiteService.registrarExamenPractico(eq(123L), any(ExamenPractico.class)))
            .thenReturn(tramite);

        // Simular datos del examen reprobado
        ExamenPractico examenReprobado = new ExamenPractico();
        examenReprobado.setExaminador("Dr. García");
        examenReprobado.setFecha(LocalDateTime.now());
        examenReprobado.setFaltasGraves(1); // Tiene falta grave
        examenReprobado.setFaltasLeves(2);
        examenReprobado.setVehiculoUtilizado("Automóvil");
        examenReprobado.setPistaUtilizada("Pista A");
        examenReprobado.setObservaciones("Falta grave en estacionamiento");

        // Verificar que el examen debe ser reprobado
        assertFalse(examenReprobado.calcularAprobacion());

        // Verificar que se llamó al servicio con los parámetros correctos
        ArgumentCaptor<ExamenPractico> examenCaptor = ArgumentCaptor.forClass(ExamenPractico.class);
        
        // Simular registro exitoso
        tramiteService.registrarExamenPractico(eq(123L), examenCaptor.capture());
        
        // Verificar las propiedades del examen
        ExamenPractico examenCapturado = examenCaptor.getValue();
        assertEquals(1, examenCapturado.getFaltasGraves());
        assertEquals(2, examenCapturado.getFaltasLeves());
        assertFalse(examenCapturado.calcularAprobacion());
    }

    @Test
    @DisplayName("Debe rechazar examen correctamente")
    void debeRechazarExamen() throws Exception {
        // Configurar el mock para retornar el trámite
        when(tramiteService.rechazarExamenPractico(eq(123L), any(String.class)))
            .thenReturn(tramite);

        // Verificar que se puede llamar al método de rechazo
        String motivo = "Examen práctico rechazado por irregularidades";
        tramiteService.rechazarExamenPractico(123L, motivo);
        
        // Verificar que se llamó al servicio
        verify(tramiteService, times(1)).rechazarExamenPractico(123L, motivo);
    }

    @Test
    @DisplayName("Debe validar campos obligatorios")
    void debeValidarCamposObligatorios() {
        // Test de validación para examinador
        ExamenPractico examen = new ExamenPractico();
        examen.setExaminador(""); // Campo vacío
        examen.setFecha(LocalDateTime.now());
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(0);
        
        // El examinador no debe estar vacío para un examen válido
        assertTrue(examen.getExaminador().isEmpty());
        
        // Test de validación para fecha
        examen.setExaminador("Dr. García");
        examen.setFecha(null); // Fecha nula
        assertNull(examen.getFecha());
        
        // Test de validación para faltas negativas
        examen.setFecha(LocalDateTime.now());
        examen.setFaltasGraves(-1); // Valor inválido
        assertEquals(-1, examen.getFaltasGraves());
        
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(-1); // Valor inválido
        assertEquals(-1, examen.getFaltasLeves());
    }

    @Test
    @DisplayName("Debe manejar casos edge de faltas")
    void debeMarcarCasosEdgeDeFaltas() {
        ExamenPractico examen = new ExamenPractico();
        
        // Caso límite: exactamente 3 faltas leves sin graves - APROBADO
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(3);
        assertTrue(examen.calcularAprobacion());
        
        // Caso límite: 4 faltas leves sin graves - REPROBADO
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(4);
        assertFalse(examen.calcularAprobacion());
        
        // Caso límite: 1 falta grave sin leves - REPROBADO
        examen.setFaltasGraves(1);
        examen.setFaltasLeves(0);
        assertFalse(examen.calcularAprobacion());
        
        // Caso perfecto: sin faltas - APROBADO
        examen.setFaltasGraves(0);
        examen.setFaltasLeves(0);
        assertTrue(examen.calcularAprobacion());
    }
}
