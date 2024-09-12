package br.com.api.domain.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DocumentInfosUpdateResponse(

        String name,

        Integer version,

        LocalDate creation,

        LocalDate updated) {
}
