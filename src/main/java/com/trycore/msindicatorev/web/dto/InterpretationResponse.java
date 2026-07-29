package com.trycore.msindicatorev.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Lectura en lenguaje natural del desempeno de una actividad a partir de su CPI y SPI.
 */
@Schema(name = "InterpretationResponse",
        description = "Interpretacion del desempeno de la actividad segun sus indices CPI y SPI")
public record InterpretationResponse(

        @Schema(description = "Identificador de la actividad", example = "1")
        Long activityId,

        @Schema(description = "Nombre de la actividad", example = "Cimentacion")
        String activityName,

        @Schema(description = "CPI - Indice de desempeno del costo", example = "0.8654")
        BigDecimal cpi,

        @Schema(description = "SPI - Indice de desempeno del cronograma", example = "0.7500")
        BigDecimal spi,

        @Schema(description = "Estado del CPI",
                example = "Mal desempeño Financiero",
                allowableValues = {"Gasto conforme a lo planeado", "Eficiencia de Costo",
                        "Mal desempeño Financiero", "No calculable"})
        String cpiStatus,

        @Schema(description = "Estado del SPI",
                example = "Retrazado",
                allowableValues = {"Avanza conforme a lo planeado", "Avanza más de lo previsto",
                        "Retrazado", "No calculable"})
        String spiStatus,

        @Schema(description = "Analisis combinado de CPI frente a SPI. El valor 'Proyecto conforme a lo planeado' "
                + "cubre los casos en que CPI o SPI valen exactamente 1, no contemplados en los cuatro cuadrantes.",
                example = "Proyecto crítico",
                allowableValues = {"Proyecto ideal", "Proyecto crítico", "Proyecto con mayor gasto y retrasado",
                        "Rápido avance a mayor costo", "Proyecto conforme a lo planeado", "No calculable"})
        String cpiVsSpiAnalysis) {
}
