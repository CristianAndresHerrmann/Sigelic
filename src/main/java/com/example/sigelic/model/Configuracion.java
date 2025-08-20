package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa la configuración del sistema SIGELIC
 * Maneja parámetros de configuración como clave-valor
 */
@Entity
@Table(name = "configuracion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La clave de configuración es obligatoria")
    @Size(max = 100, message = "La clave no puede exceder 100 caracteres")
    @Column(unique = true, nullable = false, length = 100)
    private String clave;

    @NotBlank(message = "El valor de configuración es obligatorio")
    @Size(max = 500, message = "El valor no puede exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String valor;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String descripcion;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    @Column(nullable = false, length = 50)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConfiguracion tipo = TipoConfiguracion.TEXT;

    @Column(nullable = false)
    private Boolean modificable = true;

    @Size(max = 100, message = "El usuario no puede exceder 100 caracteres")
    @Column(name = "actualizado_por", length = 100)
    private String actualizadoPor;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Enum que define los tipos de configuración disponibles
     */
    public enum TipoConfiguracion {
        TEXT,           // Texto libre
        INTEGER,        // Número entero
        BOOLEAN,        // Verdadero/Falso
        EMAIL,          // Dirección de email
        URL,            // URL válida
        PHONE,          // Número de teléfono
        PASSWORD        // Contraseña (se mostrará oculta)
    }

    /**
     * Constructor con parámetros principales
     */
    public Configuracion(String clave, String valor, String descripcion, String categoria, TipoConfiguracion tipo) {
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.tipo = tipo;
    }

    /**
     * Constructor con parámetros principales y modificabilidad
     */
    public Configuracion(String clave, String valor, String descripcion, String categoria, TipoConfiguracion tipo, Boolean modificable) {
        this(clave, valor, descripcion, categoria, tipo);
        this.modificable = modificable;
    }

    /**
     * Obtiene el valor como entero
     */
    public Integer getValorComoInteger() {
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obtiene el valor como booleano
     */
    public Boolean getValorComoBoolean() {
        return Boolean.valueOf(valor);
    }

    /**
     * Establece el valor desde un entero
     */
    public void setValorDesdeInteger(Integer valorEntero) {
        this.valor = valorEntero != null ? valorEntero.toString() : "";
    }

    /**
     * Establece el valor desde un booleano
     */
    public void setValorDesdeBoolean(Boolean valorBoolean) {
        this.valor = valorBoolean != null ? valorBoolean.toString() : "false";
    }
}
