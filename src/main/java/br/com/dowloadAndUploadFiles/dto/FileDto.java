package br.com.dowloadAndUploadFiles.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FileDto(

        @NotNull(message = "The field name cannot be empty")
        String name,
        @NotNull(message = "The field validity cannot be empty")
        LocalDate validity) {
}
