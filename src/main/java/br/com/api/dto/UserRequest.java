package br.com.api.dto;

import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotNull(message = "The field username cannot be empty")
        String username,

        @NotNull(message = "The field name_corporate_reason cannot be empty")
        String nameCorporateReason,

        @NotNull(message = "The field email cannot be empty")
        String email,

        @NotNull(message = "The field password cannot be empty")
        String password,

        @NotNull(message = "The field cnpj_cpf cannot be empty")
        String cnpjCpf,

        String cnae,

        @NotNull(message = "The field roleInt cannot be empty")
        Long roleInt) {
}
