package com.example.sigelic.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.sigelic.config.TestSecurityConfig;
import com.example.sigelic.dto.request.AptoMedicoRequestDTO;
import com.example.sigelic.dto.response.AptoMedicoResponseDTO;
import com.example.sigelic.service.TramiteService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AptoMedicoController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de AptoMedicoController")
class AptoMedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TramiteService tramiteService;

    @Autowired
    private ObjectMapper objectMapper;

    private AptoMedicoRequestDTO requestDTO;
    private AptoMedicoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new AptoMedicoRequestDTO();
        requestDTO.setMedicoExaminador("Dra. Lopez");
        requestDTO.setFechaExamen(LocalDateTime.now());
        requestDTO.setApto(true);
        requestDTO.setAgudezaVisualOjoDerecho(1.0);
        requestDTO.setAgudezaVisualOjoIzquierdo(1.0);
        requestDTO.setCampoVisualNormal(true);
        requestDTO.setVisionCromaticaNormal(true);
        requestDTO.setAudicionNormal(true);
        requestDTO.setReflejosNormales(true);
        requestDTO.setCoordinacionNormal(true);
        requestDTO.setEquilibrioNormal(true);
        requestDTO.setCardiovascularNormal(true);
        requestDTO.setSistemaLocomotorNormal(true);
        requestDTO.setMesesValidez(12);

        responseDTO = AptoMedicoResponseDTO.builder()
                .id(1L)
                .profesional("Dra. Lopez")
                .apto(true)
                .fecha(LocalDateTime.now())
                .fechaVencimiento(LocalDate.now().plusMonths(12))
                .vigente(true)
                .build();
    }

    @Test
    @DisplayName("Debe registrar un apto médico exitosamente")
    void debeRegistrarAptoMedicoExitosamente() throws Exception {
        when(tramiteService.registrarAptoMedico(anyLong(), any(AptoMedicoRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/tramites/1/apto-medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profesional").value("Dra. Lopez"))
                .andExpect(jsonPath("$.apto").value(true));
    }

    @Test
    @DisplayName("Debe obtener el apto médico de un trámite")
    void debeObtenerAptoMedico() throws Exception {
        when(tramiteService.obtenerAptoMedico(1L)).thenReturn(Optional.of(responseDTO));

        mockMvc.perform(get("/api/tramites/1/apto-medico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profesional").value("Dra. Lopez"));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando el trámite no tiene apto médico registrado")
    void debeRetornar404CuandoNoHayAptoMedico() throws Exception {
        when(tramiteService.obtenerAptoMedico(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tramites/99/apto-medico"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe obtener los aptos médicos próximos a vencer")
    void debeObtenerAptosProximosAVencer() throws Exception {
        when(tramiteService.obtenerAptosProximosAVencer()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/aptos-medicos/proximos-vencer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].profesional").value("Dra. Lopez"));
    }

    @Test
    @DisplayName("Debe responder el placeholder de estadísticas de aptos médicos")
    void debeResponderEstadisticasPlaceholder() throws Exception {
        mockMvc.perform(get("/api/aptos-medicos/estadisticas"))
                .andExpect(status().isOk());
    }
}
