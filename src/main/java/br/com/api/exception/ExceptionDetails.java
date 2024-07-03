package br.com.api.exception;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class ExceptionDetails {

    protected String title;
    protected Integer statusCode;
    protected String cause;
    protected String timestamp;
}
