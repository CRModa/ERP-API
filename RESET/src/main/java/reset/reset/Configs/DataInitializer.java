package reset.reset.Configs;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Models.auth.Permissao;
import reset.reset.Models.auth.Role;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Repositories.auth.PermissaoRepository;
import reset.reset.Repositories.auth.RoleRepository;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissaoRepository permissaoRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Verificar se já existem dados
        if (userRepository.count() > 0) {
            return; // ← Pular se já existir usuários
        }

        // Create default empresa
        Empresa empresa = new Empresa();
        empresa.setNome("Reset");
        empresa.setNuit("100000000");
        empresa.setEmail("admin@reset.com");
        empresa.setTelefone("+258 84 9460 718");
        empresa.setAtivo(true);
        empresa = empresaRepository.save(empresa);

        // Create permissions (usando um Set para evitar duplicatas)
        Set<Permissao> allPermissoes = new HashSet<>();

        // Produto permissions
        allPermissoes.add(createPermissao("PRODUTO_CREATE", "Criar produtos", "PRODUTO", "CREATE"));
        allPermissoes.add(createPermissao("PRODUTO_READ", "Visualizar produtos", "PRODUTO", "READ"));
        allPermissoes.add(createPermissao("PRODUTO_UPDATE", "Atualizar produtos", "PRODUTO", "UPDATE"));
        allPermissoes.add(createPermissao("PRODUTO_DELETE", "Excluir produtos", "PRODUTO", "DELETE"));

        // Venda permissions
        allPermissoes.add(createPermissao("VENDA_CREATE", "Criar vendas", "VENDA", "CREATE"));
        allPermissoes.add(createPermissao("VENDA_READ", "Visualizar vendas", "VENDA", "READ"));
        allPermissoes.add(createPermissao("VENDA_UPDATE", "Atualizar vendas", "VENDA", "UPDATE"));
        allPermissoes.add(createPermissao("VENDA_DELETE", "Excluir vendas", "VENDA", "DELETE"));

        // Cliente permissions
        allPermissoes.add(createPermissao("CLIENTE_CREATE", "Criar clientes", "CLIENTE", "CREATE"));
        allPermissoes.add(createPermissao("CLIENTE_READ", "Visualizar clientes", "CLIENTE", "READ"));
        allPermissoes.add(createPermissao("CLIENTE_UPDATE", "Atualizar clientes", "CLIENTE", "UPDATE"));
        allPermissoes.add(createPermissao("CLIENTE_DELETE", "Excluir clientes", "CLIENTE", "DELETE"));

        // Utilizador permissions
        allPermissoes.add(createPermissao("UTILIZADOR_CREATE", "Criar utilizadores", "UTILIZADOR", "CREATE"));
        allPermissoes.add(createPermissao("UTILIZADOR_READ", "Visualizar utilizadores", "UTILIZADOR", "READ"));
        allPermissoes.add(createPermissao("UTILIZADOR_UPDATE", "Atualizar utilizadores", "UTILIZADOR", "UPDATE"));
        allPermissoes.add(createPermissao("UTILIZADOR_DELETE", "Excluir utilizadores", "UTILIZADOR", "DELETE"));

        // Relatorio permissions
        allPermissoes.add(createPermissao("RELATORIO_READ", "Visualizar relatórios", "RELATORIO", "READ"));
        allPermissoes.add(createPermissao("RELATORIO_EXPORT", "Exportar relatórios", "RELATORIO", "EXECUTE"));

        // Create Roles
        Role adminRole = createRole("ADMIN", "Administrador do sistema", allPermissoes);

        Set<Permissao> vendedorPermissoes = new HashSet<>(Arrays.asList(
                getPermissao("PRODUTO_READ"),
                getPermissao("VENDA_CREATE"),
                getPermissao("VENDA_READ"),
                getPermissao("CLIENTE_CREATE"),
                getPermissao("CLIENTE_READ"),
                getPermissao("CLIENTE_UPDATE"),
                getPermissao("RELATORIO_READ")
        ));
        Role vendedorRole = createRole("VENDEDOR", "Vendedor", vendedorPermissoes);

        Set<Permissao> contabilistaPermissoes = new HashSet<>(Arrays.asList(
                getPermissao("PRODUTO_READ"),
                getPermissao("VENDA_READ"),
                getPermissao("CLIENTE_READ"),
                getPermissao("RELATORIO_READ"),
                getPermissao("RELATORIO_EXPORT")
        ));
        Role contabilistaRole = createRole("CONTABILISTA", "Contabilista", contabilistaPermissoes);

        Set<Permissao> gerentePermissoes = new HashSet<>(Arrays.asList(
                getPermissao("PRODUTO_CREATE"),
                getPermissao("PRODUTO_READ"),
                getPermissao("PRODUTO_UPDATE"),
                getPermissao("VENDA_CREATE"),
                getPermissao("VENDA_READ"),
                getPermissao("VENDA_UPDATE"),
                getPermissao("CLIENTE_CREATE"),
                getPermissao("CLIENTE_READ"),
                getPermissao("CLIENTE_UPDATE"),
                getPermissao("RELATORIO_READ"),
                getPermissao("RELATORIO_EXPORT")
        ));
        Role gerenteRole = createRole("GERENTE", "Gerente", gerentePermissoes);

        // Create admin user
        if (!userRepository.findByUsername("cmoda").isPresent()) {
            User admin = new User();
            admin.setEmpresa(empresa);
            admin.setNome("César Moda");
            admin.setUsername("cmoda");
            admin.setEmail("cmoda@reset.com");
            admin.setPassword(passwordEncoder.encode("  dev!0!"));
            admin.setPerfil("ADMIN");
            admin.setAtivo(true);
            admin.setRoles(new HashSet<>(Arrays.asList(adminRole)));
            userRepository.save(admin);
        }
    }

    private Permissao createPermissao(String nome, String descricao, String recurso, String acao) {
        return permissaoRepository.findByNome(nome)
                .orElseGet(() -> {
                    Permissao permissao = new Permissao();
                    permissao.setNome(nome);
                    permissao.setDescricao(descricao);
                    permissao.setRecurso(recurso);
                    permissao.setAcao(acao);
                    return permissaoRepository.save(permissao);
                });
    }

    private Role createRole(String nome, String descricao, Set<Permissao> permissoes) {
        return roleRepository.findByNome(nome)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setNome(nome);
                    role.setDescricao(descricao);
                    role.setPermissoes(permissoes);
                    role.setAtivo(true);
                    return roleRepository.save(role);
                });
    }

    private Permissao getPermissao(String nome) {
        return permissaoRepository.findByNome(nome)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada: " + nome));
    }
}