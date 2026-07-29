package com.trycore.msindicatorev.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Indicadores de Earned Value Management calculados para una actividad.
 *
 * <p>Los indicadores que implican una division por cero se devuelven como {@code null} en lugar de
 * cero, para no confundir "sin dato" con "valor nulo real".
 */
@Schema(name = "IndicatorResponse",
        description = "Indicadores EVM de una actividad. Los campos calculados mediante una division "
                + "se devuelven como null cuando el divisor es cero (indicador no calculable).")
public record IndicatorResponse(

        @Schema(description = "Identificador de la actividad", example = "1")
        Long activityId,

        @Schema(description = "Nombre de la actividad", example = "Cimentacion")
        String activityName,

        @Schema(description = "BAC - Presupuesto total planificado (totalPlannedBudget)", example = "10000.0000")
        BigDecimal bac,

        @Schema(description = "PV - Valor planificado = porcentPlanned x totalPlannedBudget", example = "6000.0000")
        BigDecimal pv,

        @Schema(description = "EV - Valor ganado = porcentComplete x totalPlannedBudget", example = "4500.0000")
        BigDecimal ev,

        @Schema(description = "CV - Variacion de costo = EV - AC", example = "-700.0000")
        BigDecimal cv,

        @Schema(description = "SV - Variacion de cronograma = EV - PV", example = "-1500.0000")
        BigDecimal sv,

        @Schema(description = "CPI - Indice de desempeno del costo = EV / AC. null si AC es cero", example = "0.8654")
        BigDecimal cpi,

        @Schema(description = "SPI - Indice de desempeno del cronograma = EV / PV. null si PV es cero", example = "0.7500")
        BigDecimal spi,

        @Schema(description = "EAC - Estimacion al completar = BAC / CPI. null si CPI es null o cero", example = "11555.3500")
        BigDecimal eac,

        @Schema(description = "VAC - Variacion al completar = BAC - EAC. null si EAC es null", example = "-1555.3500")
        BigDecimal vac) {
}
