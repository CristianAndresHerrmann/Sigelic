package com.example.sigelic.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigelic.dto.request.AptoMedicoRequestDTO;
import com.example.sigelic.dto.response.AptoMedicoResponseDTO;
import com.example.sigelic.model.AptoMedico;
import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.Pago;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.repository.AptoMedicoRepository;
import com.example.sigelic.repository.ExamenPracticoRepository;
import com.example.sigelic.repository.ExamenTeoricoRepository;
import com.example.sigelic.repository.PagoRepository;
import com.example.sigelic.repository.TramiteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar trámites de licencias
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TramiteService {

    private final TramiteRepository tramiteRepository;
    private final TitularService titularService;
    private final ExamenTeoricoRepository examenTeoricoRepository;
    private final ExamenPracticoRepository examenPracticoRepository;
    private final AptoMedicoRepository aptoMedicoRepository;
    private final PagoRepository pagoRepository;
    private final LicenciaService licenciaService;

    /**
     * Busca un trámite por ID
     */
    @Transactional(readOnly = true)
    public Optional<Tramite> findById(Long id) {
        return tramiteRepository.findById(id);
    }

    /**
     * Obtiene todos los trámites
     */
    @Transactional(readOnly = true)
    public List<Tramite> findAll() {
        return tramiteRepository.findAllWithTitular();
    }

    /**
     * Obtiene todos los trámites de un titular
     */
    @Transactional(readOnly = true)
    public List<Tramite> findByTitular(Long titularId) {
        Titular titular = titularService.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));
        return tramiteRepository.findByTitular(titular);
    }

    /**
     * Obtiene trámites por estado
     */
    @Transactional(readOnly = true)
    public List<Tramite> findByEstado(EstadoTramite estado) {
        return tramiteRepository.findByEstado(estado);
    }

    /**
     * Inicia un nuevo trámite
     */
    public Tramite iniciarTramite(Long titularId, TipoTramite tipo, ClaseLicencia clase) {
        Titular titular = titularService.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));

        // Verificar que el titular pueda iniciar trámites
        if (!titularService.puedeIniciarTramite(titularId)) {
            throw new IllegalStateException("El titular tiene inhabilitaciones activas y no puede iniciar trámites");
        }

        // Verificar que no tenga trámites activos
        List<EstadoTramite> estadosActivos = Arrays.asList(
            EstadoTramite.INICIADO, EstadoTramite.DOCS_OK, EstadoTramite.APTO_MED,
            EstadoTramite.EX_TEO_OK, EstadoTramite.EX_PRA_OK, EstadoTramite.PAGO_OK
        );
        
        if (tramiteRepository.existsByTitularAndEstadoIn(titular, estadosActivos)) {
            throw new IllegalStateException("El titular ya tiene un trámite activo");
        }

        // Verificar edad mínima para la clase solicitada
        int edadTitular = titular.getEdad();
        if (edadTitular < clase.getEdadMinima()) {
            throw new IllegalArgumentException("El titular no cumple con la edad mínima para la clase " + clase.name());
        }

        // Validaciones específicas por tipo de trámite
        validarRequisitosPorTipoTramite(titular, tipo, clase);

        Tramite tramite = new Tramite();
        tramite.setTitular(titular);
        tramite.setTipo(tipo);
        tramite.setClaseSolicitada(clase);
        tramite.setEstado(EstadoTramite.INICIADO);

        log.info("Iniciando trámite de {} para titular: {} {}", tipo.name(), titular.getNombre(), titular.getApellido());
        return tramiteRepository.save(tramite);
    }

    /**
     * Valida los requisitos específicos según el tipo de trámite
     */
    private void validarRequisitosPorTipoTramite(Titular titular, TipoTramite tipo, ClaseLicencia clase) {
        switch (tipo) {
            case RENOVACION:
                validarRenovacion(titular, clase);
                break;
            case DUPLICADO:
                validarDuplicado(titular, clase);
                break;
            case CAMBIO_DOMICILIO:
                validarCambioDomicilio(titular, clase);
                break;
            case EMISION:
                validarEmision(titular, clase);
                break;
            default:
                // No hay validaciones específicas para otros tipos
        }
    }

    /**
     * Valida que el titular pueda hacer una renovación
     */
    private void validarRenovacion(Titular titular, ClaseLicencia clase) {
        // Buscar licencias del titular para la clase solicitada
        List<Licencia> licenciasDelTitular = licenciaService.findByTitular(titular);
        
        // Verificar que tenga al menos una licencia de la clase solicitada
        boolean tieneLicenciaClase = licenciasDelTitular.stream()
                .anyMatch(licencia -> licencia.getClase() == clase);
        
        if (!tieneLicenciaClase) {
            throw new IllegalStateException("No se puede renovar: el titular no tiene una licencia de clase " + 
                    clase.name() + " en el sistema");
        }

        // Verificar que tenga una licencia vigente o vencida (pero no muy antigua)
        boolean tieneLicenciaRenovable = licenciasDelTitular.stream()
                .anyMatch(licencia -> licencia.getClase() == clase && 
                         licencia.getFechaVencimiento().isAfter(LocalDate.now().minusYears(2)));
        
        if (!tieneLicenciaRenovable) {
            throw new IllegalStateException("No se puede renovar: la licencia de clase " + clase.name() + 
                    " está vencida hace más de 2 años. Debe hacer una emisión nueva");
        }
    }

    /**
     * Valida que el titular pueda hacer un duplicado
     */
    private void validarDuplicado(Titular titular, ClaseLicencia clase) {
        // Buscar licencias del titular para la clase solicitada
        List<Licencia> licenciasDelTitular = licenciaService.findByTitular(titular);
        
        // Verificar que tenga al menos una licencia de la clase solicitada
        boolean tieneLicenciaClase = licenciasDelTitular.stream()
                .anyMatch(licencia -> licencia.getClase() == clase);
        
        if (!tieneLicenciaClase) {
            throw new IllegalStateException("No se puede hacer duplicado: el titular no tiene una licencia de clase " + 
                    clase.name() + " en el sistema");
        }

        // Verificar que tenga una licencia vigente para duplicar
        boolean tieneLicenciaVigente = licenciasDelTitular.stream()
                .anyMatch(licencia -> licencia.getClase() == clase && licencia.isVigente());
        
        if (!tieneLicenciaVigente) {
            throw new IllegalStateException("No se puede hacer duplicado: no tiene una licencia vigente de clase " + 
                    clase.name() + ". Si está vencida, debe hacer una renovación");
        }
    }

    /**
     * Valida que el titular pueda hacer un cambio de domicilio
     */
    private void validarCambioDomicilio(Titular titular, ClaseLicencia clase) {
        // Buscar licencias del titular para la clase solicitada
        List<Licencia> licenciasDelTitular = licenciaService.findByTitular(titular);
        
        // Verificar que tenga una licencia vigente de la clase solicitada
        boolean tieneLicenciaVigente = licenciasDelTitular.stream()
                .anyMatch(licencia -> licencia.getClase() == clase && licencia.isVigente());
        
        if (!tieneLicenciaVigente) {
            throw new IllegalStateException("No se puede cambiar domicilio: el titular no tiene una licencia vigente de clase " + 
                    clase.name() + " en el sistema");
        }
    }

    /**
     * Valida que el titular pueda hacer una emisión
     */
    private void validarEmision(Titular titular, ClaseLicencia clase) {
        // Para emisión, verificar que no tenga ya una licencia vigente de la misma clase
        List<Licencia> licenciasDelTitular = licenciaService.findByTitular(titular);
        
        boolean yaTimeLicenciaVigente = licenciasDelTitular.stream()
                .anyMatch(licencia -> licencia.getClase() == clase && licencia.isVigente());
        
        if (yaTimeLicenciaVigente) {
            throw new IllegalStateException("No se puede emitir: el titular ya tiene una licencia vigente de clase " + 
                    clase.name() + ". Si necesita una nueva, haga un duplicado");
        }
    }

    /**
     * Valida la documentación de un trámite
     */
    public Tramite validarDocumentacion(Long tramiteId, String agenteResponsable) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        if (tramite.getEstado() != EstadoTramite.INICIADO) {
            throw new IllegalStateException("Solo se puede validar documentación de trámites en estado INICIADO");
        }

        tramite.setDocumentacionValidada(true);
        tramite.setAgenteResponsable(agenteResponsable);
        tramite.actualizarEstado();

        log.info("Documentación validada para trámite ID: {}", tramiteId);
        return tramiteRepository.save(tramite);
    }

    /**
     * Valida la documentación de un trámite (versión simplificada)
     */
    public Tramite validarDocumentacion(Long tramiteId) {
        return validarDocumentacion(tramiteId, "Sistema"); // Agente por defecto
    }

    /**
     * Registra un examen teórico
     */
    public Tramite registrarExamenTeorico(Long tramiteId, ExamenTeorico examen) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        if (!tramite.requiereExamenTeorico()) {
            throw new IllegalStateException("Este tipo de trámite no requiere examen teórico");
        }

        examen.setTramite(tramite);
        examen.setFecha(LocalDateTime.now());
        examenTeoricoRepository.save(examen);

        if (examen.getAprobado()) {
            tramite.setExamenTeoricoAprobado(true);
            tramite.actualizarEstado();
            log.info("Examen teórico aprobado para trámite ID: {}", tramiteId);
        } else {
            log.info("Examen teórico desaprobado para trámite ID: {}", tramiteId);
        }

        return tramiteRepository.save(tramite);
    }

    /**
     * Registra un examen práctico
     */
    public Tramite registrarExamenPractico(Long tramiteId, ExamenPractico examen) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        if (!tramite.requiereExamenPractico()) {
            throw new IllegalStateException("Este tipo de trámite no requiere examen práctico");
        }

        examen.setTramite(tramite);
        examen.setFecha(LocalDateTime.now());
        examenPracticoRepository.save(examen);

        if (examen.getAprobado()) {
            tramite.setExamenPracticoAprobado(true);
            tramite.actualizarEstado();
            log.info("Examen práctico aprobado para trámite ID: {}", tramiteId);
        } else {
            log.info("Examen práctico desaprobado para trámite ID: {}", tramiteId);
        }

        return tramiteRepository.save(tramite);
    }

    /**
     * Registra un apto médico
     */
    public Tramite registrarAptoMedico(Long tramiteId, AptoMedico apto) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        if (!tramite.requiereAptoMedico()) {
            throw new IllegalStateException("Este tipo de trámite no requiere apto médico");
        }

        apto.setTramite(tramite);
        apto.setFecha(LocalDateTime.now());
        aptoMedicoRepository.save(apto);

        if (apto.getApto()) {
            tramite.setAptoMedicoVigente(true);
            tramite.actualizarEstado();
            log.info("Apto médico registrado para trámite ID: {}", tramiteId);
        } else {
            log.info("No apto médico registrado para trámite ID: {}", tramiteId);
        }

        return tramiteRepository.save(tramite);
    }

    /**
     * Registra un pago
     */
    public Tramite registrarPago(Long tramiteId, Pago pago) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        pago.setTramite(tramite);
        pagoRepository.save(pago);

        if (pago.isAcreditado()) {
            tramite.setPagoAcreditado(true);
            tramite.actualizarEstado();
            log.info("Pago acreditado para trámite ID: {}", tramiteId);
        }

        return tramiteRepository.save(tramite);
    }

    /**
     * Emite una licencia si todos los requisitos están cumplidos
     */
    public Licencia emitirLicencia(Long tramiteId) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        if (!tramite.todosLosRequisitosCumplidos()) {
            throw new IllegalStateException("No se pueden emitir licencias sin cumplir todos los requisitos");
        }

        if (tramite.getEstado() == EstadoTramite.EMITIDA) {
            throw new IllegalStateException("El trámite ya tiene una licencia emitida");
        }

        Licencia licencia = licenciaService.emitirLicencia(tramite);
        tramite.setEstado(EstadoTramite.EMITIDA);
        tramiteRepository.save(tramite);

        log.info("Licencia emitida para trámite ID: {}", tramiteId);
        return licencia;
    }

    /**
     * Rechaza un trámite (método genérico mantenido por compatibilidad)
     */
    public Tramite rechazarTramite(Long tramiteId, String motivo) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        log.info("Intentando rechazar trámite ID: {} en estado: {} - Motivo: {}", 
                tramiteId, tramite.getEstado(), motivo);

        // Determinar qué tipo de rechazo es basado en el estado actual
        switch (tramite.getEstado()) {
            case INICIADO -> {
                // Si está iniciado, se está validando documentación
                return rechazarDocumentacion(tramiteId, motivo);
            }
            case DOCS_OK, APTO_MED -> {
                // Si tiene docs OK o apto médico, puede estar en examen teórico
                if (tramite.requiereExamenTeorico()) {
                    return rechazarExamenTeorico(tramiteId, motivo);
                } else if (tramite.requiereExamenPractico()) {
                    return rechazarExamenPractico(tramiteId, motivo);
                } else {
                    // Rechazo genérico
                    tramite.setEstado(EstadoTramite.RECHAZADA);
                    tramite.setObservaciones(motivo);
                    log.info("Trámite rechazado genéricamente ID: {} - Motivo: {}", tramiteId, motivo);
                    return tramiteRepository.save(tramite);
                }
            }
            case EX_TEO_OK -> {
                // Si aprobó teórico, puede estar en práctico
                if (tramite.requiereExamenPractico()) {
                    return rechazarExamenPractico(tramiteId, motivo);
                } else {
                    // Rechazo genérico
                    tramite.setEstado(EstadoTramite.RECHAZADA);
                    tramite.setObservaciones(motivo);
                    log.info("Trámite rechazado genéricamente ID: {} - Motivo: {}", tramiteId, motivo);
                    return tramiteRepository.save(tramite);
                }
            }
            default -> {
                // Rechazo genérico para otros casos
                tramite.setEstado(EstadoTramite.RECHAZADA);
                tramite.setObservaciones(motivo);
                log.info("Trámite rechazado genéricamente ID: {} - Motivo: {}", tramiteId, motivo);
                return tramiteRepository.save(tramite);
            }
        }
    }

    /**
     * Rechaza la documentación de un trámite
     */
    public Tramite rechazarDocumentacion(Long tramiteId, String motivo) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        log.info("Rechazando documentación para trámite ID: {} en estado: {}", tramiteId, tramite.getEstado());
        
        tramite.setObservaciones(motivo);
        tramite.rechazarDocumentacion();
        
        log.info("Documentación rechazada para trámite ID: {} - Nuevo estado: {} - Motivo: {}", 
                tramiteId, tramite.getEstado(), motivo);
        
        return tramiteRepository.save(tramite);
    }

    /**
     * Rechaza el examen teórico de un trámite
     */
    public Tramite rechazarExamenTeorico(Long tramiteId, String motivo) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        tramite.setObservaciones(motivo);
        tramite.rechazarExamenTeorico();
        log.info("Examen teórico rechazado para trámite ID: {} - Motivo: {}", tramiteId, motivo);
        return tramiteRepository.save(tramite);
    }

    /**
     * Rechaza el examen práctico de un trámite
     */
    public Tramite rechazarExamenPractico(Long tramiteId, String motivo) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        tramite.setObservaciones(motivo);
        tramite.rechazarExamenPractico();
        log.info("Examen práctico rechazado para trámite ID: {} - Motivo: {}", tramiteId, motivo);
        return tramiteRepository.save(tramite);
    }

    /**
     * Permite el reintento de un trámite rechazado
     */
    public Tramite permitirReintento(Long tramiteId, String motivo) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        tramite.setObservaciones(motivo);
        tramite.permitirReintento();
        log.info("Reintento autorizado para trámite ID: {} - Motivo: {}", tramiteId, motivo);
        return tramiteRepository.save(tramite);
    }

    /**
     * Obtiene el trámite activo de un titular
     */
    @Transactional(readOnly = true)
    public Optional<Tramite> getTramiteActivo(Long titularId) {
        Titular titular = titularService.findById(titularId)
                .orElseThrow(() -> new IllegalArgumentException("Titular no encontrado con ID: " + titularId));

        List<EstadoTramite> estadosActivos = Arrays.asList(
            EstadoTramite.INICIADO, EstadoTramite.DOCS_OK, EstadoTramite.APTO_MED,
            EstadoTramite.EX_TEO_OK, EstadoTramite.EX_PRA_OK, EstadoTramite.PAGO_OK
        );

        return tramiteRepository.findTramiteActivoByTitular(titular, estadosActivos);
    }

    /**
     * Obtiene estadísticas de trámites por período
     */
    @Transactional(readOnly = true)
    public Long getCountByTipoEnPeriodo(TipoTramite tipo, LocalDateTime desde, LocalDateTime hasta) {
        return tramiteRepository.countByTipoEnPeriodo(tipo, desde, hasta);
    }

    /**
     * Obtiene estadísticas de trámites por estado
     */
    @Transactional(readOnly = true)
    public Long getCountByEstado(EstadoTramite estado) {
        return tramiteRepository.countByEstado(estado);
    }

    /**
     * Cuenta los trámites activos (en progreso, pendientes)
     */
    @Transactional(readOnly = true)
    public long countTramitesActivos() {
        List<EstadoTramite> estadosActivos = Arrays.asList(
            EstadoTramite.INICIADO,
            EstadoTramite.DOCS_OK,
            EstadoTramite.APTO_MED,
            EstadoTramite.EX_TEO_OK,
            EstadoTramite.EX_PRA_OK,
            EstadoTramite.PAGO_OK
        );
        return estadosActivos.stream()
                .mapToLong(estado -> tramiteRepository.countByEstado(estado))
                .sum();
    }

    /**
     * Registra un apto médico usando DTO
     */
    public AptoMedicoResponseDTO registrarAptoMedico(Long tramiteId, AptoMedicoRequestDTO request) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));

        if (!tramite.requiereAptoMedico()) {
            throw new IllegalStateException("Este tipo de trámite no requiere apto médico");
        }

        // Crear entidad AptoMedico desde DTO (solo campos disponibles)
        AptoMedico apto = new AptoMedico();
        apto.setTramite(tramite);
        apto.setFecha(request.getFechaExamen());
        apto.setProfesional(request.getMedicoExaminador());
        apto.setApto(request.getApto());
        apto.setPresionSistolica(request.getPresionSistolica());
        apto.setPresionDiastolica(request.getPresionDiastolica());
        
        // Combinar campos de agudeza visual
        String agudezaVisual = String.format("OD: %.1f, OI: %.1f", 
            request.getAgudezaVisualOjoDerecho() != null ? request.getAgudezaVisualOjoDerecho().doubleValue() : 0.0,
            request.getAgudezaVisualOjoIzquierdo() != null ? request.getAgudezaVisualOjoIzquierdo().doubleValue() : 0.0);
        apto.setAgudezaVisual(agudezaVisual);
        
        apto.setObservaciones(request.getObservaciones());
        apto.setRestricciones(request.getRestricciones());
        
        // Calcular fecha de vencimiento según meses de validez
        if (request.getMesesValidez() != null) {
            apto.setFechaVencimiento(request.getFechaExamen().toLocalDate().plusMonths(request.getMesesValidez()));
        }

        aptoMedicoRepository.save(apto);

        if (apto.getApto()) {
            tramite.setAptoMedicoVigente(true);
            tramite.actualizarEstado();
            log.info("Apto médico registrado para trámite ID: {}", tramiteId);
        } else {
            // Cambiar estado a APTO_MED_RECHAZADO (estado final)
            tramite.setEstado(EstadoTramite.APTO_MED_RECHAZADO);
            tramite.setAptoMedicoVigente(false);
            log.info("No apto médico registrado para trámite ID: {} - Trámite finalizado", tramiteId);
        }

        tramiteRepository.save(tramite);

        // Convertir a DTO de respuesta
        return convertirAAptoMedicoResponseDTO(apto);
    }

    /**
     * Obtiene el apto médico de un trámite
     */
    @Transactional(readOnly = true)
    public Optional<AptoMedicoResponseDTO> obtenerAptoMedico(Long tramiteId) {
        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado con ID: " + tramiteId));
        
        return aptoMedicoRepository.findUltimoAptoVigente(tramite)
                .map(this::convertirAAptoMedicoResponseDTO);
    }

    /**
     * Obtiene lista de aptos médicos próximos a vencer
     */
    @Transactional(readOnly = true)
    public List<AptoMedicoResponseDTO> obtenerAptosProximosAVencer() {
        LocalDate desde = LocalDate.now();
        LocalDate hasta = LocalDate.now().plusDays(30);
        List<AptoMedico> aptosProximos = aptoMedicoRepository.findAptosProximosAVencer(desde, hasta);
        return aptosProximos.stream()
                .map(this::convertirAAptoMedicoResponseDTO)
                .toList();
    }

    /**
     * Convierte AptoMedico a DTO de respuesta
     */
    private AptoMedicoResponseDTO convertirAAptoMedicoResponseDTO(AptoMedico apto) {
        return AptoMedicoResponseDTO.builder()
                .id(apto.getId())
                .profesional(apto.getProfesional())
                .matriculaProfesional(apto.getMatriculaProfesional())
                .apto(apto.getApto())
                .fecha(apto.getFecha())
                .fechaVencimiento(apto.getFechaVencimiento())
                .observaciones(apto.getObservaciones())
                .restricciones(apto.getRestricciones())
                .presionSistolica(apto.getPresionSistolica())
                .presionDiastolica(apto.getPresionDiastolica())
                .agudezaVisual(apto.getAgudezaVisual())
                .vigente(apto.isVigente())
                .build();
    }
}
