package br.com.api.dto;

import lombok.Builder;

@Builder
public record EmployeeResponse(

        String username,

        String email,

        Boolean excluded,

        Boolean approvedRequest) {
}
