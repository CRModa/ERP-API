package reset.reset.Models.auth;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissao")
@Getter
@Setter
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

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @ManyToMany(mappedBy = "permissoes")
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "permissoes")
    private Set<User> utilizadores = new HashSet<>();
}
