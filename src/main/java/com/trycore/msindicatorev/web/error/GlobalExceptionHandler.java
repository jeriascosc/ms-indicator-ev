package com.trycore.msindicatorev.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones de la aplicacion a respuestas {@code application/problem+json}
 * siguiendo la RFC 7807, de modo que todos los errores comparten una misma forma.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ActivityNotFoundException.class)
    public ProblemDetail handleNotFound(ActivityNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Actividad no encontrada");
        problem.setType(URI.create("https://api.trycore.com/problems/activity-not-found"));
        problem.setProperty("activityId", exception.getActivityId());
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La actividad enviada no supera las validaciones");
        problem.setTitle("Datos de entrada invalidos");
        problem.setType(URI.create("https://api.trycore.com/problems/validation-error"));
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }

    /**
     * Un cuerpo que no es JSON valido es un error del cliente: sin este manejador caeria en el
     * manejador generico y se responderia un 500 en lugar de un 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no es un JSON valido o no se puede interpretar");
        problem.setTitle("Cuerpo de la peticion ilegible");
        problem.setType(URI.create("https://api.trycore.com/problems/malformed-body"));
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El parametro '" + exception.getName() + "' tiene un formato invalido");
        problem.setTitle("Parametro invalido");
        problem.setType(URI.create("https://api.trycore.com/problems/invalid-parameter"));
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        // La respuesta no revela detalles internos, pero el servidor si debe dejar traza del fallo.
        log.error("Error no controlado procesando la peticion", exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Se produjo un error inesperado procesando la peticion");
        problem.setTitle("Error interno del servidor");
        problem.setType(URI.create("https://api.trycore.com/problems/internal-error"));
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }
}
