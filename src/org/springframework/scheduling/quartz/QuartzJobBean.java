package org.springframework.scheduling.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;

/**
 * Simple implementation of the Quartz Job interface, applying the
 * passed-in job data map as bean property values. This is appropriate
 * because a new Job instance will be created for each execution.
 *
 * <p>For example, let's assume that the job data map contains a key
 * "myParam" with value "5": The Job implementation can then expose
 * a bean property "myParam" of type int to receive such a value,
 * i.e. a method "setMyParam(int)". This will also work for complex
 * types like business objects etc.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see org.quartz.JobDetail#getJobDataMap
 */
public abstract class QuartzJobBean implements Job {

	/**
	 * This implementation applies the passed-in job data map as bean
	 * property values, and delegates to executeInternal afterwards.
	 * @see #executeInternal
	 */
	public final void execute(JobExecutionContext context) throws JobExecutionException {
		BeanWrapper bw = new BeanWrapperImpl(this);
		bw.setPropertyValues(new MutablePropertyValues(context.getJobDetail().getJobDataMap()), true);
		executeInternal(context);
	}

	/**
	 * Execute the actual job. The job data map will already have been
	 * applied as bean property values by execute. The contract is
	 * exactly the same as for the standard Quartz execute method.
	 * @see #execute
	 */
	protected abstract void executeInternal(JobExecutionContext context) throws JobExecutionException;

}
