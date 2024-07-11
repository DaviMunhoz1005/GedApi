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

    TODO - Pesquisar como manipular permissões de usuários de acordo com a role;
           Criar um atributo de descrição do que a role faz e não faz;

    */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName roleName;
}
