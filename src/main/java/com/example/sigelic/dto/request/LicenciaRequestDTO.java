package com.example.sigelic.dto.request;

import com.example.sigelic.model.ClaseLicencia;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de request para Licencia
 */
public class LicenciaRequestDTO {

    @NotNull(message = "El ID del titular es obligatorio")
    private Long titularId;

    @NotNull(message = "La clase de licencia es obligatoria")
    private ClaseLicencia clase;

    // Constructors
    public LicenciaRequestDTO() {}

    public LicenciaRequestDTO(Long titularId, ClaseLicencia clase) {
        this.titularId = titularId;
        this.clase = clase;
    }

    // Getters and Setters
    public Long getTitularId() {
        return titularId;
    }

    public void setTitularId(Long titularId) {
        this.titularId = titularId;
    }

    public ClaseLicencia getClase() {
        return clase;
    }

    public void setClase(ClaseLicencia clase) {
        this.clase = clase;
    }
}
