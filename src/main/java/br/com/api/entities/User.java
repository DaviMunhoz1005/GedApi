package br.com.api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_USER")
@Builder
public class User implements UserDetails {

    /*

    TODO - Criar a relação User File;
           Analisar métodos que precisam ser criados e modificados com a relação User File_;
           Pensar na lógica de renovação de expiresIn do Token;
           Pensar na lógica de outra table para version dos documentos;
           Analisar JSON na área de trabalho;

    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

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
    private List<Role> roleList;

    @Override
    public String getPassword() {

        return this.password;
    }

    @Override
    public String getUsername() {

        return this.username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return roleList.stream()
                .map(role -> (GrantedAuthority) () -> role.getRoleName().name())
                .toList();
    }
}
