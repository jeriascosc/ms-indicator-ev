package com.trycore.msindicatorev.repository;

import com.trycore.msindicatorev.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA de actividades sobre la base de datos H2 en memoria.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
