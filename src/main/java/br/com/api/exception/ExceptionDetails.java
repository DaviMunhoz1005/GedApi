package br.com.api.exception;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public class ExceptionDetails {

    protected String title;
    protected Integer statusCode;
    protected String cause;
    protected LocalDateTime timestamp;
}
