package br.com.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_EMPLOYEE")
public class Employee extends User{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @NotNull(message = "The field client cannot be empty")
    @JsonIgnore
    private Client client;

    @Builder
    public Employee(String username, String password, List<Role> roleList, Client client) {

        super(username, password, roleList);
        this.client = client;
    }
}
