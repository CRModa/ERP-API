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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissaoRepository permissaoRepository;
    private final RoleRepository roleRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Create default empresa
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa Principal");
        empresa.setNuit("100000000");
        empresa.setEmail("admin@empresa.com");
        empresa.setTelefone("+258 84 000 0000");
        empresa.setAtivo(true);
        empresa = empresaRepository.save(empresa);

        // Create permissions
        Permissao produtoCreate = createPermissao("PRODUTO_CREATE", "Criar produtos", "PRODUTO", "CREATE");
        Permissao produtoRead = createPermissao("PRODUTO_READ", "Visualizar produtos", "PRODUTO", "READ");
        Permissao produtoUpdate = createPermissao("PRODUTO_UPDATE", "Atualizar produtos", "PRODUTO", "UPDATE");
        Permissao produtoDelete = createPermissao("PRODUTO_DELETE", "Excluir produtos", "PRODUTO", "DELETE");

        Permissao vendaCreate = createPermissao("VENDA_CREATE", "Criar vendas", "VENDA", "CREATE");
        Permissao vendaRead = createPermissao("VENDA_READ", "Visualizar vendas", "VENDA", "READ");
        Permissao vendaUpdate = createPermissao("VENDA_UPDATE", "Atualizar vendas", "VENDA", "UPDATE");
        Permissao vendaDelete = createPermissao("VENDA_DELETE", "Excluir vendas", "VENDA", "DELETE");

        Permissao clienteCreate = createPermissao("CLIENTE_CREATE", "Criar clientes", "CLIENTE", "CREATE");
        Permissao clienteRead = createPermissao("CLIENTE_READ", "Visualizar clientes", "CLIENTE", "READ");
        Permissao clienteUpdate = createPermissao("CLIENTE_UPDATE", "Atualizar clientes", "CLIENTE", "UPDATE");
        Permissao clienteDelete = createPermissao("CLIENTE_DELETE", "Excluir clientes", "CLIENTE", "DELETE");

        Permissao utilizadorCreate = createPermissao("UTILIZADOR_CREATE", "Criar utilizadores", "UTILIZADOR", "CREATE");
        Permissao utilizadorRead = createPermissao("UTILIZADOR_READ", "Visualizar utilizadores", "UTILIZADOR", "READ");
        Permissao utilizadorUpdate = createPermissao("UTILIZADOR_UPDATE", "Atualizar utilizadores", "UTILIZADOR", "UPDATE");
        Permissao utilizadorDelete = createPermissao("UTILIZADOR_DELETE", "Excluir utilizadores", "UTILIZADOR", "DELETE");

        Permissao relatorioRead = createPermissao("RELATORIO_READ", "Visualizar relatórios", "RELATORIO", "READ");
        Permissao relatorioExport = createPermissao("RELATORIO_EXPORT", "Exportar relatórios", "RELATORIO", "EXECUTE");

        // Create Roles
        Set<Permissao> adminPermissoes = new HashSet<>(Arrays.asList(
                produtoCreate, produtoRead, produtoUpdate, produtoDelete,
                vendaCreate, vendaRead, vendaUpdate, vendaDelete,
                clienteCreate, clienteRead, clienteUpdate, clienteDelete,
                utilizadorCreate, utilizadorRead, utilizadorUpdate, utilizadorDelete,
                relatorioRead, relatorioExport
        ));

        Role adminRole = createRole("ADMIN", "Administrador do sistema", adminPermissoes);

        Set<Permissao> vendedorPermissoes = new HashSet<>(Arrays.asList(
                produtoRead,
                vendaCreate, vendaRead,
                clienteCreate, clienteRead, clienteUpdate,
                relatorioRead
        ));

        Role vendedorRole = createRole("VENDEDOR", "Vendedor", vendedorPermissoes);

        Set<Permissao> contabilistaPermissoes = new HashSet<>(Arrays.asList(
                produtoRead,
                vendaRead,
                clienteRead,
                relatorioRead, relatorioExport
        ));

        Role contabilistaRole = createRole("CONTABILISTA", "Contabilista", contabilistaPermissoes);

        Set<Permissao> gerentePermissoes = new HashSet<>(Arrays.asList(
                produtoCreate, produtoRead, produtoUpdate,
                vendaCreate, vendaRead, vendaUpdate,
                clienteCreate, clienteRead, clienteUpdate,
                relatorioRead, relatorioExport
        ));

        Role gerenteRole = createRole("GERENTE", "Gerente", gerentePermissoes);

        // Create admin user
        if (!utilizadorRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setEmpresa(empresa);
            admin.setNome("Administrador");
            admin.setUsername("admin");
            admin.setEmail("admin@empresa.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPerfil("ADMIN");
            admin.setAtivo(true);
            admin.setRoles(new HashSet<>(Arrays.asList(adminRole)));
            utilizadorRepository.save(admin);
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
}
