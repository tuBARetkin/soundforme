package org.soundforme.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.quartz.JobDetail;
import org.soundforme.scheduler.RefreshJob;
import org.soundforme.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import javax.inject.Inject;
import java.util.Collections;

/**
 * @author NGorelov
 */
@Configuration
@Import(value = {MongoConfig.class, MongoTestConfig.class})
@ComponentScan(basePackages = "org.soundforme")
@PropertySource("classpath:app.properties")
public class SharedConfig {
    @Autowired
    @Qualifier("subscriptionService")
    private SubscriptionService subscriptionService;

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Gson gson(){
        return new GsonBuilder().create();
    }

    @Bean
    @DependsOn("subscriptionService")
    public JobDetailFactoryBean refreshJob() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setDurability(true);
        factoryBean.setJobClass(RefreshJob.class);
        return factoryBean;
    }

    @Bean
    @DependsOn("refreshJob")
    public SimpleTriggerFactoryBean refreshTrigger() {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail((JobDetail) refreshJob());
        factoryBean.setRepeatInterval(100000);
        return factoryBean;
    }

    @Bean
    @DependsOn("refreshTrigger")
    public SchedulerFactoryBean refreshScheduler() {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        factoryBean.setTriggers(refreshTrigger().getObject());
        return factoryBean;
    }


    /*
    <property name="jobClass" value="com.fls.gen4.control.ControlJob" />
        <property name="durability" value="true" />
        <property name="jobDataAsMap">
            <map>
                <entry key="clientNodeRepository" value-ref="clientNodeRepository"/>
                <entry key="generationHolder" value-ref="generationHolder"/>
                <entry key="restTemplate" value-ref="restTemplate"/>
                <entry key="taxpayerRepository" value-ref="taxpayerRepository"/>
                <entry key="taxpayersBuffer" value-ref="taxpayersBuffer"/>
                <entry key="clientHistoryRepository" value-ref="clientHistoryRepository"/>
                <entry key="packsWithUpdatesHolder" value-ref="packsWithUpdatesHolder"/>
            </map>
        </property>
     */
}
