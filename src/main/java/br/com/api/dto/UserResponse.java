package br.com.api.dto;

import br.com.api.entities.Role;
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

        Role role) {
}
