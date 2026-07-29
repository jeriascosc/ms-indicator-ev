package com.trycore.msindicatorev;

import com.trycore.msindicatorev.web.dto.ActivityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de extremo a extremo contra el contexto completo y la base H2 en memoria,
 * incluyendo los datos precargados por data.sql.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class MsIndicatorEvApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static HttpEntity<ActivityRequest> jsonRequest(ActivityRequest body, String user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (user != null) {
            headers.set("X-User", user);
        }
        return new HttpEntity<>(body, headers);
    }

    @Test
    @DisplayName("El contexto arranca y expone la documentacion OpenAPI")
    void contextLoadsAndPublishesOpenApi() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api-docs"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("ms-indicator-ev API")
                .contains("/api/v1/activities/{id}/indicators")
                .contains("/api/v1/activities/{id}/interpretation");
    }

    @Test
    @DisplayName("Los datos de ejemplo de data.sql se cargan y producen los cuatro cuadrantes")
    void seedDataCoversEveryQuadrant() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(url("/api/v1/activities/interpretations"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Proyecto ideal")
                .contains("Proyecto crítico")
                .contains("Proyecto con mayor gasto y retrasado")
                .contains("Rápido avance a mayor costo")
                .contains("Proyecto conforme a lo planeado")
                .contains("No calculable");
    }

    @Test
    @DisplayName("Ciclo completo: crear, consultar indicadores, actualizar y eliminar")
    void fullCrudAndIndicatorFlow() {
        ActivityRequest request = new ActivityRequest("Actividad de integracion",
                new BigDecimal("10000.00"), new BigDecimal("0.50"), new BigDecimal("0.60"),
                new BigDecimal("5000.00"));

        // POST -> 201 con cabecera Location
        ResponseEntity<String> created = restTemplate.postForEntity(
                url("/api/v1/activities"), jsonRequest(request, "jeriasco"), String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI location = created.getHeaders().getLocation();
        assertThat(location).isNotNull();
        assertThat(created.getBody()).contains("\"created\":\"jeriasco\"").contains("_links");

        // GET indicadores -> CPI y SPI de 1.2
        ResponseEntity<String> indicators =
                restTemplate.getForEntity(location + "/indicators", String.class);
        assertThat(indicators.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(indicators.getBody())
                .contains("\"pv\":5000.0000")
                .contains("\"ev\":6000.0000")
                .contains("\"cpi\":1.2000")
                .contains("\"spi\":1.2000")
                .contains("\"eac\":8333.3333");

        // GET interpretacion -> proyecto ideal
        ResponseEntity<String> interpretation =
                restTemplate.getForEntity(location + "/interpretation", String.class);
        assertThat(interpretation.getBody()).contains("Proyecto ideal");

        // PUT -> queda registrado el usuario que modifica
        ActivityRequest changes = new ActivityRequest("Actividad de integracion v2",
                new BigDecimal("10000.00"), new BigDecimal("0.50"), new BigDecimal("0.40"),
                new BigDecimal("5000.00"));
        ResponseEntity<String> updated = restTemplate.exchange(location, HttpMethod.PUT,
                jsonRequest(changes, "otro-usuario"), String.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody())
                .contains("Actividad de integracion v2")
                .contains("\"createUpdate\":\"otro-usuario\"");

        // DELETE -> 204 y a partir de ahi 404
        ResponseEntity<Void> deleted =
                restTemplate.exchange(location, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> afterDelete = restTemplate.getForEntity(location, String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(afterDelete.getBody()).contains("Actividad no encontrada");
    }

    @Test
    @DisplayName("Una actividad invalida es rechazada con 400 y el detalle del campo")
    void rejectsInvalidActivity() {
        ActivityRequest invalid = new ActivityRequest("Invalida", new BigDecimal("10000.00"),
                new BigDecimal("1.50"), new BigDecimal("0.45"), new BigDecimal("5200.00"));

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/activities"), jsonRequest(invalid, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("porcentPlanned");
    }
}
