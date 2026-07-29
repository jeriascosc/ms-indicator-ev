package com.trycore.msindicatorev.web;

import com.trycore.msindicatorev.service.ActivityService;
import com.trycore.msindicatorev.service.IndicatorService;
import com.trycore.msindicatorev.web.dto.IndicatorResponse;
import com.trycore.msindicatorev.web.dto.InterpretationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sub-recursos de calculo sobre las actividades: indicadores EVM y su interpretacion de negocio.
 */
@RestController
@RequestMapping(path = "/api/v1/activities", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Indicators", description = "Indicadores de Earned Value Management y su interpretacion")
public class IndicatorController {

    private final ActivityService activityService;
    private final IndicatorService indicatorService;

    public IndicatorController(ActivityService activityService, IndicatorService indicatorService) {
        this.activityService = activityService;
        this.indicatorService = indicatorService;
    }

    @GetMapping("/indicators")
    @Operation(summary = "Indicadores EVM de todas las actividades",
            description = "Calcula PV, EV, CV, SV, CPI, SPI, EAC y VAC para cada registro de la tabla "
                    + "activity. Los indicadores cuyo divisor es cero se devuelven como null.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores calculados para todos los registros")
    })
    public ResponseEntity<List<IndicatorResponse>> indicatorsForAll() {
        return ResponseEntity.ok(indicatorService.calculateAll(activityService.findAll()));
    }

    @GetMapping("/{id}/indicators")
    @Operation(summary = "Indicadores EVM de una actividad",
            description = "Calcula PV, EV, CV, SV, CPI, SPI, EAC y VAC para la actividad indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores calculados"),
            @ApiResponse(responseCode = "404", description = "No existe una actividad con ese id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<IndicatorResponse> indicatorsByActivity(
            @Parameter(description = "Identificador de la actividad", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(indicatorService.calculate(activityService.findById(id)));
    }

    @GetMapping("/interpretations")
    @Operation(summary = "Interpretacion de todas las actividades",
            description = "Devuelve, por cada registro, el estado del CPI, el estado del SPI y el "
                    + "analisis combinado CPI vs SPI.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interpretacion generada para todos los registros")
    })
    public ResponseEntity<List<InterpretationResponse>> interpretationsForAll() {
        return ResponseEntity.ok(indicatorService.interpretAll(activityService.findAll()));
    }

    @GetMapping("/{id}/interpretation")
    @Operation(summary = "Interpretacion de una actividad",
            description = "Traduce el CPI y el SPI de la actividad a su lectura de negocio: estado de "
                    + "costo, estado de cronograma y analisis combinado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interpretacion generada"),
            @ApiResponse(responseCode = "404", description = "No existe una actividad con ese id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<InterpretationResponse> interpretationByActivity(
            @Parameter(description = "Identificador de la actividad", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(indicatorService.interpret(activityService.findById(id)));
    }
}
