package br.com.api.dto;

import br.com.api.entities.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FileDto(

        @NotNull(message = "The field validity cannot be empty")
        LocalDate validity,

        @NotNull(message = "The field user cannot be empty")
        User user){
}
