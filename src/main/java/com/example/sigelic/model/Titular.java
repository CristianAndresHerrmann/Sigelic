package com.example.sigelic.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad que representa a un titular de licencia de conducir
 */
@Entity
@Table(name = "titulares")
@Data
@EqualsAndHashCode(exclude = {"licencias", "tramites", "turnos"})
@ToString(exclude = {"licencias", "tramites", "turnos"})
public class Titular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 7, max = 8, message = "El DNI debe tener entre 7 y 8 dígitos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El domicilio es obligatorio")
    @Size(max = 200, message = "El domicilio no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String domicilio;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @OneToMany(mappedBy = "titular", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inhabilitacion> inhabilitaciones = new ArrayList<>();

    @OneToMany(mappedBy = "titular", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Licencia> licencias = new ArrayList<>();

    @OneToMany(mappedBy = "titular", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tramite> tramites = new ArrayList<>();

    @OneToMany(mappedBy = "titular", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Turno> turnos = new ArrayList<>();

    /**
     * Calcula la edad del titular a la fecha actual
     */
    public int getEdad() {
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }

    /**
     * Calcula la edad del titular a una fecha específica
     */
    public int getEdadEn(LocalDate fecha) {
        return fecha.getYear() - fechaNacimiento.getYear();
    }

    /**
     * Verifica si el titular tiene inhabilitaciones activas
     */
    public boolean tieneInhabilitacionesActivas() {
        return inhabilitaciones.stream()
                .anyMatch(Inhabilitacion::isActiva);
    }

    /**
     * Obtiene el nombre completo del titular
     */
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }
}
