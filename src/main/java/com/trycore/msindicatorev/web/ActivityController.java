package com.trycore.msindicatorev.web;

import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.service.ActivityService;
import com.trycore.msindicatorev.web.dto.ActivityRequest;
import com.trycore.msindicatorev.web.dto.ActivityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * CRUD de actividades expuesto como recurso REST: URIs orientadas a recursos, verbos HTTP con
 * semantica correcta y codigos de estado significativos.
 */
@RestController
@RequestMapping(path = "/api/v1/activities", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Activities", description = "Gestion de las actividades del proyecto")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    @Operation(summary = "Listar actividades",
            description = "Devuelve todas las actividades registradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    public ResponseEntity<List<ActivityResponse>> findAll() {
        List<ActivityResponse> activities = activityService.findAll().stream()
                .map(ActivityResponse::from)
                .toList();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una actividad por id",
            description = "Recupera una actividad concreta a partir de su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una actividad con ese id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ActivityResponse> findById(
            @Parameter(description = "Identificador de la actividad", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(ActivityResponse.from(activityService.findById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear una actividad",
            description = "Registra una nueva actividad. Los porcentajes se envian como fraccion "
                    + "entre 0.0 y 1.0. La cabecera opcional X-User queda registrada en el campo de "
                    + "auditoria 'created'; si se omite se usa 'system'.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Actividad creada. La cabecera Location "
                    + "apunta al recurso recien creado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o cuerpo ilegible",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ActivityResponse> create(
            @Valid @RequestBody ActivityRequest request,
            @Parameter(description = "Usuario que ejecuta la operacion", example = "jeriasco")
            @RequestHeader(value = "X-User", required = false) String user) {

        Activity created = activityService.create(request, user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(ActivityResponse.from(created));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualizar una actividad",
            description = "Reemplaza los datos de una actividad existente. La cabecera opcional "
                    + "X-User queda registrada en el campo de auditoria 'createUpdate'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o cuerpo ilegible",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No existe una actividad con ese id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ActivityResponse> update(
            @Parameter(description = "Identificador de la actividad", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest request,
            @Parameter(description = "Usuario que ejecuta la operacion", example = "jeriasco")
            @RequestHeader(value = "X-User", required = false) String user) {

        return ResponseEntity.ok(ActivityResponse.from(activityService.update(id, request, user)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una actividad",
            description = "Borra definitivamente una actividad del sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actividad eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe una actividad con ese id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador de la actividad", example = "1")
            @PathVariable Long id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
