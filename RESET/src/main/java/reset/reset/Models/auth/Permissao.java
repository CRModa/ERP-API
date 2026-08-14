package reset.reset.Models.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String nome; // EX: PRODUTO_CREATE, PRODUTO_READ, VENDA_CREATE, etc

    @Column(length = 200)
    private String descricao;

    @Column(length = 50)
    private String recurso; // PRODUTO, VENDA, CLIENTE, USUARIO, etc

    @Column(length = 20)
    private String acao; // CREATE, READ, UPDATE, DELETE, EXECUTE

    @ManyToMany(mappedBy = "permissoes")
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "permissoes")
    private Set<User> utilizadores = new HashSet<>();
}
