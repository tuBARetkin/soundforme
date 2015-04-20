package org.soundforme;

import org.soundforme.config.SharedConfig;
import org.soundforme.model.Subscription;
import org.soundforme.repositories.SubscriptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;

/**
 * @author NGorelov
 */
@SpringBootApplication
@Import(value = SharedConfig.class)
public class App implements CommandLineRunner {

    @Inject
    private SubscriptionRepository subscriptionRepository;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Subscription subscription = new Subscription();
        subscription.setTitle("test");
        subscriptionRepository.save(subscription);
    }
}
