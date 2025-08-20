package com.example.sigelic.config;

import com.example.sigelic.service.CustomUserDetailsService;
import com.example.sigelic.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de seguridad para SIGELIC con sistema RBAC completo y Vaadin
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends VaadinWebSecurity {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configurar URLs públicas específicas para API
        http.authorizeHttpRequests(authz -> authz
            // API endpoints específicos
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            
            // Endpoints de gestión de usuarios - requieren permisos específicos
            .requestMatchers("/api/usuarios/**").hasAnyAuthority("USUARIOS_LEER", "USUARIOS_CREAR", "USUARIOS_EDITAR", "USUARIOS_ELIMINAR")
            
            // Endpoints de seguridad y auditoria
            .requestMatchers("/api/seguridad/**").hasAuthority("SEGURIDAD_GESTIONAR_ROLES")
            .requestMatchers("/api/auditoria/**").hasAuthority("AUDITORIA_ACCEDER_LOGS")
            
            // Endpoints de licencias
            .requestMatchers("/api/licencias/crear").hasAuthority("LICENCIAS_CREAR")
            .requestMatchers("/api/licencias/editar/**").hasAuthority("LICENCIAS_EDITAR")
            .requestMatchers("/api/licencias/eliminar/**").hasAuthority("LICENCIAS_ELIMINAR")
            .requestMatchers("/api/licencias/**").hasAnyAuthority("LICENCIAS_LEER", "LICENCIAS_CREAR", "LICENCIAS_EDITAR")
            
            // Endpoints de exámenes
            .requestMatchers("/api/examenes/crear").hasAuthority("EXAMENES_CREAR")
            .requestMatchers("/api/examenes/editar/**").hasAuthority("EXAMENES_EDITAR")
            .requestMatchers("/api/examenes/calificar/**").hasAuthority("EXAMENES_CALIFICAR")
            .requestMatchers("/api/examenes/**").hasAnyAuthority("EXAMENES_LEER", "EXAMENES_CREAR", "EXAMENES_EDITAR")
            
            // Endpoints de pagos
            .requestMatchers("/api/pagos/procesar").hasAuthority("PAGOS_PROCESAR")
            .requestMatchers("/api/pagos/reembolsar/**").hasAuthority("PAGOS_REEMBOLSAR")
            .requestMatchers("/api/pagos/**").hasAnyAuthority("PAGOS_LEER", "PAGOS_PROCESAR")
            
            // Endpoints de reportes
            .requestMatchers("/api/reportes/**").hasAuthority("REPORTES_GENERAR")
            
            // Actuator para monitoreo (solo administradores)
            .requestMatchers("/actuator/**").hasAuthority("SISTEMA_CONFIGURAR")
            
            // Consola H2 (solo para desarrollo)
            .requestMatchers("/h2-console/**").hasAuthority("SISTEMA_CONFIGURAR")
        );

        // Configuración de headers de seguridad
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin())
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true))
        );

        // Configuración específica de Vaadin
        setLoginView(http, LoginView.class);
        
        // Delegar la configuración base a VaadinWebSecurity
        super.configure(http);
    }
}
