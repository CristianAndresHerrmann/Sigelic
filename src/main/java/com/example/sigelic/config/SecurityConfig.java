package com.example.sigelic.config;

import com.example.sigelic.service.CustomUserDetailsService;
import com.example.sigelic.security.Authorities;
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
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/usuarios/perfil", "/api/usuarios/cambiar-password").authenticated()
            .requestMatchers("/api/usuarios/**").hasAuthority(Authorities.SEGURIDAD_GESTIONAR_ROLES)
            .requestMatchers("/api/seguridad/**").hasAuthority(Authorities.SEGURIDAD_GESTIONAR_ROLES)
            .requestMatchers("/api/auditoria/**").hasAuthority(Authorities.AUDITORIA_VER)
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/actuator/**", "/h2-console/**").hasAuthority(Authorities.PARAMETROS_EDITAR)
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
