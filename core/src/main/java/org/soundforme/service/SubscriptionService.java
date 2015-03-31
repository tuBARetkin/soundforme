package org.soundforme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.external.DiscogsConnectionException;
import org.soundforme.external.DiscogsStore;
import org.soundforme.external.ReleaseCollector;
import org.soundforme.model.Subscription;
import org.soundforme.model.SubscriptionType;
import org.soundforme.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author NGorelov
 */
@Service
public class SubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Inject
    private ReleaseCollector releaseCollector;
    @Inject
    private DiscogsStore discogsStore;
    @Inject
    private SubscriptionRepository subscriptionRepository;

    public Subscription follow(String stringId) {
        checkNotNull(stringId, "Id of discogs resource should be defined");
        checkArgument(stringId.matches("[al][0-9]+"), "Id should match '[al][0-9]+'");

        SubscriptionType type = stringId.charAt(0) == 'a' ? SubscriptionType.ARTIST : SubscriptionType.LABEL;
        int id = Integer.parseInt(stringId.substring(1).trim());

        Subscription result = subscriptionRepository.findByDiscogsIdAndType(id, type);
        if(result == null) {
            String title = type == SubscriptionType.ARTIST ? discogsStore.getArtistNameById(id) : discogsStore.getLabelTitleById(id);
            if(isNotBlank(title)) {
                result = new Subscription();
                result.setDiscogsId(id);
                result.setType(type);
                result.setClosed(false);
                result.setTitle(title);
                result = subscriptionRepository.save(result);
                logger.info("New subscription {}/{} saved to db", id, type);
            } else {
                throw new DiscogsConnectionException("Resource " + stringId + " not found");
            }
        } else if (result.getClosed()){
            result.setClosed(false);
            result = subscriptionRepository.save(result);
            logger.info("Changed 'closed' status of subscription {}/{}", id, type);
        }

        return result;
    }

    public void unsubscribe(Subscription subscription) {
        checkNotNull(subscription, "Subscription object should be defined");
        checkArgument(isNotBlank(subscription.getId()), "Subscription id should not be empty or null");

        Subscription deletedItem = subscriptionRepository.findOne(subscription.getId());
        deletedItem.setClosed(true);
        subscriptionRepository.save(deletedItem);
    }

    public void refresh(){

    }

    public List<Subscription> findAll(){
        return subscriptionRepository.findByClosed(false);
    }
}
