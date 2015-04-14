package org.soundforme.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.quartz.JobDetail;
import org.soundforme.scheduler.AutowiringSpringBeanJobFactory;
import org.soundforme.scheduler.RefreshJob;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import javax.inject.Inject;

/**
 * @author NGorelov
 */
@Configuration
@Import(value = {MongoConfig.class, MongoTestConfig.class})
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

    @Configuration
    @Profile("main")
    static class QuartzConfig {
        @Inject
        private ApplicationContext applicationContext;

        @Bean
        public JobDetailFactoryBean refreshJob() {
            JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
            factoryBean.setDurability(true);
            factoryBean.setJobClass(RefreshJob.class);
            return factoryBean;
        }

        @Bean
        public SimpleTriggerFactoryBean refreshTrigger() {
            SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
            factoryBean.setJobDetail(refreshJob().getObject());
            factoryBean.setRepeatInterval(100000);
            return factoryBean;
        }

        @Bean
        public SchedulerFactoryBean refreshScheduler() {
            SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();

            AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
            jobFactory.setApplicationContext(applicationContext);

            factoryBean.setJobFactory(jobFactory);
            factoryBean.setTriggers(refreshTrigger().getObject());
            return factoryBean;
        }
    }
}
