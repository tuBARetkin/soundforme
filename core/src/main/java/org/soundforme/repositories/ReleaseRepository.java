package org.soundforme.repositories;

import org.soundforme.model.Release;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author NGorelov
 */
public interface ReleaseRepository extends MongoRepository<Release, String> {
    Release findByDiscogsId(int discogsId);
    Page<Release> findByStarred(boolean starred, Pageable pageable);
    Page<Release> findAll(Pageable pageable);
}
