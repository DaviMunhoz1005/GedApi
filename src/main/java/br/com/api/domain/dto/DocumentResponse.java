package br.com.api.domain.dto;

import br.com.api.domain.entities.Documents;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DocumentResponse(

        String name,

        Documents originalDocument,

        String extension,

        Integer version,

        LocalDate validity,

        LocalDate creation,

        LocalDate updated,

        LocalDate exclusion) {
}
