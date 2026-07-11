package com.example.sigelic.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.CostoTramite;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.repository.CostoTramiteRepository;
import com.example.sigelic.security.Authorities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la administración de costos de trámites (tabla de parametrización)
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CostoTramiteService {

    private final CostoTramiteRepository costoTramiteRepository;

    /**
     * Lista todos los costos registrados
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + Authorities.PARAMETROS_EDITAR + "')")
    public List<CostoTramite> findAll() {
        return costoTramiteRepository.findAll();
    }

    /**
     * Actualiza el costo vigente de una combinación tipo de trámite + clase de licencia ya existente.
     * Las combinaciones son fijas (parametrización cargada por migración); esta operación no crea
     * combinaciones nuevas, sino que registra un nuevo valor para una combinación existente,
     * cerrando la vigencia del costo actual el día anterior al inicio del nuevo para evitar solapamientos.
     */
    @PreAuthorize("hasAuthority('" + Authorities.PARAMETROS_EDITAR + "')")
    public CostoTramite actualizarCosto(TipoTramite tipo, ClaseLicencia clase, BigDecimal costo,
                                        LocalDate vigenciaDesde, LocalDate vigenciaHasta, String descripcion) {
        if (tipo == null || clase == null) {
            throw new IllegalArgumentException("El tipo de trámite y la clase de licencia son obligatorios");
        }
        if (costo == null || costo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El costo debe ser mayor a cero");
        }
        if (vigenciaDesde == null) {
            throw new IllegalArgumentException("La fecha de inicio de vigencia es obligatoria");
        }
        if (vigenciaHasta != null && vigenciaHasta.isBefore(vigenciaDesde)) {
            throw new IllegalArgumentException("La fecha de fin de vigencia no puede ser anterior al inicio");
        }

        // Cerrar la vigencia del costo vigente actual para la misma combinación
        Optional<CostoTramite> costoVigente = costoTramiteRepository.findCostoVigente(tipo, clase, vigenciaDesde);
        if (costoVigente.isPresent()) {
            CostoTramite anterior = costoVigente.get();
            anterior.setFechaVigenciaHasta(vigenciaDesde.minusDays(1));
            costoTramiteRepository.save(anterior);
            log.info("Cerrada vigencia del costo ID {} ({} - {}) al {}",
                    anterior.getId(), tipo, clase, vigenciaDesde.minusDays(1));
        }

        CostoTramite nuevo = new CostoTramite();
        nuevo.setTipoTramite(tipo);
        nuevo.setClaseLicencia(clase);
        nuevo.setCosto(costo);
        nuevo.setFechaVigenciaDesde(vigenciaDesde);
        nuevo.setFechaVigenciaHasta(vigenciaHasta);
        nuevo.setDescripcion(descripcion);
        nuevo.setActivo(true);

        log.info("Actualizando costo {} - {} a ${} vigente desde {}", tipo, clase, costo, vigenciaDesde);
        return costoTramiteRepository.save(nuevo);
    }

    /**
     * Desactiva un costo: deja de considerarse en la resolución del costo vigente
     */
    @PreAuthorize("hasAuthority('" + Authorities.PARAMETROS_EDITAR + "')")
    public CostoTramite desactivarCosto(Long id) {
        CostoTramite costo = costoTramiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Costo no encontrado con ID: " + id));

        if (Boolean.FALSE.equals(costo.getActivo())) {
            throw new IllegalStateException("El costo ya se encuentra desactivado");
        }

        costo.setActivo(false);
        log.info("Desactivando costo ID {} ({} - {})", id, costo.getTipoTramite(), costo.getClaseLicencia());
        return costoTramiteRepository.save(costo);
    }
}
