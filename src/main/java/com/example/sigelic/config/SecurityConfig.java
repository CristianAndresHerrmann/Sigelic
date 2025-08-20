package com.example.sigelic.config;

import com.example.sigelic.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para SIGELIC con sistema RBAC completo
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración CSRF
            .csrf(csrf -> csrf.disable())
            
            // Configuración de sesiones
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            
            // Configuración de autorización
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/", "/login", "/logout", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                
                // API públicos (registro, recuperación de contraseña, etc.)
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
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración de login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // Configuración de headers de seguridad
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
            )
            
            // Configuración de login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
