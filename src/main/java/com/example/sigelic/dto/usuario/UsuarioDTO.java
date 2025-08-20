package com.example.sigelic.dto.usuario;

import com.example.sigelic.model.RolSistema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private String dni;
    private String direccion;
    private RolSistema rol;
    private Boolean activo;
    private Boolean cuentaBloqueada;
    private Boolean cambioPasswordRequerido;
    private Integer intentosFallidos;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String creadoPor;
    private String actualizadoPor;
}
