package reset.reset.Services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.Permissao;
import reset.reset.Models.auth.Role;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Repositories.auth.PermissaoRepository;
import reset.reset.Repositories.auth.RoleRepository;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Security.JwtService;
import reset.reset.Security.UserPrincipal;
import reset.reset.dto.auth.AuthResponse;
import reset.reset.dto.auth.LoginRequest;
import reset.reset.dto.auth.RegisterRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository utilizadorRepository;
    private final EmpresaRepository empresaRepository;
    private final RoleRepository roleRepository;
    private final PermissaoRepository permissaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = utilizadorRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Update last login
            user.setUltimoLogin(LocalDateTime.now());
            utilizadorRepository.save(user);

            // Gerar token
            UserPrincipal userPrincipal = new UserPrincipal(user);
            String token = jwtService.generateToken(userPrincipal);

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .nome(user.getNome())
                    .empresaId(user.getEmpresa().getId())
                    .empresaNome(user.getEmpresa().getNome())
                    .roles(user.getRoles().stream()
                            .map(Role::getNome)
                            .collect(Collectors.toSet()))
                    .permissions(user.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .collect(Collectors.toSet()))
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new BusinessException("Invalid username or password");
        }
    }

    @Transactional
    public User register(RegisterRequest request) {
        // Validate username uniqueness
        if (utilizadorRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntityException("Username already exists: " + request.getUsername());
        }

        // Validate email uniqueness
        if (request.getEmail() != null && utilizadorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("Email already exists: " + request.getEmail());
        }

        // Get or create empresa
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new EntityNotFoundException("Empresa not found"));

        // Create user
        User utilizador = new User();
        utilizador.setUsername(request.getUsername());
        utilizador.setPassword(passwordEncoder.encode(request.getPassword()));
        utilizador.setNome(request.getNome());
        utilizador.setEmail(request.getEmail());
        utilizador.setEmpresa(empresa);
        utilizador.setAtivo(true);
        utilizador.setDataRegisto(LocalDateTime.now());

        // Assign roles
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoleNames()) {
                Role role = roleRepository.findByNome(roleName)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
            utilizador.setRoles(roles);
        }

        // Assign permissions
        if (request.getPermissionNames() != null && !request.getPermissionNames().isEmpty()) {
            Set<Permissao> permissoes = new HashSet<>();
            for (String permName : request.getPermissionNames()) {
                Permissao permissao = permissaoRepository.findByNome(permName)
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permName));
                permissoes.add(permissao);
            }
            utilizador.setPermissoes(permissoes);
        }

        return utilizadorRepository.save(utilizador);
    }

    @Transactional
    public AuthResponse refreshToken(String token) {
        String username = jwtService.extractUsername(token);
        User utilizador = utilizadorRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String newToken = jwtService.generateToken(utilizador);

        return AuthResponse.builder()
                .token(newToken)
                .username(utilizador.getUsername())
                .email(utilizador.getEmail())
                .nome(utilizador.getNome())
                .empresaId(utilizador.getEmpresa().getId())
                .build();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("User not authenticated");
        }

        String username = authentication.getName();
        return utilizadorRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public boolean hasPermission(String permission) {
        User user = getCurrentUser();
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission));
    }

    public boolean hasRole(String role) {
        User user = getCurrentUser();
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}

