package br.com.api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_FILE")
@Builder
public class File_ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "The field name cannot be empty")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "The field extension cannot be empty")
    @Column(nullable = false)
    private String extension;

    @NotNull(message = "The field version cannot be empty")
    @Column(nullable = false)
    private Integer version;

    @NotNull(message = "The field validity cannot be empty")
    @Column(nullable = false)
    private LocalDate validity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "The field user cannot be empty")
    private User user;
}
