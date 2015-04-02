package org.soundforme.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.repositories.ReleaseRepository;
import org.soundforme.repositories.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author NGorelov
 */
@Service
public class ReleaseService {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseService.class);

    @Inject
    private ReleaseRepository releaseRepository;

    public Page<Release> loadPage(Pageable pageable) {
        checkNotNull(pageable, "Pageable object should be defined");
        return releaseRepository.findAll(pageable);
    }

    public Page<Release> loadStarredPage(Pageable pageable) {
        checkNotNull(pageable, "Pageable object should be defined");
        return releaseRepository.findByStarred(true, pageable);
    }

    public Release findOne(String id) {
        checkNotNull(id, "Id string should be defined");
        checkArgument(isNotBlank(id), "Id string should not be blank");

        return releaseRepository.findOne(id);
    }

    public void markChecked(Release release, boolean checked) {
        setFlagAndSave(release, (Release existed) -> existed.setChecked(checked));
    }

    public void markStarred(Release release, boolean starred) {
        setFlagAndSave(release, (Release existed) -> existed.setStarred(starred));
    }

    private void setFlagAndSave(Release release, Consumer<Release> flagSetter){
        checkNotNull(release, "Release object should be defined");
        checkArgument(isNotBlank(release.getId()), "Release id should not be blank");

        Release existedRelease = releaseRepository.findOne(release.getId());
        if(existedRelease != null){
            flagSetter.accept(existedRelease);
            releaseRepository.save(existedRelease);
        }
    }
}
