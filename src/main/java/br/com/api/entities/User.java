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
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements UserDetails {

    /*
    *
    * TODO - Criar documentação do Spring OpenAPI Swagger;
    *        Trocar Filename para FileName na aplicação inteira;
    *        Estudar e implementar refresh Token;
    *        Pensar na lógica de ter uma Tabela separada para as versões dos documentos;
    *
    * */

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid")
    private List<File_> fileList;

    public User(String username, String password, List<Role> roleList) {

        this.username = username;
        this.password = password;
        this.roleList = roleList;
    }

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