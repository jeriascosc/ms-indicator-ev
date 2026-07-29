package com.trycore.msindicatorev.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityTest {

    private static Activity newActivity() {
        return new Activity("Cimentacion", new BigDecimal("10000.00"),
                new BigDecimal("0.60"), new BigDecimal("0.45"), new BigDecimal("5200.00"));
    }

    @Test
    @DisplayName("El callback de alta sella la fecha de creacion y deja intacta la de modificacion")
    void stampsCreationDate() {
        Activity activity = newActivity();
        assertThat(activity.getCreateDate()).isNull();

        activity.onCreate();

        assertThat(activity.getCreateDate()).isNotNull().isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(activity.getCreateDateUpdate()).isNull();
    }

    @Test
    @DisplayName("El callback de modificacion sella solo la fecha de actualizacion")
    void stampsUpdateDate() {
        Activity activity = newActivity();
        activity.onCreate();
        LocalDateTime createdAt = activity.getCreateDate();

        activity.onUpdate();

        assertThat(activity.getCreateDateUpdate()).isNotNull();
        assertThat(activity.getCreateDate()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Los campos de auditoria de usuario se asignan explicitamente")
    void keepsAuditUsers() {
        Activity activity = newActivity();
        activity.setCreated("jeriasco");
        activity.setCreateUpdate("otro-usuario");
        activity.setCreateDate(LocalDateTime.of(2026, 7, 29, 10, 0));
        activity.setCreateDateUpdate(LocalDateTime.of(2026, 7, 29, 12, 0));

        assertThat(activity.getCreated()).isEqualTo("jeriasco");
        assertThat(activity.getCreateUpdate()).isEqualTo("otro-usuario");
        assertThat(activity.getCreateDate()).isEqualTo(LocalDateTime.of(2026, 7, 29, 10, 0));
        assertThat(activity.getCreateDateUpdate()).isEqualTo(LocalDateTime.of(2026, 7, 29, 12, 0));
    }

    @Test
    @DisplayName("Dos actividades son iguales solo si comparten un id persistido")
    void comparesById() {
        Activity first = newActivity();
        Activity second = newActivity();

        // sin id todavia, ninguna entidad transitoria es igual a otra
        assertThat(first).isNotEqualTo(second);
        assertThat(first).isEqualTo(first);

        first.setId(1L);
        second.setId(1L);
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);

        second.setId(2L);
        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo("no es una actividad");
        assertThat(first).isNotEqualTo(null);
    }

    @Test
    @DisplayName("toString identifica la actividad por id y nombre")
    void describesItself() {
        Activity activity = newActivity();
        activity.setId(7L);

        assertThat(activity.toString()).contains("7").contains("Cimentacion");
    }
}
