package org.soundforme.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.soundforme.collector.model.ReleasesPage;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author NGorelov
 */
@Configuration
@Import(value = MongoConfig.class)
@ComponentScan(basePackages = "org.soundforme")
@PropertySource("classpath:app.properties")
public class SharedConfig {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Gson gson(){
        return new GsonBuilder().create();
    }
}
