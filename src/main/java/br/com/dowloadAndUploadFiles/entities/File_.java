package br.com.dowloadAndUploadFiles.entities;

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
    private String name;

    @NotNull(message = "The field extension cannot be empty")
    private String extension;

    @NotNull(message = "The field version cannot be empty")
    private Integer version;

    @NotNull(message = "The field validity cannot be empty")
    private LocalDate validity;
}
