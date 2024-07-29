package br.com.api.domain.dto;

public record JwtResponse(

        String accessToken,

        String expiresIn) {
}
