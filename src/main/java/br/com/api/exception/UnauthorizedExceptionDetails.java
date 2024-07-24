package br.com.api.exception;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@SuperBuilder
public class UnauthorizedExceptionDetails extends ExceptionDetails {

    public UnauthorizedExceptionDetails(String title, int statusCode, String cause) {

        super(title, statusCode, cause, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
