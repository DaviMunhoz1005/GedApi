package br.com.api.domain.dto;

import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotNull(message = "The field username cannot be empty")
        String username,

        String nameCorporateReason,

        @NotNull(message = "The field email cannot be empty")
        String email,

        @NotNull(message = "The field password cannot be empty")
        String password,

        @NotNull(message = "The field cnpj_cpf cannot be empty")
        String cnpjCpf,

        String cnae) {
}
