package reset.reset.Models.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 20, nullable = false, unique = true)
    private String nuit;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    @Column(length = 50)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 10)
    private String moeda = "MZN";

    @Column(length = 100)
    private String pais = "Moçambique";

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @Column(columnDefinition = "TEXT")
    private String logotipo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

//    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
//    private List<Utilizador> utilizadores;
//
//    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
//    private List<Cliente> clientes;
//
//    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
//    private List<Fornecedor> fornecedores;
//
//    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
//    private List<Iva> ivas;
//
//    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
//    private List<Produto> produtos;
//
//    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
//    private List<CategoriaProduto> categorias;
//
//    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
//    private List<Armazem> armazens;
}