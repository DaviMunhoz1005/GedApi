package br.com.api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_USER")
@Builder
public class User {

    /*

    TODO - Implementar a diferenciação de roles para eu controlar o User, permissões dos documento;
           Relação de User com File_, fazer com que eu precise informar apenas o nome ou id do usuário;
           Criar DTO;
           Rever vídeo do Guia Definitivo para retornar um token de forma formatada pelo dto LoginResponse q ele fez;

    */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull(message = "The field username cannot be empty")
    private String username;

    @NotNull(message = "The field password cannot be empty")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USERS_ROLES",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;
}
