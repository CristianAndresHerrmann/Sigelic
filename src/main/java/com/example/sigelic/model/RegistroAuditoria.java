package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de auditoría para trazabilidad
 */
@Entity
@Table(name = "auditoria")
@Data
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La entidad es obligatoria")
    @Size(max = 50, message = "La entidad no puede exceder 50 caracteres")
    @Column(nullable = false, length = 50)
    private String entidad;

    @NotNull(message = "El ID de la entidad es obligatorio")
    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @NotBlank(message = "La operación es obligatoria")
    @Size(max = 20, message = "La operación no puede exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String operacion; // CREATE, UPDATE, DELETE

    @Size(max = 100, message = "El usuario no puede exceder 100 caracteres")
    @Column(length = 100)
    private String usuario;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "valores_anteriores", columnDefinition = "TEXT")
    private String valoresAnteriores;

    @Column(name = "valores_nuevos", columnDefinition = "TEXT")
    private String valoresNuevos;

    @Size(max = 200, message = "Los detalles no pueden exceder 200 caracteres")
    @Column(length = 200)
    private String detalles;

    @Size(max = 45, message = "La IP no puede exceder 45 caracteres")
    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;
}
