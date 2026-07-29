package com.trycore.msindicatorev.service;

import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.repository.ActivityRepository;
import com.trycore.msindicatorev.web.dto.ActivityRequest;
import com.trycore.msindicatorev.web.error.ActivityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Operaciones CRUD sobre las actividades del proyecto.
 *
 * <p>Los campos de auditoria de usuario ({@code created} y {@code createUpdate}) se sellan aqui a
 * partir de la cabecera {@code X-User}, mientras que las marcas de tiempo las gestiona la propia
 * entidad con {@code @PrePersist} / {@code @PreUpdate}. De este modo los callbacks JPA no dependen
 * del contexto de la peticion.
 */
@Service
@Transactional(readOnly = true)
public class ActivityService {

    /** Usuario aplicado cuando la peticion no informa la cabecera {@code X-User}. */
    public static final String DEFAULT_USER = "system";

    private final ActivityRepository repository;

    public ActivityService(ActivityRepository repository) {
        this.repository = repository;
    }

    public List<Activity> findAll() {
        return repository.findAll();
    }

    public Activity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ActivityNotFoundException(id));
    }

    @Transactional
    public Activity create(ActivityRequest request, String user) {
        Activity activity = new Activity(
                request.name(),
                request.totalPlannedBudget(),
                request.porcentPlanned(),
                request.porcentComplete(),
                request.actualCost());
        activity.setCreated(resolveUser(user));
        return repository.save(activity);
    }

    @Transactional
    public Activity update(Long id, ActivityRequest request, String user) {
        Activity activity = findById(id);
        activity.setName(request.name());
        activity.setTotalPlannedBudget(request.totalPlannedBudget());
        activity.setPorcentPlanned(request.porcentPlanned());
        activity.setPorcentComplete(request.porcentComplete());
        activity.setActualCost(request.actualCost());
        activity.setCreateUpdate(resolveUser(user));
        return repository.save(activity);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ActivityNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private String resolveUser(String user) {
        return StringUtils.hasText(user) ? user.trim() : DEFAULT_USER;
    }
}
