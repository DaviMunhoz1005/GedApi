package br.com.api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_DOCUMENT")
@Builder
public class Document {

    /*
    *
    * TODO - renomear de file para document;
    *        fazer documentDto adequado;
    *        Corrigir file service;
    *        Corrigir file controller;
    *
    * */

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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "original_document_uuid")
    private Document originalDocument;
}
