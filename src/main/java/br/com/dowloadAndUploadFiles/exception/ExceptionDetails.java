package br.com.dowloadAndUploadFiles.exception;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public class ExceptionDetails {

    /*

    TODO - criar exceções personalizadas para cada caso na API

    */

    protected String title;
    protected Integer statusCode;
    protected String cause;
    protected LocalDateTime timestamp;
}
