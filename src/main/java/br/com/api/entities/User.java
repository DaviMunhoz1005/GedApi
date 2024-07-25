package br.com.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
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
    *
    * TODO - Estudar e implementar refresh Token;
    *        Pensar na lógica de ter uma Tabela separada para as versões dos documentos;
    *
    * */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(unique = true)
    @NotNull(message = "The field username cannot be empty")
    private String username;

    @NotNull(message = "The field email cannot be empty")
    private String email;

    @NotNull(message = "The field password cannot be empty")
    private String password;

    @NotNull(message = "The field password cannot be empty")
    private Boolean excluded;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USERS_ROLES",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roleList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid_creation")
    private List<Document> listDocumentsCreation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid_exclusion")
    private List<Document> listDocumentsExclusion;

    @ManyToMany
    @JoinTable(
            name = "TB_USER_CLIENT",
            joinColumns = @JoinColumn(name = "user_uuid"),
            inverseJoinColumns = @JoinColumn(name = "client_uuid"))
    @JsonIgnore
    private List<Client> clients;

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