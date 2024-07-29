package br.com.api.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_USER_CLIENT")
@Builder
public class UserClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "client_uuid", nullable = false)
    private Clients client;

    @Column(name = "approved_request")
    private Boolean approvedRequest;
}
