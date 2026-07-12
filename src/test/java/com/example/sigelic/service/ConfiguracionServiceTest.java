package com.example.sigelic.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sigelic.model.Configuracion;
import com.example.sigelic.model.Configuracion.TipoConfiguracion;
import com.example.sigelic.repository.ConfiguracionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ConfiguracionService")
class ConfiguracionServiceTest {

    @Mock
    private ConfiguracionRepository configuracionRepository;

    @InjectMocks
    private ConfiguracionService configuracionService;

    private Configuracion configuracion;

    @BeforeEach
    void setUp() {
        configuracion = new Configuracion("sistema.nombre", "SIGELIC", "Nombre del sistema", "GENERAL", TipoConfiguracion.TEXT);
        configuracion.setId(1L);
        configuracion.setModificable(true);
    }

    @Test
    @DisplayName("Debe listar todas las configuraciones")
    void debeListarTodas() {
        when(configuracionRepository.findAllOrderedByCategoriaAndClave()).thenReturn(List.of(configuracion));

        List<Configuracion> resultado = configuracionService.findAll();

        assertThat(resultado).containsExactly(configuracion);
    }

    @Test
    @DisplayName("Debe buscar configuraciones por categoría")
    void debeBuscarPorCategoria() {
        when(configuracionRepository.findByCategoriaOrderByClave("GENERAL")).thenReturn(List.of(configuracion));

        List<Configuracion> resultado = configuracionService.findByCategoria("GENERAL");

        assertThat(resultado).containsExactly(configuracion);
    }

    @Test
    @DisplayName("Debe obtener el valor de una clave existente")
    void debeObtenerValorConDefault() {
        when(configuracionRepository.findValorByClave("sistema.nombre")).thenReturn(Optional.of("SIGELIC"));

        String resultado = configuracionService.getValor("sistema.nombre", "default");

        assertThat(resultado).isEqualTo("SIGELIC");
    }

    @Test
    @DisplayName("Debe usar el valor por defecto cuando la clave no existe")
    void debeUsarValorPorDefectoCuandoNoExiste() {
        when(configuracionRepository.findValorByClave("inexistente")).thenReturn(Optional.empty());

        String resultado = configuracionService.getValor("inexistente", "default");

        assertThat(resultado).isEqualTo("default");
    }

    @Test
    @DisplayName("Debe obtener el valor como Integer con default cuando no es parseable")
    void debeObtenerValorComoIntegerConDefault() {
        Configuracion numerica = new Configuracion("licencias.validez_anos", "5", "desc", "LICENCIAS", TipoConfiguracion.INTEGER);
        when(configuracionRepository.findByClave("licencias.validez_anos")).thenReturn(Optional.of(numerica));

        Integer resultado = configuracionService.getValorComoInteger("licencias.validez_anos", 99);

        assertThat(resultado).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe usar el default de Integer cuando la clave no existe")
    void debeUsarDefaultIntegerCuandoNoExiste() {
        when(configuracionRepository.findByClave("inexistente")).thenReturn(Optional.empty());

        Integer resultado = configuracionService.getValorComoInteger("inexistente", 99);

        assertThat(resultado).isEqualTo(99);
    }

    @Test
    @DisplayName("Debe obtener el valor como Boolean")
    void debeObtenerValorComoBoolean() {
        Configuracion booleana = new Configuracion("seguridad.cambio_password_obligatorio", "true", "desc", "SEGURIDAD", TipoConfiguracion.BOOLEAN);
        when(configuracionRepository.findByClave("seguridad.cambio_password_obligatorio")).thenReturn(Optional.of(booleana));

        Boolean resultado = configuracionService.getValorComoBoolean("seguridad.cambio_password_obligatorio", false);

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Debe guardar una configuración")
    void debeGuardarConfiguracion() {
        when(configuracionRepository.save(configuracion)).thenReturn(configuracion);

        Configuracion resultado = configuracionService.save(configuracion);

        assertThat(resultado).isEqualTo(configuracion);
    }

    @Test
    @DisplayName("Debe actualizar el valor de una configuración modificable")
    void debeActualizarValorDeConfiguracionModificable() {
        when(configuracionRepository.findByClave("sistema.nombre")).thenReturn(Optional.of(configuracion));
        when(configuracionRepository.save(any(Configuracion.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Configuracion> resultado = configuracionService.actualizarValor("sistema.nombre", "Nuevo Nombre", "admin");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getValor()).isEqualTo("Nuevo Nombre");
        assertThat(resultado.get().getActualizadoPor()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Debe rechazar actualizar una configuración no modificable")
    void debeRechazarActualizarConfiguracionNoModificable() {
        configuracion.setModificable(false);
        when(configuracionRepository.findByClave("sistema.nombre")).thenReturn(Optional.of(configuracion));

        assertThatThrownBy(() -> configuracionService.actualizarValor("sistema.nombre", "Nuevo", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no es modificable");
        verify(configuracionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar vacío al actualizar una clave inexistente")
    void debeRetornarVacioAlActualizarClaveInexistente() {
        when(configuracionRepository.findByClave("inexistente")).thenReturn(Optional.empty());

        Optional<Configuracion> resultado = configuracionService.actualizarValor("inexistente", "valor", "admin");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe actualizar múltiples configuraciones ignorando valores vacíos")
    void debeActualizarMultiplesConfiguracionesIgnorandoVacios() {
        when(configuracionRepository.findByClave("sistema.nombre")).thenReturn(Optional.of(configuracion));
        when(configuracionRepository.save(any(Configuracion.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> cambios = new LinkedHashMap<>();
        cambios.put("sistema.nombre", "Nuevo Nombre");
        cambios.put("otra.clave", "  ");
        cambios.put("otra.clave2", null);

        configuracionService.actualizarConfiguraciones(cambios, "admin");

        verify(configuracionRepository, times(1)).findByClave("sistema.nombre");
        verify(configuracionRepository, never()).findByClave("otra.clave");
        verify(configuracionRepository, never()).findByClave("otra.clave2");
    }

    @Test
    @DisplayName("Debe agrupar configuraciones modificables por categoría")
    void debeAgruparConfiguracionesModificablesPorCategoria() {
        Configuracion otraGeneral = new Configuracion("contacto.email", "a@a.com", "desc", "GENERAL", TipoConfiguracion.EMAIL);
        Configuracion seguridad = new Configuracion("seguridad.max_intentos_fallidos", "3", "desc", "SEGURIDAD", TipoConfiguracion.INTEGER);
        when(configuracionRepository.findByModificableTrue()).thenReturn(List.of(configuracion, otraGeneral, seguridad));

        Map<String, List<Configuracion>> resultado = configuracionService.getConfiguracionesModificablesPorCategoria();

        assertThat(resultado.get("GENERAL")).containsExactlyInAnyOrder(configuracion, otraGeneral);
        assertThat(resultado.get("SEGURIDAD")).containsExactly(seguridad);
    }

    @Test
    @DisplayName("Debe inicializar solo las configuraciones por defecto que no existen")
    void debeInicializarSoloConfiguracionesInexistentes() {
        when(configuracionRepository.existsByClave(anyString())).thenReturn(false);
        when(configuracionRepository.existsByClave("sistema.nombre")).thenReturn(true);

        configuracionService.inicializarConfiguracionesPorDefecto();

        verify(configuracionRepository, never()).save(argThat(c -> c.getClave().equals("sistema.nombre")));
        verify(configuracionRepository, times(1)).save(argThat(c -> c.getClave().equals("sistema.url")));
    }
}
