package com.trycore.msindicatorev.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos de entrada para crear o actualizar una actividad.
 */
@Schema(name = "ActivityRequest", description = "Datos de entrada de una actividad del proyecto")
public record ActivityRequest(

        @Schema(description = "Nombre de la actividad", example = "Cimentacion", maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre de la actividad es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @Schema(description = "BAC - Presupuesto total planificado de la actividad", example = "10000.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "totalPlannedBudget es obligatorio")
        @DecimalMin(value = "0.0", message = "totalPlannedBudget no puede ser negativo")
        BigDecimal totalPlannedBudget,

        @Schema(description = "Porcentaje planificado de avance, expresado como fraccion entre 0.0 y 1.0 (0.60 = 60%)",
                example = "0.60", minimum = "0.0", maximum = "1.0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "porcentPlanned es obligatorio")
        @DecimalMin(value = "0.0", message = "porcentPlanned debe estar entre 0.0 y 1.0")
        @DecimalMax(value = "1.0", message = "porcentPlanned debe estar entre 0.0 y 1.0")
        BigDecimal porcentPlanned,

        @Schema(description = "Porcentaje real completado, expresado como fraccion entre 0.0 y 1.0 (0.45 = 45%)",
                example = "0.45", minimum = "0.0", maximum = "1.0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "porcentComplete es obligatorio")
        @DecimalMin(value = "0.0", message = "porcentComplete debe estar entre 0.0 y 1.0")
        @DecimalMax(value = "1.0", message = "porcentComplete debe estar entre 0.0 y 1.0")
        BigDecimal porcentComplete,

        @Schema(description = "AC - Costo real incurrido en la actividad", example = "5200.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "actualCost es obligatorio")
        @DecimalMin(value = "0.0", message = "actualCost no puede ser negativo")
        BigDecimal actualCost) {
}
