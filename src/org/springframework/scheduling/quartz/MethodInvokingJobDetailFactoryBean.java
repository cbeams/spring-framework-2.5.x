/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.scheduling.quartz;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.MethodInvoker;

/**
 * FactoryBean that exposes a JobDetail object that delegates
 * job execution to a specified (static or non-static) method.
 * Avoids the need to implement a one-line Quartz Job that just
 * invokes an existing business method.
 *
 * <p>Derived from MethodInvoker to share common properties and
 * behavior with MethodInvokingFactoryBean.
 * 
 * <p>Supports both concurrently running jobs and non-currently
 * running ones through the "concurrent" property.
 *
 * <p><b>Note: JobDetails created via this FactoryBean are <i>not</i>
 * serializable and thus not suitable for persistent job stores.</b>
 * You need to implement your own Quartz Job as a thin wrapper for
 * each case where you want to delegate to an external method.
 *
 * @author Juergen Hoeller
 * @author Alef Arendsen
 * @since 18.02.2004
 * @see #setConcurrent
 * @see org.springframework.beans.factory.config.MethodInvokingFactoryBean
 */
public class MethodInvokingJobDetailFactoryBean extends ArgumentConvertingMethodInvoker
    implements FactoryBean, BeanNameAware, InitializingBean {

	private String name;

	private String group = Scheduler.DEFAULT_GROUP;

	private boolean concurrent = true;

	private String beanName;

	private JobDetail jobDetail;


	/**
	 * Set the name of the job.
	 * Default is the bean name of this FactoryBean.
	 * @see org.quartz.JobDetail#setName
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the group of the job.
	 * Default is the default group of the Scheduler.
	 * @see org.quartz.JobDetail#setGroup
	 * @see org.quartz.Scheduler#DEFAULT_GROUP
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	
	/**
	 * Specify whether or not multiple jobs should be run in a concurrent
	 * fashion. The behavior when one does not want concurrent jobs to be
	 * executed is realized through adding the {@link StatefulJob} interface.
	 * More information on stateful versus stateless jobs can be found
	 * <a href="http://www.opensymphony.com/quartz/tutorial.html#jobsMore">here</a>.
	 * <p>The default setting is to run jobs concurrently.
	 * @param concurrent whether one wants to execute multiple jobs created
	 * by this bean concurrently
	 */
	public void setConcurrent(boolean concurrent) {
		this.concurrent = concurrent;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}


	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchMethodException {
		prepare();

		// consider the concurrent flag to choose between stateful and stateless job
		this.jobDetail = new JobDetail(this.name != null ? this.name : this.beanName,
		                               this.group,
		                               this.concurrent ? MethodInvokingJob.class : StatefulMethodInvokingJob.class);
		this.jobDetail.getJobDataMap().put("methodInvoker", this);
		this.jobDetail.setVolatility(true);		
	}

	public Object getObject() {
		return this.jobDetail;
	}

	public Class getObjectType() {
		return (this.jobDetail != null) ? this.jobDetail.getClass() : JobDetail.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Quartz Job implementation that invokes a specified method.
	 * Automatically applied by MethodInvokingJobDetailFactoryBean.
	 */
	public static class MethodInvokingJob extends QuartzJobBean {

		protected static final Log logger = LogFactory.getLog(MethodInvokingJob.class);

		private MethodInvoker methodInvoker;

		private String errorMessage;

		/**
		 * Set the MethodInvoker to use.
		 */
		public void setMethodInvoker(MethodInvoker methodInvoker) {
			this.methodInvoker = methodInvoker;
			this.errorMessage = "Could not invoke method '" + this.methodInvoker.getTargetMethod() +
					"' on target object [" + this.methodInvoker.getTargetObject() + "]";
		}

		/**
		 * Invoke the method via the MethodInvoker.
		 */
		protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
			try {
				this.methodInvoker.invoke();
			}
			catch (InvocationTargetException ex) {
				logger.warn(this.errorMessage + ": " + ex.getTargetException().getMessage());
				if (ex.getTargetException() instanceof JobExecutionException) {
					throw (JobExecutionException) ex.getTargetException();
				}
				Exception jobEx = (ex.getTargetException() instanceof Exception) ?
						(Exception) ex.getTargetException() : ex;
				throw new JobExecutionException(this.errorMessage, jobEx, false);
			}
			catch (Exception ex) {
				logger.warn(this.errorMessage + ": " + ex.getMessage());
				throw new JobExecutionException(this.errorMessage, ex, false);
			}
		}
	}


	/**
	 * Extension of the MethodInvokingJob, implementing the StatefulJob interface.
	 * Quartz checks whether or not jobs are stateful and if so,
	 * won't let jobs interfere with each other.
	 */
	public static class StatefulMethodInvokingJob extends MethodInvokingJob implements StatefulJob {
		// No implementation, just a addition of the tag interface StatefulJob
		// in order to allow stateful method invoking jobs.
	}

}
