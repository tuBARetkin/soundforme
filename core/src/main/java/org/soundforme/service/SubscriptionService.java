package org.soundforme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Subscription;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author NGorelov
 */
@Service
public class SubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    public Subscription follow(String url) {
        throw new UnsupportedOperationException();
    }

    public void unsubscribe(Subscription subscription){
        throw new UnsupportedOperationException();
    }

    public List<Subscription> findAll() {
        throw new UnsupportedOperationException();
    }
}
