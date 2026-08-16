package reset.reset.Security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import reset.reset.Models.auth.Permissao;
import reset.reset.Models.auth.Role;
import reset.reset.Models.auth.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String nome;
    private final String email;
    private final Long empresaId;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.nome = user.getNome();
        this.email = user.getEmail();
        this.empresaId = user.getEmpresa() != null ? user.getEmpresa().getId() : null;
        this.enabled = user.getAtivo() != null && user.getAtivo();

        // Construir authorities de forma segura
        Set<GrantedAuthority> authoritySet = new HashSet<>();

        // Adicionar roles como ROLE_
        if (user.getRoles() != null) {
            authoritySet.addAll(
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNome()))
                            .collect(Collectors.toSet())
            );

            // Adicionar permissões das roles
            user.getRoles().stream()
                    .filter(role -> role.getPermissoes() != null)
                    .flatMap(role -> role.getPermissoes().stream())
                    .map(permissao -> new SimpleGrantedAuthority(permissao.getNome()))
                    .forEach(authoritySet::add);
        }

        // Adicionar permissões diretas do usuário
        if (user.getPermissoes() != null) {
            user.getPermissoes().stream()
                    .map(permissao -> new SimpleGrantedAuthority(permissao.getNome()))
                    .forEach(authoritySet::add);
        }

        this.authorities = authoritySet;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return enabled;
    }
}