package com.example.sigelic.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.sigelic.controller.TramiteController;
import com.example.sigelic.mapper.TramiteMapper;
import com.example.sigelic.service.TramiteService;

@WebMvcTest(TramiteController.class)
@Import(SensitiveControllerAuthorizationTest.RealSecurityConfiguration.class)
class SensitiveControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TramiteService tramiteService;

    @MockitoBean
    private TramiteMapper tramiteMapper;

    @Test
    void usuarioNoAutenticadoNoAccedeALaApi() throws Exception {
        mockMvc.perform(get("/api/tramites/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = Authorities.TRAMITE_VER)
    void usuarioDeConsultaNoPuedeEmitirLicencia() throws Exception {
        mockMvc.perform(post("/api/tramites/1/emitir-licencia"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tramiteService);
    }

    @Test
    @WithMockUser(authorities = Authorities.LICENCIA_EMITIR)
    void usuarioAutorizadoPuedeEmitirLicencia() throws Exception {
        mockMvc.perform(post("/api/tramites/1/emitir-licencia"))
                .andExpect(status().isOk());

        verify(tramiteService).emitirLicencia(1L);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class RealSecurityConfiguration {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll());
            return http.build();
        }
    }
}
