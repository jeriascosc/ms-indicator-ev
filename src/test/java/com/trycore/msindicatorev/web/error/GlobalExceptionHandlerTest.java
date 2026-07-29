package com.trycore.msindicatorev.web.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("La actividad no encontrada produce un ProblemDetail 404 con el id consultado")
    void buildsNotFoundProblem() {
        ProblemDetail problem = handler.handleNotFound(new ActivityNotFoundException(99L));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Actividad no encontrada");
        assertThat(problem.getProperties()).containsEntry("activityId", 99L);
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Un parametro con formato invalido produce un ProblemDetail 400")
    void buildsTypeMismatchProblem() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);

        ProblemDetail problem = handler.handleTypeMismatch(exception);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Parametro invalido");
        assertThat(problem.getDetail()).contains("id");
    }

    @Test
    @DisplayName("Un cuerpo JSON ilegible produce un ProblemDetail 400, no un 500")
    void buildsUnreadableBodyProblem() {
        ProblemDetail problem = handler.handleUnreadableBody(
                new HttpMessageNotReadableException("JSON parse error", (HttpInputMessage) null));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Cuerpo de la peticion ilegible");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Cualquier error no previsto se traduce a un ProblemDetail 500 sin filtrar detalles internos")
    void buildsInternalErrorProblem() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("fallo de conexion a la base"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Error interno del servidor");
        assertThat(problem.getDetail()).doesNotContain("fallo de conexion a la base");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }
}
