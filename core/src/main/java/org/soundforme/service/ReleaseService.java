package org.soundforme.service;

import org.soundforme.model.Release;
import org.soundforme.repositories.ReleaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author NGorelov
 */
@Service
public class ReleaseService {
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

    public void setChecked(String id, boolean checked) {
        setFlagAndSave(id, (Release existed) -> existed.setChecked(checked));
    }

    public void setStarred(String id, boolean starred) {
        setFlagAndSave(id, (Release existed) -> existed.setStarred(starred));
    }

    private void setFlagAndSave(String id, Consumer<Release> flagSetter){
        checkArgument(isNotBlank(id), "Release id should not be blank");

        Release existedRelease = releaseRepository.findOne(id);
        if(existedRelease != null){
            flagSetter.accept(existedRelease);
            releaseRepository.save(existedRelease);
        }
    }
}
