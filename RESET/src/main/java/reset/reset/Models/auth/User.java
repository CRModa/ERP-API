package reset.reset.Models.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import reset.reset.Models.core.Empresa;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 150)
    private String nome;

    @Column(length = 150)
    private String email;

    @Column(length = 50, unique = true, nullable = false)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(length = 50)
    private String perfil; // ADMIN, VENDEDOR, CONTABILISTA (deprecated - use roles)

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "data_registo", updatable = false)
    private LocalDateTime dataRegisto;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilizador_role",
            joinColumns = @JoinColumn(name = "utilizador_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilizador_permissao",
            joinColumns = @JoinColumn(name = "utilizador_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    private Set<Permissao> permissoes = new HashSet<>();

    // UserDetails Implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role authorities
        roles.forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getNome()));
            role.getPermissoes().forEach(permissao ->
                    authorities.add(new SimpleGrantedAuthority(permissao.getNome()))
            );
        });

        // Add direct user permissions
        permissoes.forEach(permissao ->
                authorities.add(new SimpleGrantedAuthority(permissao.getNome()))
        );

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
