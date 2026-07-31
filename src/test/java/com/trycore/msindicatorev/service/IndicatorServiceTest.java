package com.trycore.msindicatorev.service;

import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.web.dto.IndicatorResponse;
import com.trycore.msindicatorev.web.dto.InterpretationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndicatorServiceTest {

    private final IndicatorService service = new IndicatorService();

    private static Activity activity(String budget, String planned, String complete, String actualCost) {
        Activity activity = new Activity("Actividad de prueba",
                new BigDecimal(budget), new BigDecimal(planned), new BigDecimal(complete),
                new BigDecimal(actualCost));
        activity.setId(1L);
        return activity;
    }

    @Test
    @DisplayName("Calcula las ocho formulas EVM sobre un caso conocido")
    void calculatesEveryIndicator() {
        // BAC=10000, PV=5000, EV=6000, AC=5000
        IndicatorResponse result = service.calculate(activity("10000", "0.50", "0.60", "5000"));

        assertThat(result.activityId()).isEqualTo(1L);
        assertThat(result.activityName()).isEqualTo("Actividad de prueba");
        assertThat(result.bac()).isEqualByComparingTo("10000");
        assertThat(result.pv()).isEqualByComparingTo("5000");
        assertThat(result.ev()).isEqualByComparingTo("6000");
        assertThat(result.cv()).isEqualByComparingTo("1000");   // EV - AC
        assertThat(result.sv()).isEqualByComparingTo("1000");   // EV - PV
        assertThat(result.cpi()).isEqualByComparingTo("1.2");   // EV / AC
        assertThat(result.spi()).isEqualByComparingTo("1.2");   // EV / PV
        assertThat(result.eac()).isEqualByComparingTo("8333.3333"); // BAC / CPI
        assertThat(result.vac()).isEqualByComparingTo("1666.6667"); // BAC - EAC
    }

    @Test
    @DisplayName("Los indicadores se redondean a cuatro decimales")
    void appliesScaleOfFour() {
        IndicatorResponse result = service.calculate(activity("10000", "0.50", "0.30", "7000"));

        assertThat(result.cpi().scale()).isEqualTo(IndicatorService.SCALE);
        assertThat(result.cpi()).isEqualByComparingTo("0.4286"); // 3000/7000 redondeado HALF_UP
        assertThat(result.pv().scale()).isEqualTo(IndicatorService.SCALE);
    }

    @Test
    @DisplayName("CV y SV pueden ser negativos cuando hay sobrecosto o retraso")
    void producesNegativeVariances() {
        IndicatorResponse result = service.calculate(activity("10000", "0.60", "0.40", "8000"));

        assertThat(result.cv()).isEqualByComparingTo("-4000"); // 4000 - 8000
        assertThat(result.sv()).isEqualByComparingTo("-2000"); // 4000 - 6000
        assertThat(result.vac()).isNegative();
    }

    @Test
    @DisplayName("Con costo real cero, CPI, EAC y VAC quedan sin calcular")
    void returnsNullWhenActualCostIsZero() {
        IndicatorResponse result = service.calculate(activity("10000", "0.50", "0.40", "0"));

        assertThat(result.cpi()).isNull();
        assertThat(result.eac()).isNull();
        assertThat(result.vac()).isNull();
        assertThat(result.spi()).isEqualByComparingTo("0.8"); // PV no es cero, SPI si se calcula
        assertThat(result.cv()).isEqualByComparingTo("4000");
    }

    @Test
    @DisplayName("Con valor planificado cero, SPI queda sin calcular")
    void returnsNullWhenPlannedValueIsZero() {
        IndicatorResponse result = service.calculate(activity("10000", "0.00", "0.40", "5000"));

        assertThat(result.spi()).isNull();
        assertThat(result.pv()).isEqualByComparingTo("0");
        assertThat(result.cpi()).isEqualByComparingTo("0.8"); // AC no es cero, CPI si se calcula
    }

    @Test
    @DisplayName("Con EV cero el CPI es cero y por tanto EAC y VAC no son calculables")
    void returnsNullEacWhenCpiIsZero() {
        IndicatorResponse result = service.calculate(activity("10000", "0.50", "0.00", "5000"));

        assertThat(result.cpi()).isEqualByComparingTo("0");
        assertThat(result.eac()).isNull();
        assertThat(result.vac()).isNull();
    }

    @Test
    @DisplayName("calculateAll procesa la lista completa preservando el orden")
    void calculatesForEveryActivity() {
        List<IndicatorResponse> results = service.calculateAll(List.of(
                activity("10000", "0.50", "0.60", "5000"),
                activity("20000", "0.50", "0.50", "10000")));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).ev()).isEqualByComparingTo("6000");
        assertThat(results.get(1).ev()).isEqualByComparingTo("10000");
    }

    @ParameterizedTest(name = "CPI={0} -> {1}")
    @CsvSource({
            "1.0000, Gasto conforme a lo planeado",
            "1.2500, Eficiencia de Costo",
            "0.7500, Mal desempeño Financiero"
    })
    @DisplayName("El estado del CPI cubre igualdad, eficiencia y mal desempeno")
    void mapsCpiStatus(String cpi, String expected) {
        assertThat(service.cpiStatus(new BigDecimal(cpi))).isEqualTo(expected);
    }

    @Test
    @DisplayName("Un CPI nulo se reporta como no calculable")
    void mapsNullCpiStatus() {
        assertThat(service.cpiStatus(null)).isEqualTo(IndicatorService.NOT_COMPUTABLE);
    }

    @ParameterizedTest(name = "SPI={0} -> {1}")
    @CsvSource({
            "1.0000, Avanza conforme a lo planeado",
            "1.2000, Avanza más de lo previsto",
            "0.8000, Retrazado"
    })
    @DisplayName("El estado del SPI cubre igualdad, adelanto y retraso")
    void mapsSpiStatus(String spi, String expected) {
        assertThat(service.spiStatus(new BigDecimal(spi))).isEqualTo(expected);
    }

    @Test
    @DisplayName("Un SPI nulo se reporta como no calculable")
    void mapsNullSpiStatus() {
        assertThat(service.spiStatus(null)).isEqualTo(IndicatorService.NOT_COMPUTABLE);
    }

    @ParameterizedTest(name = "CPI={0} SPI={1} -> {2}")
    @CsvSource({
            "1.2000, 1.2000, Proyecto ideal",
            "0.8000, 0.8000, Proyecto crítico",
            "0.7500, 1.2000, Proyecto con mayor gasto y retrasado",
            "1.2500, 0.8000, Rápido avance a mayor costo",
            "1.0000, 1.0000, Proyecto conforme a lo planeado",
            "1.0000, 1.5000, Proyecto conforme a lo planeado",
            "0.9000, 1.0000, Proyecto conforme a lo planeado"
    })
    @DisplayName("El analisis CPI vs SPI cubre los cuatro cuadrantes y los casos de igualdad a 1")
    void mapsCombinedAnalysis(String cpi, String spi, String expected) {
        assertThat(service.analyze(new BigDecimal(cpi), new BigDecimal(spi))).isEqualTo(expected);
    }

    @ParameterizedTest(name = "CPI nulo o SPI nulo -> No calculable")
    @CsvSource({
            "1.0000, ",
            ", 1.0000"
    })
    @DisplayName("El analisis combinado no se emite si falta alguno de los dos indices")
    void skipsAnalysisWhenAnIndexIsMissing(BigDecimal cpi, BigDecimal spi) {
        assertThat(service.analyze(cpi, spi)).isEqualTo(IndicatorService.NOT_COMPUTABLE);
    }

    @Test
    @DisplayName("interpret combina indices y textos de negocio para una actividad")
    void interpretsActivity() {
        InterpretationResponse result = service.interpret(activity("10000", "0.50", "0.40", "5000"));

        assertThat(result.activityId()).isEqualTo(1L);
        assertThat(result.cpi()).isEqualByComparingTo("0.8");
        assertThat(result.spi()).isEqualByComparingTo("0.8");
        assertThat(result.cpiStatus()).isEqualTo("Mal desempeño Financiero");
        assertThat(result.spiStatus()).isEqualTo("Retrazado");
        assertThat(result.cpiVsSpiAnalysis()).isEqualTo("Proyecto crítico");
    }

    @Test
    @DisplayName("interpret reporta no calculable cuando el costo real y el planificado son cero")
    void interpretsNonComputableActivity() {
        InterpretationResponse result = service.interpret(activity("10000", "0.00", "0.00", "0"));

        assertThat(result.cpiStatus()).isEqualTo(IndicatorService.NOT_COMPUTABLE);
        assertThat(result.spiStatus()).isEqualTo(IndicatorService.NOT_COMPUTABLE);
        assertThat(result.cpiVsSpiAnalysis()).isEqualTo(IndicatorService.NOT_COMPUTABLE);
    }

    @Test
    @DisplayName("interpretAll procesa la lista completa")
    void interpretsEveryActivity() {
        List<InterpretationResponse> results = service.interpretAll(List.of(
                activity("10000", "0.50", "0.60", "5000"),
                activity("10000", "0.50", "0.40", "5000")));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).cpiVsSpiAnalysis()).isEqualTo("Proyecto ideal");
        assertThat(results.get(1).cpiVsSpiAnalysis()).isEqualTo("Proyecto crítico");
    }
}
