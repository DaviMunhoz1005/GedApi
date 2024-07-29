package br.com.api.domain.entities;

import br.com.api.domain.enums.RoleName;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_ROLES")
@Builder
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @Column(unique = true, updatable = false)
    private String description;
}
