package br.com.api.domain.dto;

import br.com.api.domain.entities.Roles;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponse(

        UUID userId,

        UUID clientId,

        String username,

        String nameCorporateReason,

        String email,

        String cnpjCpf,

        String cnae,

        Boolean excluded,

        Roles role) {
}
