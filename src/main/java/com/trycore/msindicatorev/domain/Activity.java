package com.trycore.msindicatorev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Actividad de un proyecto sobre la que se calculan los indicadores de Earned Value Management.
 *
 * <p>Los porcentajes se expresan como fraccion en el rango {@code [0.0, 1.0]} (0.75 = 75%), de modo
 * que las formulas PV y EV se aplican directamente contra {@code totalPlannedBudget} sin conversion.
 */
@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** BAC: presupuesto total planificado de la actividad. */
    @NotNull(message = "totalPlannedBudget es obligatorio")
    @DecimalMin(value = "0.0", message = "totalPlannedBudget no puede ser negativo")
    @Column(name = "total_planned_budget", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPlannedBudget;

    /** Porcentaje planificado de avance, como fraccion 0.0 - 1.0. */
    @NotNull(message = "porcentPlanned es obligatorio")
    @DecimalMin(value = "0.0", message = "porcentPlanned debe estar entre 0.0 y 1.0")
    @DecimalMax(value = "1.0", message = "porcentPlanned debe estar entre 0.0 y 1.0")
    @Column(name = "porcent_planned", nullable = false, precision = 9, scale = 4)
    private BigDecimal porcentPlanned;

    /** Porcentaje real completado, como fraccion 0.0 - 1.0. */
    @NotNull(message = "porcentComplete es obligatorio")
    @DecimalMin(value = "0.0", message = "porcentComplete debe estar entre 0.0 y 1.0")
    @DecimalMax(value = "1.0", message = "porcentComplete debe estar entre 0.0 y 1.0")
    @Column(name = "porcent_complete", nullable = false, precision = 9, scale = 4)
    private BigDecimal porcentComplete;

    /** AC: costo real incurrido. */
    @NotNull(message = "actualCost es obligatorio")
    @DecimalMin(value = "0.0", message = "actualCost no puede ser negativo")
    @Column(name = "actual_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal actualCost;

    /** Usuario que dio de alta el registro. */
    @Column(name = "created", length = 100)
    private String created;

    /** Fecha de alta del registro. */
    @Column(name = "create_date")
    private LocalDateTime createDate;

    /** Usuario que realizo la ultima modificacion. */
    @Column(name = "create_update", length = 100)
    private String createUpdate;

    /** Fecha de la ultima modificacion. */
    @Column(name = "create_date_update")
    private LocalDateTime createDateUpdate;

    protected Activity() {
        // requerido por JPA
    }

    public Activity(String name, BigDecimal totalPlannedBudget, BigDecimal porcentPlanned,
                    BigDecimal porcentComplete, BigDecimal actualCost) {
        this.name = name;
        this.totalPlannedBudget = totalPlannedBudget;
        this.porcentPlanned = porcentPlanned;
        this.porcentComplete = porcentComplete;
        this.actualCost = actualCost;
    }

    @PrePersist
    void onCreate() {
        this.createDate = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.createDateUpdate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTotalPlannedBudget() {
        return totalPlannedBudget;
    }

    public void setTotalPlannedBudget(BigDecimal totalPlannedBudget) {
        this.totalPlannedBudget = totalPlannedBudget;
    }

    public BigDecimal getPorcentPlanned() {
        return porcentPlanned;
    }

    public void setPorcentPlanned(BigDecimal porcentPlanned) {
        this.porcentPlanned = porcentPlanned;
    }

    public BigDecimal getPorcentComplete() {
        return porcentComplete;
    }

    public void setPorcentComplete(BigDecimal porcentComplete) {
        this.porcentComplete = porcentComplete;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getCreateUpdate() {
        return createUpdate;
    }

    public void setCreateUpdate(String createUpdate) {
        this.createUpdate = createUpdate;
    }

    public LocalDateTime getCreateDateUpdate() {
        return createDateUpdate;
    }

    public void setCreateDateUpdate(LocalDateTime createDateUpdate) {
        this.createDateUpdate = createDateUpdate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Activity activity)) {
            return false;
        }
        return id != null && id.equals(activity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Activity{id=" + id + ", name='" + name + "'}";
    }
}
