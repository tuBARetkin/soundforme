package org.soundforme.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

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
