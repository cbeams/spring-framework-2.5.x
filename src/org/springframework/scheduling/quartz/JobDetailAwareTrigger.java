package org.springframework.scheduling.quartz;

import org.quartz.JobDetail;

/**
 * Interface to be implemented by Quartz Triggers that are aware
 * of the JobDetail object that they are associated with.
 *
 * <p>SchedulerFactoryBean will auto-detect Triggers that implement this
 * interface and register them for the respective JobDetail accordingly.
 *
 * <p>The alternative is to configure a Trigger for a Job name and group:
 * This involves the need to register the JobDetail object separately
 * with SchedulerFactoryBean.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see SchedulerFactoryBean#setTriggers
 * @see SchedulerFactoryBean#setJobDetails
 * @see org.quartz.Trigger#setJobName
 * @see org.quartz.Trigger#setJobGroup
 */
public interface JobDetailAwareTrigger {

	/**
	 * Return the JobDetail that this Trigger is associated with.
	 * @return the associated JobDetail, or null if none
	 */
	JobDetail getJobDetail();

}
