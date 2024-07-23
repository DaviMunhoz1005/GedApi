package br.com.api.dto;

import br.com.api.entities.Document;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DocumentResponse(

        String name,

        Document originalDocument,

        String extension,

        Integer version,

        LocalDate validity,

        LocalDate creation,

        LocalDate updated,

        LocalDate exclusion) {
}
