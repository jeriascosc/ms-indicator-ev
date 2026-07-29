package com.trycore.msindicatorev.web.hateoas;

import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.web.ActivityController;
import com.trycore.msindicatorev.web.IndicatorController;
import com.trycore.msindicatorev.web.dto.ActivityResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Enriquece cada actividad con los enlaces hipermedia que la hacen navegable, elevando la API al
 * nivel 3 del Modelo de Madurez de Richardson.
 */
@Component
public class ActivityModelAssembler
        implements RepresentationModelAssembler<Activity, EntityModel<ActivityResponse>> {

    @Override
    public EntityModel<ActivityResponse> toModel(Activity activity) {
        Long id = activity.getId();
        return EntityModel.of(ActivityResponse.from(activity),
                linkTo(methodOn(ActivityController.class).findById(id)).withSelfRel(),
                linkTo(methodOn(IndicatorController.class).indicatorsByActivity(id)).withRel("indicators"),
                linkTo(methodOn(IndicatorController.class).interpretationByActivity(id)).withRel("interpretation"),
                linkTo(methodOn(ActivityController.class).findAll()).withRel("activities"));
    }

    @Override
    public CollectionModel<EntityModel<ActivityResponse>> toCollectionModel(Iterable<? extends Activity> activities) {
        List<EntityModel<ActivityResponse>> models = new java.util.ArrayList<>();
        activities.forEach(activity -> models.add(toModel(activity)));

        return CollectionModel.of(models,
                linkTo(methodOn(ActivityController.class).findAll()).withSelfRel(),
                linkTo(methodOn(IndicatorController.class).indicatorsForAll()).withRel("indicators"),
                linkTo(methodOn(IndicatorController.class).interpretationsForAll()).withRel("interpretations"));
    }
}
