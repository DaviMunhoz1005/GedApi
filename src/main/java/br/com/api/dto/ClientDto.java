package br.com.api.dto;

import jakarta.validation.constraints.NotNull;

public record ClientDto(

        @NotNull(message = "The field username cannot be empty")
        String username,

        @NotNull(message = "The field password cannot be empty")
        String password,

        @NotNull(message = "The field roleInt cannot be empty")
        Long roleInt) {
}
