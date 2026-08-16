package com.example.sigelic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para inicializar datos del sistema
 * Crea usuarios por defecto si no existen
 * Solo se ejecuta en perfiles que NO sean test
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializationService implements CommandLineRunner {

    private final UsuarioService usuarioService;

    /**
     * Contrasena inicial de las cuentas con permisos totales.
     * Los defaults valen para desarrollo local; en el perfil 'prod' se exigen
     * por variable de entorno (SIGELIC_ADMIN_PASSWORD / SIGELIC_SUPERADMIN_PASSWORD)
     * y la aplicacion no arranca si faltan.
     */
    @Value("${sigelic.seed.admin-password:Admin123!}")
    private String adminPassword;

    @Value("${sigelic.seed.superadmin-password:SuperAdmin123!}")
    private String superadminPassword;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando inicialización de datos del sistema...");

        // Se valida antes de crear nada: crearUsuariosPorDefecto() atrapa sus
        // excepciones, así que una semilla inválida pasaría inadvertida.
        validarSemilla(adminPassword, "SIGELIC_ADMIN_PASSWORD");
        validarSemilla(superadminPassword, "SIGELIC_SUPERADMIN_PASSWORD");

        crearUsuariosPorDefecto();

        log.info("Inicialización de datos completada.");
    }

    /**
     * Evita que una variable de entorno sin definir termine usada como contraseña.
     * Si el placeholder no se resolvió, el valor llega como el literal "${...}".
     */
    private void validarSemilla(String valor, String variableEntorno) {
        if (valor == null || valor.isBlank() || valor.startsWith("${")) {
            throw new IllegalStateException(
                "La variable de entorno " + variableEntorno + " no está definida. "
                + "Es obligatoria en el perfil 'prod' para fijar la contraseña inicial "
                + "de las cuentas con permisos totales.");
        }
    }

    /**
     * Crea usuarios por defecto del sistema
     */
    private void crearUsuariosPorDefecto() {
        try {
            // Crear usuario administrador por defecto
            if (!usuarioService.existsByUsername("admin")) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(adminPassword);
                admin.setEmail("admin@sigelic.gov.ar");
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setRol(RolSistema.ADMINISTRADOR);
                admin.setActivo(true);
                admin.setCuentaBloqueada(false);
                admin.setCambioPasswordRequerido(false);
                admin.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(admin);
                // La contrasena es un secreto de entorno: no debe registrarse en el log
                log.info("Usuario administrador 'admin' creado.");
            }
            
            // Crear usuario superadministrador por defecto (cuenta técnica con todos los permisos)
            if (!usuarioService.existsByUsername("superadmin")) {
                Usuario superadmin = new Usuario();
                superadmin.setUsername("superadmin");
                superadmin.setPassword(superadminPassword);
                superadmin.setEmail("superadmin@sigelic.gov.ar");
                superadmin.setNombre("Super");
                superadmin.setApellido("Administrador");
                superadmin.setRol(RolSistema.SUPERADMIN);
                superadmin.setActivo(true);
                superadmin.setCuentaBloqueada(false);
                superadmin.setCambioPasswordRequerido(false);
                superadmin.setCreadoPor("SISTEMA");

                usuarioService.crearUsuario(superadmin);
                // La contrasena es un secreto de entorno: no debe registrarse en el log
                log.info("Usuario superadministrador 'superadmin' creado.");
            }

            // Crear usuario supervisor por defecto
            if (!usuarioService.existsByUsername("supervisor")) {
                Usuario supervisor = new Usuario();
                supervisor.setUsername("supervisor");
                supervisor.setPassword("Super123!");
                supervisor.setEmail("supervisor@sigelic.gov.ar");
                supervisor.setNombre("Supervisor");
                supervisor.setApellido("Sistema");
                supervisor.setRol(RolSistema.SUPERVISOR);
                supervisor.setActivo(true);
                supervisor.setCuentaBloqueada(false);
                supervisor.setCambioPasswordRequerido(true);
                supervisor.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(supervisor);
                log.info("Usuario supervisor creado: supervisor/Super123!");
            }

            // Crear usuario agente por defecto
            if (!usuarioService.existsByUsername("agente")) {
                Usuario agente = new Usuario();
                agente.setUsername("agente");
                agente.setPassword("Agente123!");
                agente.setEmail("agente@sigelic.gov.ar");
                agente.setNombre("Agente");
                agente.setApellido("Sistema");
                agente.setRol(RolSistema.AGENTE);
                agente.setActivo(true);
                agente.setCuentaBloqueada(false);
                agente.setCambioPasswordRequerido(true);
                agente.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(agente);
                log.info("Usuario agente creado: agente/Agente123!");
            }

            // Crear usuario médico por defecto
            if (!usuarioService.existsByUsername("medico")) {
                Usuario medico = new Usuario();
                medico.setUsername("medico");
                medico.setPassword("Medico123!");
                medico.setEmail("medico@sigelic.gov.ar");
                medico.setNombre("Médico");
                medico.setApellido("Sistema");
                medico.setRol(RolSistema.MEDICO);
                medico.setActivo(true);
                medico.setCuentaBloqueada(false);
                medico.setCambioPasswordRequerido(true);
                medico.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(medico);
                log.info("Usuario médico creado: medico/Medico123!");
            }

            // Crear usuario examinador por defecto
            if (!usuarioService.existsByUsername("examinador")) {
                Usuario examinador = new Usuario();
                examinador.setUsername("examinador");
                examinador.setPassword("Examin123!");
                examinador.setEmail("examinador@sigelic.gov.ar");
                examinador.setNombre("Examinador");
                examinador.setApellido("Sistema");
                examinador.setRol(RolSistema.EXAMINADOR);
                examinador.setActivo(true);
                examinador.setCuentaBloqueada(false);
                examinador.setCambioPasswordRequerido(true);
                examinador.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(examinador);
                log.info("Usuario examinador creado: examinador/Examin123!");
            }

            // Crear usuario cajero por defecto
            if (!usuarioService.existsByUsername("cajero")) {
                Usuario cajero = new Usuario();
                cajero.setUsername("cajero");
                cajero.setPassword("Cajero123!");
                cajero.setEmail("cajero@sigelic.gov.ar");
                cajero.setNombre("Cajero");
                cajero.setApellido("Sistema");
                cajero.setRol(RolSistema.CAJERO);
                cajero.setActivo(true);
                cajero.setCuentaBloqueada(false);
                cajero.setCambioPasswordRequerido(true);
                cajero.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(cajero);
                log.info("Usuario cajero creado: cajero/Cajero123!");
            }

            // Crear usuario auditor por defecto
            if (!usuarioService.existsByUsername("auditor")) {
                Usuario auditor = new Usuario();
                auditor.setUsername("auditor");
                auditor.setPassword("Auditor123!");
                auditor.setEmail("auditor@sigelic.gov.ar");
                auditor.setNombre("Auditor");
                auditor.setApellido("Sistema");
                auditor.setRol(RolSistema.AUDITOR);
                auditor.setActivo(true);
                auditor.setCuentaBloqueada(false);
                auditor.setCambioPasswordRequerido(true);
                auditor.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(auditor);
                log.info("Usuario auditor creado: auditor/Auditor123!");
            }

            // Crear usuario ciudadano por defecto (para testing)
            if (!usuarioService.existsByUsername("ciudadano")) {
                Usuario ciudadano = new Usuario();
                ciudadano.setUsername("ciudadano");
                ciudadano.setPassword("Ciudadano123!");
                ciudadano.setEmail("ciudadano@ejemplo.com");
                ciudadano.setNombre("Juan");
                ciudadano.setApellido("Pérez");
                ciudadano.setDni("12345678");
                ciudadano.setTelefono("3424123456");
                ciudadano.setDireccion("Calle Falsa 123, Santa Fe");
                ciudadano.setRol(RolSistema.CIUDADANO);
                ciudadano.setActivo(true);
                ciudadano.setCuentaBloqueada(false);
                ciudadano.setCambioPasswordRequerido(false);
                ciudadano.setCreadoPor("SISTEMA");
                
                usuarioService.crearUsuario(ciudadano);
                log.info("Usuario ciudadano creado: ciudadano/Ciudadano123!");
            }

        } catch (Exception e) {
            log.error("Error al crear usuarios por defecto: {}", e.getMessage(), e);
        }
    }
}
