package de.witcom.api.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import de.witcom.api.model.Session;

@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
    Session findByApplicationId(String applicationId);
}