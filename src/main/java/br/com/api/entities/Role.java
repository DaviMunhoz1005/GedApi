package br.com.api.entities;

import br.com.api.entities.enums.RoleName;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_ROLES")
@Builder
public class Role {

    /*

    TODO - pesquisar como manipular permissões de usuários de acordo com a role

    */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName roleName;
}
