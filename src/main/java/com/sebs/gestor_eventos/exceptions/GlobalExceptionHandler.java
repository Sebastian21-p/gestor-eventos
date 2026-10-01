package com.sebs.gestor_eventos.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail manejarAccesoDenegado(AccesoDenegadoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Valor inválido"),
                        (primero, segundo) -> primero,
                        LinkedHashMap::new));

        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud tiene datos inválidos");
        problem.setProperty("errores", errores);
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue());
    }

}
