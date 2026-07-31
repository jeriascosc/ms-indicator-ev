package com.trycore.msindicatorev.web;

import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.service.ActivityService;
import com.trycore.msindicatorev.service.IndicatorService;
import com.trycore.msindicatorev.web.error.ActivityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IndicatorController.class)
@Import(IndicatorService.class)
class IndicatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    /** BAC=10000, PV=5000, EV=6000, AC=5000 -> CPI=1.2, SPI=1.2 (proyecto ideal). */
    private static Activity idealActivity() {
        Activity activity = new Activity("Cimentacion", new BigDecimal("10000.0000"),
                new BigDecimal("0.5000"), new BigDecimal("0.6000"), new BigDecimal("5000.0000"));
        activity.setId(1L);
        return activity;
    }

    /** AC=0 y PV=0 -> indicadores no calculables. */
    private static Activity notStartedActivity() {
        Activity activity = new Activity("Actividad no iniciada", new BigDecimal("10000.0000"),
                new BigDecimal("0.0000"), new BigDecimal("0.0000"), new BigDecimal("0.0000"));
        activity.setId(2L);
        return activity;
    }

    @Test
    @DisplayName("GET /api/v1/activities/indicators devuelve un array plano con todos los registros")
    void listsIndicatorsForEveryActivity() throws Exception {
        given(activityService.findAll()).willReturn(List.of(idealActivity(), notStartedActivity()));

        mockMvc.perform(get("/api/v1/activities/indicators"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].activityName").value("Cimentacion"))
                .andExpect(jsonPath("$[1].activityName").value("Actividad no iniciada"))
                .andExpect(jsonPath("$._links").doesNotExist())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/activities/{id}/indicators calcula las ocho formulas EVM")
    void getsIndicatorsForOneActivity() throws Exception {
        given(activityService.findById(1L)).willReturn(idealActivity());

        mockMvc.perform(get("/api/v1/activities/1/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId").value(1))
                .andExpect(jsonPath("$.bac").value(10000.0000))
                .andExpect(jsonPath("$.pv").value(5000.0000))
                .andExpect(jsonPath("$.ev").value(6000.0000))
                .andExpect(jsonPath("$.cv").value(1000.0000))
                .andExpect(jsonPath("$.sv").value(1000.0000))
                .andExpect(jsonPath("$.cpi").value(1.2000))
                .andExpect(jsonPath("$.spi").value(1.2000))
                .andExpect(jsonPath("$.eac").value(8333.3333))
                .andExpect(jsonPath("$.vac").value(1666.6667))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("Los indicadores con divisor cero se devuelven como null")
    void returnsNullIndicatorsWhenNotComputable() throws Exception {
        given(activityService.findById(2L)).willReturn(notStartedActivity());

        mockMvc.perform(get("/api/v1/activities/2/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpi").doesNotExist())
                .andExpect(jsonPath("$.spi").doesNotExist())
                .andExpect(jsonPath("$.eac").doesNotExist())
                .andExpect(jsonPath("$.vac").doesNotExist())
                .andExpect(jsonPath("$.pv").value(0));
    }

    @Test
    @DisplayName("GET de indicadores sobre un id inexistente devuelve 404")
    void returnsNotFoundForIndicators() throws Exception {
        given(activityService.findById(99L)).willThrow(new ActivityNotFoundException(99L));

        mockMvc.perform(get("/api/v1/activities/99/indicators"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.activityId").value(99));
    }

    @Test
    @DisplayName("GET /api/v1/activities/interpretations devuelve un array plano con todos los registros")
    void listsInterpretationsForEveryActivity() throws Exception {
        given(activityService.findAll()).willReturn(List.of(idealActivity(), notStartedActivity()));

        mockMvc.perform(get("/api/v1/activities/interpretations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cpiVsSpiAnalysis").value("Proyecto ideal"))
                .andExpect(jsonPath("$[1].cpiVsSpiAnalysis").value("No calculable"))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/activities/{id}/interpretation traduce CPI y SPI a lenguaje de negocio")
    void getsInterpretationForOneActivity() throws Exception {
        given(activityService.findById(1L)).willReturn(idealActivity());

        mockMvc.perform(get("/api/v1/activities/1/interpretation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId").value(1))
                .andExpect(jsonPath("$.cpi").value(1.2000))
                .andExpect(jsonPath("$.spi").value(1.2000))
                .andExpect(jsonPath("$.cpiStatus").value("Eficiencia de Costo"))
                .andExpect(jsonPath("$.spiStatus").value("Avanza más de lo previsto"))
                .andExpect(jsonPath("$.cpiVsSpiAnalysis").value("Proyecto ideal"))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("La interpretacion reporta 'No calculable' cuando faltan los indices")
    void reportsNotComputableInterpretation() throws Exception {
        given(activityService.findById(2L)).willReturn(notStartedActivity());

        mockMvc.perform(get("/api/v1/activities/2/interpretation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpiStatus").value("No calculable"))
                .andExpect(jsonPath("$.spiStatus").value("No calculable"))
                .andExpect(jsonPath("$.cpiVsSpiAnalysis").value("No calculable"));
    }

    @Test
    @DisplayName("GET de interpretacion sobre un id inexistente devuelve 404")
    void returnsNotFoundForInterpretation() throws Exception {
        given(activityService.findById(99L)).willThrow(new ActivityNotFoundException(99L));

        mockMvc.perform(get("/api/v1/activities/99/interpretation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
