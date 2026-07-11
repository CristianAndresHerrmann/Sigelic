package com.example.sigelic.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.CostoTramite;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.repository.CostoTramiteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CostoTramiteService - Parametrización de costos")
class CostoTramiteServiceTest {

    @Mock
    private CostoTramiteRepository costoTramiteRepository;

    @InjectMocks
    private CostoTramiteService costoTramiteService;

    private CostoTramite costoVigente;

    @BeforeEach
    void setUp() {
        costoVigente = new CostoTramite();
        costoVigente.setId(1L);
        costoVigente.setTipoTramite(TipoTramite.EMISION);
        costoVigente.setClaseLicencia(ClaseLicencia.B);
        costoVigente.setCosto(new BigDecimal("35000.00"));
        costoVigente.setFechaVigenciaDesde(LocalDate.of(2025, 1, 1));
        costoVigente.setFechaVigenciaHasta(null);
        costoVigente.setActivo(true);
    }

    @Test
    @DisplayName("Actualizar un costo cierra la vigencia del costo vigente anterior")
    void actualizarCostoCierraVigenciaDelAnterior() {
        // Given
        LocalDate desde = LocalDate.now();
        when(costoTramiteRepository.findCostoVigente(TipoTramite.EMISION, ClaseLicencia.B, desde))
                .thenReturn(Optional.of(costoVigente));
        when(costoTramiteRepository.save(any(CostoTramite.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CostoTramite nuevo = costoTramiteService.actualizarCosto(TipoTramite.EMISION, ClaseLicencia.B,
                new BigDecimal("40000.00"), desde, null, "Actualización de tarifa");

        // Then: el anterior queda cerrado el día previo y el nuevo activo sin límite
        assertThat(costoVigente.getFechaVigenciaHasta()).isEqualTo(desde.minusDays(1));
        assertThat(nuevo.getCosto()).isEqualByComparingTo("40000.00");
        assertThat(nuevo.getActivo()).isTrue();
        assertThat(nuevo.getFechaVigenciaDesde()).isEqualTo(desde);
        verify(costoTramiteRepository, times(2)).save(any(CostoTramite.class));
    }

    @Test
    @DisplayName("Actualizar un costo sin costo vigente previo no cierra nada")
    void actualizarCostoSinVigentePrevio() {
        // Given
        LocalDate desde = LocalDate.now();
        when(costoTramiteRepository.findCostoVigente(TipoTramite.DUPLICADO, ClaseLicencia.A, desde))
                .thenReturn(Optional.empty());
        when(costoTramiteRepository.save(any(CostoTramite.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CostoTramite nuevo = costoTramiteService.actualizarCosto(TipoTramite.DUPLICADO, ClaseLicencia.A,
                new BigDecimal("15000.00"), desde, null, null);

        // Then
        ArgumentCaptor<CostoTramite> captor = ArgumentCaptor.forClass(CostoTramite.class);
        verify(costoTramiteRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(nuevo);
        assertThat(nuevo.getTipoTramite()).isEqualTo(TipoTramite.DUPLICADO);
    }

    @Test
    @DisplayName("Debe rechazar un costo menor o igual a cero")
    void debeRechazarCostoNoPositivo() {
        assertThatThrownBy(() -> costoTramiteService.actualizarCosto(TipoTramite.EMISION, ClaseLicencia.B,
                BigDecimal.ZERO, LocalDate.now(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
        verify(costoTramiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar una vigencia con fin anterior al inicio")
    void debeRechazarVigenciaInvertida() {
        LocalDate desde = LocalDate.now();
        assertThatThrownBy(() -> costoTramiteService.actualizarCosto(TipoTramite.EMISION, ClaseLicencia.B,
                new BigDecimal("1000.00"), desde, desde.minusDays(1), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anterior al inicio");
        verify(costoTramiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Desactivar un costo activo lo marca inactivo")
    void desactivarCostoActivo() {
        // Given
        when(costoTramiteRepository.findById(1L)).thenReturn(Optional.of(costoVigente));
        when(costoTramiteRepository.save(any(CostoTramite.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CostoTramite resultado = costoTramiteService.desactivarCosto(1L);

        // Then
        assertThat(resultado.getActivo()).isFalse();
        verify(costoTramiteRepository).save(costoVigente);
    }

    @Test
    @DisplayName("Desactivar un costo ya inactivo lanza excepción")
    void desactivarCostoYaInactivo() {
        // Given
        costoVigente.setActivo(false);
        when(costoTramiteRepository.findById(1L)).thenReturn(Optional.of(costoVigente));

        // When & Then
        assertThatThrownBy(() -> costoTramiteService.desactivarCosto(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("desactivado");
        verify(costoTramiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Desactivar un costo inexistente lanza excepción")
    void desactivarCostoInexistente() {
        // Given
        when(costoTramiteRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> costoTramiteService.desactivarCosto(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }
}
