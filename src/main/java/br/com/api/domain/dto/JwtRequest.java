package br.com.api.domain.dto;

public record JwtRequest(

        String username,

        String password) {
}
