package br.com.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_DOCUMENT")
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @NotNull(message = "The field name cannot be empty")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "The field guide name cannot be empty")
    @Column(nullable = false)
    private String guideName;

    @NotNull(message = "The field extension cannot be empty")
    @Column(nullable = false)
    private String extension;

    @NotNull(message = "The field version cannot be empty")
    @Column(nullable = false)
    private Integer version;

    @NotNull(message = "The field validity date cannot be empty")
    @Column(nullable = false)
    private LocalDate validity;

    @NotNull(message = "The field creation date cannot be empty")
    @Column(nullable = false)
    private LocalDate creation;

    private LocalDate updated;

    private LocalDate exclusion;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "original_document_uuid")
    @JsonIgnore
    private Document originalDocument;
}
