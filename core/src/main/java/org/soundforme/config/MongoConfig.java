package org.soundforme.config;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author NGorelov
 */
@Configuration
@EnableMongoRepositories(basePackages = "org.soundforme.repositories")
@Profile("main")
public class MongoConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "soundforme";
    }

    @Override
    public MongoClient mongo() throws Exception {
        return new MongoClient(new ServerAddress());
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.soundforme.model";
    }
}
