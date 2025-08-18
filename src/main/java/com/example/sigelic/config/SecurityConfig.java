package com.example.sigelic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para SIGELIC
 * Permite acceso libre a los endpoints de la API para facilitar el desarrollo y testing
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración CSRF
            .csrf(csrf -> csrf.disable())
            
            // Configuración de autorización
            .authorizeHttpRequests(authz -> authz
                // Permitir acceso libre a los endpoints de la API
                .requestMatchers("/api/**").permitAll()
                
                // Permitir acceso a recursos estáticos
                .requestMatchers("/", "/login", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                
                // Permitir acceso a Vaadin
                .requestMatchers("/VAADIN/**").permitAll()
                .requestMatchers("/vaadinServlet/**").permitAll()
                .requestMatchers("/frontend/**").permitAll()
                
                // Permitir acceso a Actuator (monitoreo)
                .requestMatchers("/actuator/**").permitAll()
                
                // Permitir acceso a la consola H2 (si se habilita en el futuro)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración de headers de seguridad
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Permitir frames del mismo origen
            )
            
            // Configuración de login (formulario por defecto de Spring Security)
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
