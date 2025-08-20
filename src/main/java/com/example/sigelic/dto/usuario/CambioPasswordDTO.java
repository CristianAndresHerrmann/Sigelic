package com.example.sigelic.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para cambio de contraseña
 */
@Data
public class CambioPasswordDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
    private String nuevaPassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPassword;
}
