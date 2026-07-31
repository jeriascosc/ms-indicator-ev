package com.trycore.msindicatorev.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.service.ActivityService;
import com.trycore.msindicatorev.web.dto.ActivityRequest;
import com.trycore.msindicatorev.web.error.ActivityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** El slice @WebMvcTest no expone el ObjectMapper como bean; aqui solo se usa para serializar el cuerpo. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ActivityService activityService;

    private static Activity storedActivity() {
        Activity activity = new Activity("Cimentacion", new BigDecimal("10000.0000"),
                new BigDecimal("0.6000"), new BigDecimal("0.4500"), new BigDecimal("5200.0000"));
        activity.setId(1L);
        activity.setCreated("system");
        activity.setCreateDate(LocalDateTime.of(2026, 7, 29, 10, 15, 30));
        return activity;
    }

    private static ActivityRequest validRequest() {
        return new ActivityRequest("Cimentacion", new BigDecimal("10000.00"),
                new BigDecimal("0.60"), new BigDecimal("0.45"), new BigDecimal("5200.00"));
    }

    @Test
    @DisplayName("GET /api/v1/activities devuelve 200 con un array JSON plano")
    void listsActivities() throws Exception {
        given(activityService.findAll()).willReturn(List.of(storedActivity()));

        mockMvc.perform(get("/api/v1/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cimentacion"))
                .andExpect(jsonPath("$[0]._links").doesNotExist())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/activities/{id} devuelve 200 con el DTO plano, sin enlaces")
    void getsActivityById() throws Exception {
        given(activityService.findById(1L)).willReturn(storedActivity());

        mockMvc.perform(get("/api/v1/activities/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cimentacion"))
                .andExpect(jsonPath("$.totalPlannedBudget").value(10000.0000))
                .andExpect(jsonPath("$.created").value("system"))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/activities/{id} devuelve 404 con ProblemDetail si no existe")
    void returnsNotFoundForMissingActivity() throws Exception {
        given(activityService.findById(99L)).willThrow(new ActivityNotFoundException(99L));

        mockMvc.perform(get("/api/v1/activities/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Actividad no encontrada"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.activityId").value(99));
    }

    @Test
    @DisplayName("GET con un id no numerico devuelve 400")
    void rejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/activities/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametro invalido"));
    }

    @Test
    @DisplayName("POST /api/v1/activities devuelve 201 con cabecera Location")
    void createsActivity() throws Exception {
        given(activityService.create(any(ActivityRequest.class), eq("jeriasco"))).willReturn(storedActivity());

        mockMvc.perform(post("/api/v1/activities")
                        .header("X-User", "jeriasco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/activities/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.created").value("system"))
                .andExpect(jsonPath("$._links").doesNotExist());

        verify(activityService).create(any(ActivityRequest.class), eq("jeriasco"));
    }

    @Test
    @DisplayName("POST sin cabecera X-User propaga null al servicio")
    void createsActivityWithoutUserHeader() throws Exception {
        given(activityService.create(any(ActivityRequest.class), eq(null))).willReturn(storedActivity());

        mockMvc.perform(post("/api/v1/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated());

        verify(activityService).create(any(ActivityRequest.class), eq(null));
    }

    @Test
    @DisplayName("POST con porcentaje fuera de rango devuelve 400 detallando el campo")
    void rejectsOutOfRangePercentage() throws Exception {
        ActivityRequest invalid = new ActivityRequest("Cimentacion", new BigDecimal("10000.00"),
                new BigDecimal("1.50"), new BigDecimal("0.45"), new BigDecimal("5200.00"));

        mockMvc.perform(post("/api/v1/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Datos de entrada invalidos"))
                .andExpect(jsonPath("$.errors.porcentPlanned").exists());
    }

    @Test
    @DisplayName("POST sin nombre devuelve 400")
    void rejectsMissingName() throws Exception {
        ActivityRequest invalid = new ActivityRequest("  ", new BigDecimal("10000.00"),
                new BigDecimal("0.60"), new BigDecimal("0.45"), new BigDecimal("5200.00"));

        mockMvc.perform(post("/api/v1/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("POST con un cuerpo JSON malformado devuelve 400, no 500")
    void rejectsMalformedJsonBody() throws Exception {
        mockMvc.perform(post("/api/v1/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"roto\", esto no es json}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Cuerpo de la peticion ilegible"));
    }

    @Test
    @DisplayName("PUT /api/v1/activities/{id} devuelve 200 con la actividad actualizada")
    void updatesActivity() throws Exception {
        given(activityService.update(eq(1L), any(ActivityRequest.class), eq("jeriasco")))
                .willReturn(storedActivity());

        mockMvc.perform(put("/api/v1/activities/1")
                        .header("X-User", "jeriasco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("PUT sobre un id inexistente devuelve 404")
    void returnsNotFoundOnUpdate() throws Exception {
        given(activityService.update(eq(99L), any(ActivityRequest.class), any()))
                .willThrow(new ActivityNotFoundException(99L));

        mockMvc.perform(put("/api/v1/activities/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.activityId").value(99));
    }

    @Test
    @DisplayName("DELETE /api/v1/activities/{id} devuelve 204 sin cuerpo")
    void deletesActivity() throws Exception {
        doNothing().when(activityService).delete(1L);

        mockMvc.perform(delete("/api/v1/activities/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(activityService).delete(1L);
    }

    @Test
    @DisplayName("DELETE sobre un id inexistente devuelve 404")
    void returnsNotFoundOnDelete() throws Exception {
        willThrow(new ActivityNotFoundException(99L)).given(activityService).delete(99L);

        mockMvc.perform(delete("/api/v1/activities/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
