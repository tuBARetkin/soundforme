package org.soundforme.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.inject.Inject;

/**
 * @author NGorelov
 */
@Configurable(autowire = Autowire.BY_TYPE)
public class RefreshJob extends QuartzJobBean {
    @Inject
    private SubscriptionService subscriptionService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        subscriptionService.refresh();
    }
}
