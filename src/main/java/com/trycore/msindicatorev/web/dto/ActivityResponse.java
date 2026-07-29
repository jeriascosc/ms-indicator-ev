package com.trycore.msindicatorev.web.dto;

import com.trycore.msindicatorev.domain.Activity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representacion de salida de una actividad, incluyendo sus campos de auditoria.
 */
@Schema(name = "ActivityResponse", description = "Actividad almacenada con su informacion de auditoria")
public record ActivityResponse(

        @Schema(description = "Identificador unico de la actividad", example = "1")
        Long id,

        @Schema(description = "Nombre de la actividad", example = "Cimentacion")
        String name,

        @Schema(description = "BAC - Presupuesto total planificado", example = "10000.0000")
        BigDecimal totalPlannedBudget,

        @Schema(description = "Porcentaje planificado como fraccion 0.0 - 1.0", example = "0.6000")
        BigDecimal porcentPlanned,

        @Schema(description = "Porcentaje completado como fraccion 0.0 - 1.0", example = "0.4500")
        BigDecimal porcentComplete,

        @Schema(description = "AC - Costo real incurrido", example = "5200.0000")
        BigDecimal actualCost,

        @Schema(description = "Usuario que dio de alta el registro", example = "system")
        String created,

        @Schema(description = "Fecha de alta del registro", example = "2026-07-29T10:15:30")
        LocalDateTime createDate,

        @Schema(description = "Usuario que realizo la ultima modificacion", example = "jeriasco")
        String createUpdate,

        @Schema(description = "Fecha de la ultima modificacion", example = "2026-07-29T11:42:05")
        LocalDateTime createDateUpdate) {

    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getName(),
                activity.getTotalPlannedBudget(),
                activity.getPorcentPlanned(),
                activity.getPorcentComplete(),
                activity.getActualCost(),
                activity.getCreated(),
                activity.getCreateDate(),
                activity.getCreateUpdate(),
                activity.getCreateDateUpdate());
    }
}
