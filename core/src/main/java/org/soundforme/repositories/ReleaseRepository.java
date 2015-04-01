package org.soundforme.repositories;

import org.soundforme.model.Release;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author NGorelov
 */
public interface ReleaseRepository extends MongoRepository<Release, String> {
    Release findByDiscogsId(int discogsId);
}
