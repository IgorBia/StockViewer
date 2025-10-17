package com.stockviewer.stockapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String errorCode, List<FieldErrorDTO> errors) {
}
