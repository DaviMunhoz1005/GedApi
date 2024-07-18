package br.com.api.dto;

public record JwtResponse(

        String accessToken,
        String expiresIn) {
}
