package br.com.api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_EMPLOYEE")
public class Employee extends User{

    @ManyToOne
    @JoinColumn(name = "client_id")
    @NotNull(message = "The field client cannot be empty")
    private Client client;
}
