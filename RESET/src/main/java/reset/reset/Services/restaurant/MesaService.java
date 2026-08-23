package reset.reset.Services.restaurant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.restaurant.Mesa;
import reset.reset.Models.restaurant.Pedido;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.restaurant.MesaRepository;
import reset.reset.Repositories.restaurant.PedidoRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.restaurant.MesaDTO;
import reset.reset.dto.restaurant.MesaResumoDTO;
import reset.reset.dto.request.restaurant.MesaRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MesaService extends BaseServiceImpl<Mesa, Long, MesaRepository> {

    private final MesaRepository mesaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public MesaService(MesaRepository repository) {
        super(repository);
        this.mesaRepository = repository;
    }

    private User getAuthenticatedUser() {
        try {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userRepository.findById(principal.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        } catch (Exception e) {
            throw new BusinessException("Usuário não autenticado");
        }
    }

    private Long getCurrentEmpresaId() {
        return getAuthenticatedUser().getEmpresa().getId();
    }

    // ==================== VALIDAÇÕES ====================

    private void validateNumeroUniqueness(Long empresaId, String numero, Long excludeId) {
        mesaRepository.findByEmpresaIdAndNumero(empresaId, numero)
                .ifPresent(m -> {
                    if (excludeId == null || !m.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Mesa número " + numero + " já existe");
                    }
                });
    }

    private void validateCapacidade(Integer capacidade) {
        if (capacidade == null || capacidade < 1) {
            throw new BusinessException("Capacidade deve ser pelo menos 1");
        }
    }

    // ==================== CRUD COM DTOs ====================

    @Transactional
    public MesaDTO criarMesa(MesaRequest request) {
        Mesa mesa = new Mesa();
        mesa.setEmpresa(getAuthenticatedUser().getEmpresa());
        mesa.setNumero(request.getNumero());
        mesa.setCapacidade(request.getCapacidade() != null ? request.getCapacidade() : 4);
        mesa.setLocalizacao(request.getLocalizacao() != null ? request.getLocalizacao() : "Salão");
        mesa.setStatus(Mesa.StatusMesa.DISPONIVEL);
        mesa.setAtivo(true);

        validateNumeroUniqueness(mesa.getEmpresa().getId(), mesa.getNumero(), null);
        validateCapacidade(mesa.getCapacidade());

        Mesa saved = mesaRepository.save(mesa);
        log.info("Mesa criada: {}", saved.getNumero());
        return MesaDTO.fromEntity(saved);
    }

    @Transactional
    public MesaDTO atualizarMesa(Long id, MesaRequest request) {
        Mesa existing = findByIdOrThrow(id);
        validateCapacidade(request.getCapacidade());

        if (!existing.getNumero().equals(request.getNumero())) {
            validateNumeroUniqueness(getCurrentEmpresaId(), request.getNumero(), id);
        }

        existing.setNumero(request.getNumero());
        existing.setCapacidade(request.getCapacidade());
        existing.setLocalizacao(request.getLocalizacao());

        Mesa updated = mesaRepository.save(existing);
        log.info("Mesa atualizada: {}", updated.getNumero());
        return MesaDTO.fromEntity(updated);
    }

    @Transactional
    public MesaDTO ocuparMesa(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        if (mesa.getStatus() == Mesa.StatusMesa.OCUPADA) {
            throw new BusinessException("Mesa já está ocupada");
        }
        mesa.setStatus(Mesa.StatusMesa.OCUPADA);
        Mesa updated = mesaRepository.save(mesa);
        return MesaDTO.fromEntity(updated);
    }

    @Transactional
    public MesaDTO liberarMesa(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        mesa.setStatus(Mesa.StatusMesa.DISPONIVEL);
        Mesa updated = mesaRepository.save(mesa);
        return MesaDTO.fromEntity(updated);
    }

    @Transactional
    public MesaDTO reservarMesa(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        if (mesa.getStatus() == Mesa.StatusMesa.OCUPADA) {
            throw new BusinessException("Mesa está ocupada, não pode ser reservada");
        }
        mesa.setStatus(Mesa.StatusMesa.RESERVADA);
        Mesa updated = mesaRepository.save(mesa);
        return MesaDTO.fromEntity(updated);
    }

    @Transactional
    public MesaDTO marcarEmLimpeza(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        mesa.setStatus(Mesa.StatusMesa.EM_LIMPEZA);
        Mesa updated = mesaRepository.save(mesa);
        return MesaDTO.fromEntity(updated);
    }

    // ==================== CONSULTAS COM DTOs ====================

    public MesaDTO findMesaDTOById(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        return MesaDTO.fromEntity(mesa);
    }

    public Page<MesaResumoDTO> findMesasResumoByEmpresa(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        Page<Mesa> mesas = mesaRepository.findByEmpresaId(empresaId, pageable);
        return mesas.map(MesaResumoDTO::fromEntity);
    }

    public List<MesaDTO> findMesasDisponiveisDTO() {
        Long empresaId = getCurrentEmpresaId();
        List<Mesa> mesas = mesaRepository.findMesasDisponiveis(empresaId);
        return mesas.stream()
                .map(mesa -> {
                    Integer pedidosAtivos = pedidoRepository.countPedidosAtivosByMesaId(mesa.getId());
                    return MesaDTO.fromEntityWithPedidos(mesa, pedidosAtivos);
                })
                .collect(Collectors.toList());
    }

    public Page<MesaDTO> findMesasWithPedidos(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        Page<Mesa> mesas = mesaRepository.findByEmpresaId(empresaId, pageable);
        return mesas.map(mesa -> {
            Integer pedidosAtivos = pedidoRepository.countPedidosAtivosByMesaId(mesa.getId());
            return MesaDTO.fromEntityWithPedidos(mesa, pedidosAtivos);
        });
    }

    public List<MesaDTO> findAllMesas() {
        Long empresaId = getCurrentEmpresaId();
        List<Mesa> mesas = mesaRepository.findByEmpresaId(empresaId);
        return mesas.stream()
                .map(mesa -> {
                    Integer pedidosAtivos = pedidoRepository.countPedidosAtivosByMesaId(mesa.getId());
                    return MesaDTO.fromEntityWithPedidos(mesa, pedidosAtivos);
                })
                .collect(Collectors.toList());
    }

    // Métodos legado (compatibilidade)
    public List<Mesa> findMesasDisponiveis() {
        Long empresaId = getCurrentEmpresaId();
        return mesaRepository.findMesasDisponiveis(empresaId);
    }

    public Page<Mesa> findByEmpresaId(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        return mesaRepository.findByEmpresaId(empresaId, pageable);
    }

    public Page<Mesa> findActiveByEmpresaId(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        return mesaRepository.findActiveByEmpresaId(empresaId, pageable);
    }
}