package com.trycore.msindicatorev.service;

import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.repository.ActivityRepository;
import com.trycore.msindicatorev.web.dto.ActivityRequest;
import com.trycore.msindicatorev.web.error.ActivityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository repository;

    @InjectMocks
    private ActivityService service;

    private static ActivityRequest request() {
        return new ActivityRequest("Cimentacion", new BigDecimal("10000.00"),
                new BigDecimal("0.60"), new BigDecimal("0.45"), new BigDecimal("5200.00"));
    }

    private static Activity storedActivity() {
        Activity activity = new Activity("Cimentacion", new BigDecimal("10000.00"),
                new BigDecimal("0.60"), new BigDecimal("0.45"), new BigDecimal("5200.00"));
        activity.setId(1L);
        return activity;
    }

    @Test
    @DisplayName("findAll delega en el repositorio")
    void findsAllActivities() {
        given(repository.findAll()).willReturn(List.of(storedActivity()));

        assertThat(service.findAll()).hasSize(1);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findById devuelve la actividad existente")
    void findsActivityById() {
        given(repository.findById(1L)).willReturn(Optional.of(storedActivity()));

        Activity result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Cimentacion");
    }

    @Test
    @DisplayName("findById lanza ActivityNotFoundException si el id no existe")
    void failsWhenActivityDoesNotExist() {
        given(repository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ActivityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create persiste la actividad y sella el usuario recibido en la cabecera")
    void createsActivityWithProvidedUser() {
        given(repository.save(any(Activity.class))).willAnswer(invocation -> invocation.getArgument(0));

        Activity created = service.create(request(), "jeriasco");

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Cimentacion");
        assertThat(captor.getValue().getTotalPlannedBudget()).isEqualByComparingTo("10000.00");
        assertThat(captor.getValue().getPorcentPlanned()).isEqualByComparingTo("0.60");
        assertThat(captor.getValue().getPorcentComplete()).isEqualByComparingTo("0.45");
        assertThat(captor.getValue().getActualCost()).isEqualByComparingTo("5200.00");
        assertThat(created.getCreated()).isEqualTo("jeriasco");
        assertThat(created.getCreateUpdate()).isNull();
    }

    @Test
    @DisplayName("create aplica el usuario por defecto cuando no llega la cabecera X-User")
    void createsActivityWithDefaultUser() {
        given(repository.save(any(Activity.class))).willAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.create(request(), null).getCreated()).isEqualTo(ActivityService.DEFAULT_USER);
        assertThat(service.create(request(), "   ").getCreated()).isEqualTo(ActivityService.DEFAULT_USER);
    }

    @Test
    @DisplayName("update reemplaza los datos y registra el usuario que modifica")
    void updatesActivity() {
        given(repository.findById(1L)).willReturn(Optional.of(storedActivity()));
        given(repository.save(any(Activity.class))).willAnswer(invocation -> invocation.getArgument(0));

        ActivityRequest changes = new ActivityRequest("Cimentacion reforzada", new BigDecimal("12000.00"),
                new BigDecimal("0.70"), new BigDecimal("0.55"), new BigDecimal("6100.00"));

        Activity updated = service.update(1L, changes, "jeriasco");

        assertThat(updated.getName()).isEqualTo("Cimentacion reforzada");
        assertThat(updated.getTotalPlannedBudget()).isEqualByComparingTo("12000.00");
        assertThat(updated.getPorcentPlanned()).isEqualByComparingTo("0.70");
        assertThat(updated.getPorcentComplete()).isEqualByComparingTo("0.55");
        assertThat(updated.getActualCost()).isEqualByComparingTo("6100.00");
        assertThat(updated.getCreateUpdate()).isEqualTo("jeriasco");
    }

    @Test
    @DisplayName("update sobre un id inexistente no llega a guardar")
    void doesNotUpdateMissingActivity() {
        given(repository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request(), "jeriasco"))
                .isInstanceOf(ActivityNotFoundException.class);

        verify(repository, never()).save(any(Activity.class));
    }

    @Test
    @DisplayName("delete elimina la actividad cuando existe")
    void deletesActivity() {
        given(repository.existsById(1L)).willReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete sobre un id inexistente no llega a borrar")
    void doesNotDeleteMissingActivity() {
        given(repository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ActivityNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("La excepcion conserva el id consultado para el ProblemDetail")
    void exceptionKeepsRequestedId() {
        ActivityNotFoundException exception = new ActivityNotFoundException(42L);

        assertThat(exception.getActivityId()).isEqualTo(42L);
        assertThat(exception).hasMessageContaining("42");
    }
}
