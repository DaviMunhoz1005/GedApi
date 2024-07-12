package br.com.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FileDto(

        @NotNull(message = "The field validity cannot be empty")
        LocalDate validity,

        @NotNull(message = "The field username cannot be empty")
        String username){
}
