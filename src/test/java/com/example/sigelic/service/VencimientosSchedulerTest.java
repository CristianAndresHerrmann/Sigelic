package com.example.sigelic.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import com.example.sigelic.security.Authorities;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de VencimientosScheduler")
class VencimientosSchedulerTest {

    @Mock
    private PagoService pagoService;

    @Mock
    private LicenciaService licenciaService;

    @InjectMocks
    private VencimientosScheduler vencimientosScheduler;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe procesar vencimientos diarios y autenticar como SYSTEM con permisos requeridos")
    void debeProcesarVencimientosDiariosYAutenticarComoSystem() {
        // Given
        doAnswer(invocation -> {
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isNotNull();
            assertThat(context.getAuthentication().getName()).isEqualTo("SYSTEM");
            assertThat(context.getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(
                    Authorities.PROCESO_VENCIMIENTOS_EJECUTAR,
                    Authorities.LICENCIA_GESTIONAR_ESTADO
                );
            return null;
        }).when(pagoService).procesarPagosVencidos();

        doAnswer(invocation -> {
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isNotNull();
            assertThat(context.getAuthentication().getName()).isEqualTo("SYSTEM");
            return null;
        }).when(licenciaService).actualizarLicenciasVencidas();

        // When
        vencimientosScheduler.procesarVencimientosDiarios();

        // Then
        verify(pagoService).procesarPagosVencidos();
        verify(licenciaService).actualizarLicenciasVencidas();
        
        // El contexto se debe limpiar tras finalizar si no había contexto previo
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
