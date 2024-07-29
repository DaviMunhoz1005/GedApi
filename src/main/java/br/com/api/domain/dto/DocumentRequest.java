package br.com.api.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DocumentRequest(

        @NotNull(message = "The field validity_date cannot be empty")
        LocalDate validity){
}
