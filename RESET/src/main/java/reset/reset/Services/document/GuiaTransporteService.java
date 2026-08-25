package reset.reset.Services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.GuiaTransporte;
import reset.reset.Repositories.document.GuiaTransporteRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.document.GuiaTransporteDTO;
import reset.reset.dto.request.GuiaTransporteRequest;

import java.time.LocalDateTime;

@Service
@Slf4j
public class GuiaTransporteService extends BaseServiceImpl<GuiaTransporte, Long, GuiaTransporteRepository> {

    private final GuiaTransporteRepository guiaTransporteRepository;

    public GuiaTransporteService(GuiaTransporteRepository repository) {
        super(repository);
        this.guiaTransporteRepository = repository;
    }

    @Override
    protected void validateBeforeSave(GuiaTransporte guiaTransporte) {
        if (guiaTransporte.getMatricula() == null || guiaTransporte.getMatricula().isEmpty()) {
            throw new BusinessException("Vehicle registration is required");
        }
        if (guiaTransporte.getMotorista() == null || guiaTransporte.getMotorista().isEmpty()) {
            throw new BusinessException("Driver name is required");
        }
        if (guiaTransporte.getDataCarregamento() != null &&
                guiaTransporte.getDataDescarga() != null &&
                guiaTransporte.getDataDescarga().isBefore(guiaTransporte.getDataCarregamento())) {
            throw new BusinessException("Unloading date cannot be before loading date");
        }
    }

    @Override
    @Transactional
    public GuiaTransporte save(GuiaTransporte guiaTransporte) {
        if (guiaTransporte.getEstado() == null) {
            guiaTransporte.setEstado("PENDENTE");
        }
        return super.save(guiaTransporte);
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    @Transactional
    public GuiaTransporteDTO createGuiaTransporte(GuiaTransporteRequest request) {
        GuiaTransporte guia = request.toEntity();
        GuiaTransporte saved = save(guia);
        return GuiaTransporteDTO.fromEntity(saved);
    }

    public GuiaTransporteDTO findByIdDTO(Long id) {
        GuiaTransporte guia = findByIdOrThrow(id);
        return GuiaTransporteDTO.fromEntity(guia);
    }

    public Page<GuiaTransporteDTO> findAllDTO(Pageable pageable) {
        Page<GuiaTransporte> guias = findAll(pageable);
        return guias.map(GuiaTransporteDTO::fromEntity);
    }

    public Page<GuiaTransporteDTO> findByEmpresaIdDTO(Long empresaId, Pageable pageable) {
        Page<GuiaTransporte> guias = guiaTransporteRepository.findByEmpresaId(empresaId, pageable);
        return guias.map(GuiaTransporteDTO::fromEntity);
    }

    public Page<GuiaTransporteDTO> findByEstadoDTO(String estado, Pageable pageable) {
        Page<GuiaTransporte> guias = guiaTransporteRepository.findByEstado(estado, pageable);
        return guias.map(GuiaTransporteDTO::fromEntity);
    }

    @Transactional
    public GuiaTransporteDTO iniciarTransporteDTO(Long id) {
        GuiaTransporte guia = iniciarTransporte(id);
        return GuiaTransporteDTO.fromEntity(guia);
    }

    @Transactional
    public GuiaTransporteDTO finalizarTransporteDTO(Long id) {
        GuiaTransporte guia = finalizarTransporte(id);
        return GuiaTransporteDTO.fromEntity(guia);
    }

    @Transactional
    public GuiaTransporteDTO cancelarTransporteDTO(Long id, String observacao) {
        GuiaTransporte guia = cancelarTransporte(id, observacao);
        return GuiaTransporteDTO.fromEntity(guia);
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

    @Transactional
    public GuiaTransporte iniciarTransporte(Long id) {
        GuiaTransporte guia = findByIdOrThrow(id);

        if (!"PENDENTE".equals(guia.getEstado())) {
            throw new BusinessException("Only pending transport guides can be started");
        }

        guia.setEstado("EM_TRANSITO");
        guia.setDataCarregamento(LocalDateTime.now());
        return guiaTransporteRepository.save(guia);
    }

    @Transactional
    public GuiaTransporte finalizarTransporte(Long id) {
        GuiaTransporte guia = findByIdOrThrow(id);

        if (!"EM_TRANSITO".equals(guia.getEstado())) {
            throw new BusinessException("Only transport guides in transit can be finalized");
        }

        guia.setEstado("ENTREGUE");
        guia.setDataDescarga(LocalDateTime.now());
        return guiaTransporteRepository.save(guia);
    }

    @Transactional
    public GuiaTransporte cancelarTransporte(Long id, String observacao) {
        GuiaTransporte guia = findByIdOrThrow(id);

        if ("ENTREGUE".equals(guia.getEstado())) {
            throw new BusinessException("Cannot cancel a delivered transport");
        }

        guia.setEstado("CANCELADO");
        if (observacao != null) {
            guia.setObservacoesTransporte(observacao);
        }
        return guiaTransporteRepository.save(guia);
    }

    public Page<GuiaTransporte> findByEmpresaId(Long empresaId, Pageable pageable) {
        return guiaTransporteRepository.findByEmpresaId(empresaId, pageable);
    }
}