package com.trycore.msindicatorev.web.error;

/**
 * Se lanza cuando se solicita una actividad que no existe en la base de datos.
 */
public class ActivityNotFoundException extends RuntimeException {

    private final Long activityId;

    public ActivityNotFoundException(Long activityId) {
        super("No existe una actividad con id " + activityId);
        this.activityId = activityId;
    }

    public Long getActivityId() {
        return activityId;
    }
}
