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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * FactoryBean that sets up a Quartz Scheduler and exposes it for
 * bean references.
 *
 * <p>Allows registration of JobDetails, Calendars and Triggers,
 * automatically starting the scheduler on initialization and
 * shutting it down on destruction. In typical scenarios, there is
 * no need to access the Scheduler instance itself in application code.
 *
 * <p>Note that Quartz instantiates a new Job for each execution, in
 * contrast to Timer which uses a TimerTask instance that is shared
 * between repeated executions. Just JobDetail descriptors are shared.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see org.quartz.Scheduler
 * @see org.quartz.impl.StdSchedulerFactory
 * @version $Id: SchedulerFactoryBean.java,v 1.9 2004-05-18 07:35:22 jhoeller Exp $
 */
public class SchedulerFactoryBean implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Class schedulerFactoryClass = StdSchedulerFactory.class;

	private String schedulerName;

	private Resource configLocation;

	private Properties quartzProperties;

	private Map schedulerContextMap;

	private ApplicationContext applicationContext;

	private String applicationContextSchedulerContextKey;

	private List jobDetails;

	private Map calendars;

	private List triggers;

	private Scheduler scheduler;


	/**
	 * Set the Quartz SchedulerFactory implementation to use.
	 * <p>Default is StdSchedulerFactory, reading in the standard
	 * quartz.properties from quartz.jar. To use custom Quartz
	 * properties, specify "configLocation" or "quartzProperties".
	 * @see org.quartz.impl.StdSchedulerFactory
	 * @see #setConfigLocation
	 * @see #setQuartzProperties
	 */
	public void setSchedulerFactoryClass(Class schedulerFactoryClass) {
		this.schedulerFactoryClass = schedulerFactoryClass;
	}

	/**
	 * Set the name of the Scheduler to fetch from the SchedulerFactory.
	 * If not specified, the default Scheduler will be used.
	 * @see org.quartz.SchedulerFactory#getScheduler(String)
	 * @see org.quartz.SchedulerFactory#getScheduler
	 */
	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	/**
	 * Set the location of the Quartz properties config file, for example
	 * as classpath resource "classpath:quartz.properties".
	 * <p>Note: Can be omitted when all necessary properties are
	 * specified locally via this bean.
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set Quartz properties, like "org.quartz.threadPool.class".
	 * <p>Can be used to override values in a Quartz properties config file,
	 * or to specify all necessary properties locally.
	 */
	public void setQuartzProperties(Properties quartzProperties) {
		this.quartzProperties = quartzProperties;
	}

	/**
	 * Register objects in the Scheduler context via a given Map.
	 * These objects will be available to any Job that runs in this Scheduler.
	 * <p>Note: When using persistent Jobs whose JobDetail will be kept in the
	 * database, do not put Spring-managed beans or an ApplicationContext
	 * reference into the JobDataMap but rather into the SchedulerContext.
	 * @param schedulerContextAsMap Map with String keys and any objects as values
	 * (for example Spring-managed beans)
	 * @see JobDetailBean#setJobDataAsMap
	 */
	public void setSchedulerContextAsMap(Map schedulerContextAsMap) {
		this.schedulerContextMap = schedulerContextAsMap;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Set the key of an ApplicationContext reference to expose in the
	 * SchedulerContext, for example "applicationContext". Default is none.
	 * Only applicable when running in a Spring ApplicationContext.
	 * <p>Note: When using persistent Jobs whose JobDetail will be kept in the
	 * database, do not put an ApplicationContext reference into the JobDataMap
	 * but rather into the SchedulerContext.
	 * <p>In case of a QuartzJobBean, the reference will be applied to the Job
	 * instance as bean property. An "applicationContext" attribute will correspond
	 * to a "setApplicationContext" method in that scenario.
	 * <p>Note that BeanFactory callback interfaces like ApplicationContextAware
	 * are not automatically applied to Quartz Job instances, because Quartz
	 * itself is reponsible for the lifecycle of its Jobs.
	 * @see JobDetailBean#setApplicationContextJobDataKey
	 * @see org.springframework.context.ApplicationContext
	 */
	public void setApplicationContextSchedulerContextKey(String applicationContextSchedulerContextKey) {
		this.applicationContextSchedulerContextKey = applicationContextSchedulerContextKey;
	}

	/**
	 * Register a list of JobDetail objects with the Scheduler that this
	 * FactoryBean creates, to be referenced by Triggers.
	 * <p>This is not necessary when a Trigger determines the JobDetail
	 * itself: In this case, the JobDetail will be implicitly registered
	 * in combination with the Trigger.
	 * @see #setTriggers
	 * @see org.quartz.JobDetail
	 * @see JobDetailBean
	 * @see JobDetailAwareTrigger
	 * @see org.quartz.Trigger#setJobName
	 */
	public void setJobDetails(JobDetail[] jobDetails) {
		this.jobDetails = Arrays.asList(jobDetails);
	}

	/**
	 * Register a list of Quartz Calendar objects with the Scheduler that
	 * this FactoryBean creates, to be referenced by Triggers.
	 * @param calendars Map with calendar names as keys as Calendar
	 * objects as values
	 * @see org.quartz.Calendar
	 * @see org.quartz.Trigger#setCalendarName
	 */
	public void setCalendars(Map calendars) {
		this.calendars = calendars;
	}

	/**
	 * Register a list of Trigger objects with the Scheduler that this
	 * FactoryBean creates.
	 * <p>If the Trigger determines the corresponding JobDetail itself,
	 * the job will be automatically registered with the Scheduler.
	 * Else, the respective JobDetail needs to be registered via the
	 * "jobDetails" property of this FactoryBean.
	 * @see #setJobDetails
	 * @see org.quartz.JobDetail
	 * @see JobDetailAwareTrigger
	 * @see CronTriggerBean
	 * @see SimpleTriggerBean
	 */
	public void setTriggers(Trigger[] triggers) {
		this.triggers = Arrays.asList(triggers);
	}


	public void afterPropertiesSet() throws IOException, SchedulerException {
		// create SchedulerFactory
		SchedulerFactory schedulerFactory = (SchedulerFactory)
		    BeanUtils.instantiateClass(this.schedulerFactoryClass);

		// load and/or apply Quartz properties
		if (this.configLocation != null || this.quartzProperties != null) {
			if (!(schedulerFactory instanceof StdSchedulerFactory)) {
				throw new IllegalArgumentException("StdSchedulerFactory required for applying Quartz properties");
			}

			Properties props = new Properties();

			if (this.configLocation != null) {
				// load Quarz properties from given location
				InputStream is = this.configLocation.getInputStream();
				try {
					props.load(is);
				}
				finally {
					is.close();
				}
			}

			if (this.quartzProperties != null) {
				// add given Quartz properties
				props.putAll(this.quartzProperties);
			}

			((StdSchedulerFactory) schedulerFactory).initialize(props);
		}

		// get Scheduler instance from SchedulerFactory
		this.scheduler = createScheduler(schedulerFactory, this.schedulerName);

		// put specified objects into Scheduler context
		if (this.schedulerContextMap != null) {
			this.scheduler.getContext().putAll(this.schedulerContextMap);
		}

		// register ApplicationContext in Scheduler context
		if (this.applicationContextSchedulerContextKey != null) {
			if (this.applicationContext == null) {
				throw new IllegalStateException("SchedulerFactoryBean needs to be set up in an ApplicationContext " +
																				"to be able to handle an 'applicationContextSchedulerContextKey'");
			}
			this.scheduler.getContext().put(this.applicationContextSchedulerContextKey, this.applicationContext);
		}

		// register JobDetails
		if (this.jobDetails != null) {
			for (Iterator it = this.jobDetails.iterator(); it.hasNext();) {
				JobDetail jobDetail = (JobDetail) it.next();
				this.scheduler.addJob(jobDetail, true);
			}
		}
		else {
			// create empty list for easier checks when registering triggers
			this.jobDetails = new ArrayList();
		}

		// register Calendars
		if (this.calendars != null) {
			for (Iterator it = this.calendars.keySet().iterator(); it.hasNext();) {
				String calendarName = (String) it.next();
				Calendar calendar = (Calendar) this.calendars.get(calendarName);
				this.scheduler.addCalendar(calendarName, calendar, true);
			}
		}

		// register Triggers
		if (this.triggers != null) {
			for (Iterator it = this.triggers.iterator(); it.hasNext();) {
				Trigger trigger = (Trigger) it.next();
				// check if the Trigger is aware of an associated JobDetail
				if (trigger instanceof JobDetailAwareTrigger) {
					JobDetail jobDetail = ((JobDetailAwareTrigger) trigger).getJobDetail();
					if (!this.jobDetails.contains(jobDetail)) {
						// automatically register the JobDetail too
						this.jobDetails.add(jobDetail);
						this.scheduler.addJob(jobDetail, true);
					}
				}
				this.scheduler.scheduleJob(trigger);
			}
		}

		logger.info("Starting Quartz Scheduler");
		this.scheduler.start();
	}

	/**
	 * Create the Scheduler instance for the given factory and scheduler name.
	 * Called by afterPropertiesSet.
	 * <p>Default implementation invokes the corresponding getScheduler methods
	 * of SchedulerFactory. Can be overridden for custom Scheduler creation.
	 * @param schedulerFactory the factory to create the Scheduler with
	 * @param schedulerName the name of the scheduler to create
	 * @return the Scheduler instance
	 * @throws SchedulerException if thrown by Quartz methods
	 * @see #afterPropertiesSet
	 * @see org.quartz.SchedulerFactory#getScheduler
	 * @see org.quartz.SchedulerFactory#getScheduler(String)
	 */
	protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName)
			throws SchedulerException {
		if (schedulerName != null) {
			return schedulerFactory.getScheduler(schedulerName);
		}
		else {
			return schedulerFactory.getScheduler();
		}
	}


	public Object getObject() {
		return this.scheduler;
	}

	public Class getObjectType() {
		return (this.scheduler != null) ? this.scheduler.getClass() : Scheduler.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * This implementation shuts down the Quartz scheduler,
	 * stopping all scheduled jobs.
	 */
	public void destroy() throws SchedulerException {
		logger.info("Shutting down Quartz Scheduler");
		this.scheduler.shutdown();
	}

}
