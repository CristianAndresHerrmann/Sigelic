package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entidad que representa un usuario del sistema SIGELIC con RBAC
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellido;

    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    @Column(length = 15)
    private String telefono;

    @Size(max = 20, message = "El DNI no puede exceder 20 caracteres")
    @Column(length = 20)
    private String dni;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @Column(length = 200)
    private String direccion;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RolSistema rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "cuenta_bloqueada", nullable = false)
    private Boolean cuentaBloqueada = false;

    @Column(name = "cambio_password_requerido", nullable = false)
    private Boolean cambioPasswordRequerido = true;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Size(max = 100, message = "El campo 'creado por' no puede exceder 100 caracteres")
    @Column(name = "creado_por", length = 100)
    private String creadoPor;

    @Size(max = 100, message = "El campo 'actualizado por' no puede exceder 100 caracteres")
    @Column(name = "actualizado_por", length = 100)
    private String actualizadoPor;

    /**
     * Inicializa las fechas de auditoría antes de persistir
     */
    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de modificación antes de actualizar
     */
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Obtiene los permisos del usuario basados en su rol
     */
    public Set<String> getPermisos() {
        return rol.getAuthorities();
    }

    /**
     * Verifica si el usuario tiene un permiso específico
     */
    public boolean tienePermiso(String permiso) {
        return getPermisos().contains(permiso);
    }

    /**
     * Actualiza la fecha de último acceso
     */
    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    /**
     * Incrementa el contador de intentos fallidos
     */
    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 3) {
            this.cuentaBloqueada = true;
        }
    }

    /**
     * Resetea el contador de intentos fallidos
     */
    public void resetearIntentosFallidos() {
        this.intentosFallidos = 0;
    }

    /**
     * Bloquea la cuenta del usuario
     */
    public void bloquearCuenta() {
        this.cuentaBloqueada = true;
    }

    /**
     * Desbloquea la cuenta del usuario
     */
    public void desbloquearCuenta() {
        this.cuentaBloqueada = false;
        this.resetearIntentosFallidos();
    }

    /**
     * Verifica si la cuenta está activa y no bloqueada
     */
    public boolean esCuentaValida() {
        return this.activo && !this.cuentaBloqueada;
    }

    /**
     * Obtiene el nombre completo del usuario
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", cuentaBloqueada=" + cuentaBloqueada +
                '}';
    }
}
