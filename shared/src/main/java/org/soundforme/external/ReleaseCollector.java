package org.soundforme.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * @author NGorelov
 */
@Component
public class ReleaseCollector {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseCollector.class);

    public List<Release> collectAll(Subscription subscription){
        checkNotNull(subscription, "subscription should be defined");
        checkArgument(subscription.getType() != null, "type of subscription should be defined");
        checkArgument(subscription.getDiscogsId() != null, "subscription id from discogs should be defined");



        throw new UnsupportedOperationException();
    }

}
