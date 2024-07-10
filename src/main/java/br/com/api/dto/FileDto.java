package br.com.api.dto;

import br.com.api.entities.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FileDto(

        @NotNull(message = "The field validity cannot be empty")
        LocalDate validity,
        User user){
}
