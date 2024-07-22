package br.com.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_CLIENT")
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "name_corporate_reason", unique = true)
    @NotNull(message = "The field name_corporate_reason cannot be empty")
    private String nameCorporateReason;

    @Column(name = "cnpj_cpf", unique = true)
    @NotNull(message = "The field cnpj_cpf cannot be empty")
    private String cnpjCpf;

    @Column(name = "cnae", unique = true)
    private String cnae;

    @ManyToMany(mappedBy = "clients")
    private List<User> users;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_uuid")
    private List<Document> documentList;
}
