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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
 * <p>Spring's Quartz support was developed against Quartz 1.3.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see org.quartz.Scheduler
 * @see org.quartz.impl.StdSchedulerFactory
 */
public class SchedulerFactoryBean
    implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

	public static final String PROP_THREAD_COUNT = "org.quartz.threadPool.threadCount";

	public static final int DEFAULT_THREAD_COUNT = 10;


	protected final Log logger = LogFactory.getLog(getClass());

	private Class schedulerFactoryClass = StdSchedulerFactory.class;

	private String schedulerName;

	private Resource configLocation;

	private Properties quartzProperties;

	private DataSource dataSource;

	private PlatformTransactionManager transactionManager;

	private Map schedulerContextMap;

	private ApplicationContext applicationContext;

	private String applicationContextSchedulerContextKey;

	private boolean overwriteExistingJobs = false;

	private String[] jobSchedulingDataLocations;

	private List jobDetails;

	private Map calendars;

	private List triggers;

	private boolean autoStartup = true;

	private int startupDelay = 0;

	private boolean waitForJobsToCompleteOnShutdown = false;

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
	 * <p>Note: Can be omitted when all necessary properties are specified
	 * locally via this bean, or when relying on Quartz' default configuration.
	 * @see #setQuartzProperties
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set Quartz properties, like "org.quartz.threadPool.class".
	 * <p>Can be used to override values in a Quartz properties config file,
	 * or to specify all necessary properties locally.
	 * @see #setConfigLocation
	 */
	public void setQuartzProperties(Properties quartzProperties) {
		this.quartzProperties = quartzProperties;
	}

	/**
	 * Set the DataSource to be used by the Scheduler. If set,
	 * this will override corresponding settings in Quartz properties.
	 * <p>Note: If this is set, the Quartz settings should not define
	 * a job store "dataSource" to avoid meaningless double configuration.
	 * @see #setQuartzProperties
	 * @see #setTransactionManager
	 * @see org.springframework.scheduling.quartz.LocalDataSourceJobStore
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set the transaction manager to be used for registering jobs and triggers
	 * that are defined by this SchedulerFactoryBean. Default is none; setting
	 * this only makes sense when specifying a DataSource for the Scheduler.
	 * @see #setDataSource
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Register objects in the Scheduler context via a given Map.
	 * These objects will be available to any Job that runs in this Scheduler.
	 * <p>Note: When using persistent Jobs whose JobDetail will be kept in the
	 * database, do not put Spring-managed beans or an ApplicationContext
	 * reference into the JobDataMap but rather into the SchedulerContext.
	 * @param schedulerContextAsMap Map with String keys and any objects as
	 * values (for example Spring-managed beans)
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
	 * instance as bean property. An "applicationContext" attribute will
	 * correspond to a "setApplicationContext" method in that scenario.
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
	 * Set whether any jobs defined on this SchedulerFactoryBean should overwrite
	 * existing job definitions. Default is "false", to not overwrite already
	 * registered jobs that have been read in from a persistent job store.
	 */
	public void setOverwriteExistingJobs(boolean overwriteExistingJobs) {
		this.overwriteExistingJobs = overwriteExistingJobs;
	}

	/**
	 * Set the location of a Quartz job definition XML file that follows the
	 * "job_scheduling_data_1_0" DTD. Can be specified to automatically
	 * register jobs that are defined in such a file, possibly in addition
	 * to jobs defined directly on this SchedulerFactoryBean.
	 * @see ResourceJobSchedulingDataProcessor
	 * @see org.quartz.xml.JobSchedulingDataProcessor
	 */
	public void setJobSchedulingDataLocation(String jobSchedulingDataLocation) {
		this.jobSchedulingDataLocations = new String[] {jobSchedulingDataLocation};
	}

	/**
	 * Set the locations of Quartz job definition XML files that follow the
	 * "job_scheduling_data_1_0" DTD. Can be specified to automatically
	 * register jobs that are defined in such files, possibly in addition
	 * to jobs defined directly on this SchedulerFactoryBean.
	 * @see ResourceJobSchedulingDataProcessor
	 * @see org.quartz.xml.JobSchedulingDataProcessor
	 */
	public void setJobSchedulingDataLocations(String[] jobSchedulingDataLocations) {
		this.jobSchedulingDataLocations = jobSchedulingDataLocations;
	}

	/**
	 * Register a list of JobDetail objects with the Scheduler that
	 * this FactoryBean creates, to be referenced by Triggers.
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
	 * Register a list of Quartz Calendar objects with the Scheduler
	 * that this FactoryBean creates, to be referenced by Triggers.
	 * @param calendars Map with calendar names as keys as Calendar
	 * objects as values
	 * @see org.quartz.Calendar
	 * @see org.quartz.Trigger#setCalendarName
	 */
	public void setCalendars(Map calendars) {
		this.calendars = calendars;
	}

	/**
	 * Register a list of Trigger objects with the Scheduler that
	 * this FactoryBean creates.
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

	/**
	 * Set whether to automatically start the scheduler after initialization.
	 * Default is true; set this to false to allow for manual startup.
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	/**
	 * Set the number of seconds to wait after initialization before
	 * starting the scheduler asynchronously. Default is 0, meaning
	 * immediate synchronous startup on initialization of this bean.
	 * <p>Setting this to 10 or 20 seconds makes sense if no jobs
	 * should be run before the entire application has started up.
	 */
	public void setStartupDelay(int startupDelay) {
		this.startupDelay = startupDelay;
	}

	/**
	 * Set whether to wait for running jobs to complete on shutdown.
	 * Default is false.
	 * @see org.quartz.Scheduler#shutdown(boolean)
	 */
	public void setWaitForJobsToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
		this.waitForJobsToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
	}


	public void afterPropertiesSet() throws Exception {
		// create SchedulerFactory
		SchedulerFactory schedulerFactory = (SchedulerFactory)
		    BeanUtils.instantiateClass(this.schedulerFactoryClass);

		// load and/or apply Quartz properties
		if (this.configLocation != null || this.quartzProperties != null || this.dataSource != null) {
			if (!(schedulerFactory instanceof StdSchedulerFactory)) {
				throw new IllegalArgumentException("StdSchedulerFactory required for applying Quartz properties");
			}

			Properties props = new Properties();

			// Set necessary default properties here, as Quartz will not apply
			// its default configuration when explicitly given properties.
			props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
			props.setProperty(PROP_THREAD_COUNT, Integer.toString(DEFAULT_THREAD_COUNT));

			if (this.configLocation != null) {
				// load Quartz properties from given location
				InputStream is = this.configLocation.getInputStream();
				try {
					props.load(is);
				}
				finally {
					is.close();
				}
			}

			if (this.quartzProperties != null) {
				// use propertyNames enumeration to also catch default properties
				for (Enumeration enum = this.quartzProperties.propertyNames(); enum.hasMoreElements();) {
					String key = (String) enum.nextElement();
					props.setProperty(key, this.quartzProperties.getProperty(key));
				}
			}

			if (this.dataSource != null) {
				props.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, LocalDataSourceJobStore.class.getName());
			}

			((StdSchedulerFactory) schedulerFactory).initialize(props);
		}

		if (this.dataSource != null) {
			// make given DataSource available for SchedulerFactory configuration
			LocalDataSourceJobStore.configTimeDataSourceHolder.set(this.dataSource);
		}

		// get Scheduler instance from SchedulerFactory
		this.scheduler = createScheduler(schedulerFactory, this.schedulerName);

		if (this.dataSource != null) {
			LocalDataSourceJobStore.configTimeDataSourceHolder.set(null);
		}

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

		registerJobsAndTriggers();

		// start Sheduler if demanded
		if (this.autoStartup) {
			startScheduler(this.scheduler, this.startupDelay);
		}
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

	/**
	 * Register jobs and triggers (within a transaction, if possible).
	 */
	private void registerJobsAndTriggers() throws Exception {
		TransactionStatus transactionStatus = null;
		if (this.transactionManager != null) {
			transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		}
		try {

			if (this.jobSchedulingDataLocations != null) {
				ResourceJobSchedulingDataProcessor dataProcessor = new ResourceJobSchedulingDataProcessor();
				if (this.applicationContext != null) {
					dataProcessor.setResourceLoader(this.applicationContext);
				}
				for (int i = 0; i < this.jobSchedulingDataLocations.length; i++) {
					dataProcessor.processFileAndScheduleJobs(this.jobSchedulingDataLocations[i], this.scheduler,
																									 this.overwriteExistingJobs);
				}
			}

			// register JobDetails
			if (this.jobDetails != null) {
				for (Iterator it = this.jobDetails.iterator(); it.hasNext();) {
					JobDetail jobDetail = (JobDetail) it.next();
					addJobToScheduler(jobDetail);
				}
			}
			else {
				// create empty list for easier checks when registering triggers
				this.jobDetails = new LinkedList();
			}

			// register Calendars
			if (this.calendars != null) {
				for (Iterator it = this.calendars.keySet().iterator(); it.hasNext();) {
					String calendarName = (String) it.next();
					Calendar calendar = (Calendar) this.calendars.get(calendarName);
					addCalendarToScheduler(calendarName, calendar);
				}
			}

			// register Triggers
			if (this.triggers != null) {
				for (Iterator it = this.triggers.iterator(); it.hasNext();) {
					Trigger trigger = (Trigger) it.next();
					if (this.scheduler.getTrigger(trigger.getName(), trigger.getGroup()) == null) {
						// check if the Trigger is aware of an associated JobDetail
						if (trigger instanceof JobDetailAwareTrigger) {
							JobDetail jobDetail = ((JobDetailAwareTrigger) trigger).getJobDetail();
							// automatically register the JobDetail too
							if (!this.jobDetails.contains(jobDetail) && addJobToScheduler(jobDetail)) {
								this.jobDetails.add(jobDetail);
							}
						}
						this.scheduler.scheduleJob(trigger);
					}
					else if (this.overwriteExistingJobs) {
						this.scheduler.rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
					}
				}
			}
		}

		catch (Exception ex) {
			if (transactionStatus != null) {
				try {
					this.transactionManager.rollback(transactionStatus);
				}
				catch (TransactionException tex) {
					logger.error("Job registration exception overridden by rollback exception", ex);
					throw tex;
				}
			}
			throw ex;
		}
		if (transactionStatus != null) {
			this.transactionManager.commit(transactionStatus);
		}
	}

	/**
	 * Add the given job to the Scheduler, if it doesn't already exist.
	 * Overwrites the job in any case if "overwriteExistingJobs" is set.
	 * @param jobDetail the job to add
	 * @return true if the job was actually added,
	 * false if it already existed before
	 * @see #setOverwriteExistingJobs
	 */
	private boolean addJobToScheduler(JobDetail jobDetail) throws SchedulerException {
		if (this.overwriteExistingJobs ||
		    this.scheduler.getJobDetail(jobDetail.getName(), jobDetail.getGroup()) == null) {
			this.scheduler.addJob(jobDetail, true);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Add the given calendar to the Scheduler, checking for the
	 * corresponding Quartz 1.4 respectively Quartz 1.3 method
	 * (which differ in 1.4's additional "updateTriggers" flag).
	 * @param calendarName the name of the calendar
	 * @param calendar the Calendar object
	 * @see org.quartz.Scheduler#addCalendar
	 */
	private void addCalendarToScheduler(String calendarName, Calendar calendar) throws Exception {
		try {
			try {
				// try Quartz 1.4 (with "updateTriggers" flag)
				Method addCalendarMethod = this.scheduler.getClass().getMethod(
						"addCalendar", new Class[] {String.class, Calendar.class, boolean.class, boolean.class});
				addCalendarMethod.invoke(
						this.scheduler, new Object[] {calendarName, calendar, Boolean.TRUE, Boolean.TRUE});
			}
			catch (NoSuchMethodException ex) {
				// try Quartz 1.3 (without "updateTriggers" flag)
				Method addCalendarMethod = this.scheduler.getClass().getMethod(
						"addCalendar", new Class[] {String.class, Calendar.class, boolean.class});
				addCalendarMethod.invoke(
						this.scheduler, new Object[] {calendarName, calendar, Boolean.TRUE});
			}
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof Exception) {
				throw (Exception) ex.getTargetException();
			}
			else if (ex.getTargetException() instanceof Error) {
				throw (Error) ex.getTargetException();
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * Start the Quartz Scheduler, respecting the "startupDelay" setting.
	 * @param scheduler the Scheduler to start
	 * @param startupDelay the number of seconds to wait before starting
	 * the Scheduler asynchronously
	 */
	protected void startScheduler(final Scheduler scheduler, final int startupDelay) throws SchedulerException {
		if (startupDelay <= 0) {
			logger.info("Starting Quartz scheduler now");
			scheduler.start();
		}
		else {
			logger.info("Will start Quartz scheduler in " + startupDelay + " seconds");
			new Thread() {
				public void run() {
					try {
						Thread.sleep(startupDelay * 1000);
					}
					catch (InterruptedException ex) {
						// simply proceed
					}
					logger.info("Starting Quartz scheduler now, after delay of " + startupDelay + " seconds");
					try {
						scheduler.start();
					}
					catch (SchedulerException ex) {
						throw new DelayedSchedulerStartException(ex);
					}
				}
			}.start();
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
		logger.info("Shutting down Quartz scheduler");
		this.scheduler.shutdown(this.waitForJobsToCompleteOnShutdown);
	}


	/**
	 * Exception to be thrown if the Quartz scheduler cannot be started
	 * after the specified delay has passed.
	 */
	public static class DelayedSchedulerStartException extends NestedRuntimeException {

		private DelayedSchedulerStartException(SchedulerException ex) {
			super("Could not start Quartz scheduler after delay", ex);
		}
	}

}
