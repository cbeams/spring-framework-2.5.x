/*
 * Copyright 2002-2007 the original author or authors.
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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.quartz.JobListener;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.SchedulingException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;

/**
 * FactoryBean that sets up a Quartz {@link org.quartz.Scheduler},
 * manages its lifecycle as part of the Spring application context,
 * and exposes the Scheduler reference for dependency injection.
 *
 * <p>Allows registration of JobDetails, Calendars and Triggers, automatically
 * starting the scheduler on initialization and shutting it down on destruction.
 * In scenarios that just require static registration of jobs at startup, there
 * is no need to access the Scheduler instance itself in application code.
 *
 * <p>For dynamic registration of jobs at runtime, use a bean reference to
 * this SchedulerFactoryBean to get direct access to the Quartz Scheduler
 * (<code>org.quartz.Scheduler</code>). This allows you to create new jobs
 * and triggers, and also to control and monitor the entire Scheduler.
 *
 * <p>Note that Quartz instantiates a new Job for each execution, in
 * contrast to Timer which uses a TimerTask instance that is shared
 * between repeated executions. Just JobDetail descriptors are shared.
 *
 * <p>When using persistent jobs, it is strongly recommended to perform all
 * operations on the Scheduler within Spring-managed (or plain JTA) transactions.
 * Else, database locking will not properly work and might even break.
 * (See {@link #setDataSource setDataSource} javadoc for details.)
 *
 * <p>The preferred way to achieve transactional execution is to demarcate
 * declarative transactions at the business facade level, which will
 * automatically apply to Scheduler operations performed within those scopes.
 * Alternatively, you may add transactional advice for the Scheduler itself.
 *
 * <p>This version of Spring's SchedulerFactoryBean requires Quartz 1.5 or higher.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see #setDataSource
 * @see org.quartz.Scheduler
 * @see org.quartz.SchedulerFactory
 * @see org.quartz.impl.StdSchedulerFactory
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 */
public class SchedulerFactoryBean
    implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean, Lifecycle {

	public static final String PROP_THREAD_COUNT = "org.quartz.threadPool.threadCount";

	public static final int DEFAULT_THREAD_COUNT = 10;


	private static final ThreadLocal configTimeTaskExecutorHolder = new ThreadLocal();

	private static final ThreadLocal configTimeDataSourceHolder = new ThreadLocal();

	private static final ThreadLocal configTimeNonTransactionalDataSourceHolder = new ThreadLocal();

	/**
	 * Return the TaskExecutor for the currently configured Quartz Scheduler,
	 * to be used by LocalTaskExecutorThreadPool.
	 * <p>This instance will be set before initialization of the corresponding
	 * Scheduler, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setDataSource
	 * @see LocalDataSourceJobStore
	 */
	public static TaskExecutor getConfigTimeTaskExecutor() {
		return (TaskExecutor) configTimeTaskExecutorHolder.get();
	}

	/**
	 * Return the DataSource for the currently configured Quartz Scheduler,
	 * to be used by LocalDataSourceJobStore.
	 * <p>This instance will be set before initialization of the corresponding
	 * Scheduler, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setDataSource
	 * @see LocalDataSourceJobStore
	 */
	public static DataSource getConfigTimeDataSource() {
		return (DataSource) configTimeDataSourceHolder.get();
	}

	/**
	 * Return the non-transactional DataSource for the currently configured
	 * Quartz Scheduler, to be used by LocalDataSourceJobStore.
	 * <p>This instance will be set before initialization of the corresponding
	 * Scheduler, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setNonTransactionalDataSource
	 * @see LocalDataSourceJobStore
	 */
	public static DataSource getConfigTimeNonTransactionalDataSource() {
		return (DataSource) configTimeNonTransactionalDataSourceHolder.get();
	}


	protected final Log logger = LogFactory.getLog(getClass());


	private Class schedulerFactoryClass = StdSchedulerFactory.class;

	private String schedulerName;

	private Resource configLocation;

	private Properties quartzProperties;


	private TaskExecutor taskExecutor;

	private DataSource dataSource;

	private DataSource nonTransactionalDataSource;

	private PlatformTransactionManager transactionManager;


	private Map schedulerContextMap;

	private ApplicationContext applicationContext;

	private String applicationContextSchedulerContextKey;

	private JobFactory jobFactory;

	private boolean jobFactorySet = false;


	private boolean overwriteExistingJobs = false;

	private String[] jobSchedulingDataLocations;

	private List jobDetails;

	private Map calendars;

	private List triggers;


	private SchedulerListener[] schedulerListeners;

	private JobListener[] globalJobListeners;

	private JobListener[] jobListeners;

	private TriggerListener[] globalTriggerListeners;

	private TriggerListener[] triggerListeners;


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
		if (schedulerFactoryClass == null || !SchedulerFactory.class.isAssignableFrom(schedulerFactoryClass)) {
			throw new IllegalArgumentException("schedulerFactoryClass must implement [org.quartz.SchedulerFactory]");
		}
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
	 * Set the Spring TaskExecutor to use as Quartz backend.
	 * Exposed as thread pool through the Quartz SPI.
	 * <p>Can be used to assign a JDK 1.5 ThreadPoolExecutor or a CommonJ
	 * WorkManager as Quartz backend, to avoid Quartz's manual thread creation.
	 * <p>By default, a Quartz SimpleThreadPool will be used, configured through
	 * the corresponding Quartz properties.
	 * @see #setQuartzProperties
	 * @see LocalTaskExecutorThreadPool
	 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
	 * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Set the default DataSource to be used by the Scheduler. If set,
	 * this will override corresponding settings in Quartz properties.
	 * <p>Note: If this is set, the Quartz settings should not define
	 * a job store "dataSource" to avoid meaningless double configuration.
	 * <p>A Spring-specific subclass of Quartz' JobStoreCMT will be used.
	 * It is therefore strongly recommended to perform all operations on
	 * the Scheduler within Spring-managed (or plain JTA) transactions.
	 * Else, database locking will not properly work and might even break
	 * (e.g. if trying to obtain a lock on Oracle without a transaction).
	 * <p>Supports both transactional and non-transactional DataSource access.
	 * With a non-XA DataSource and local Spring transactions, a single DataSource
	 * argument is sufficient. In case of an XA DataSource and global JTA transactions,
	 * SchedulerFactoryBean's "nonTransactionalDataSource" property should be set,
	 * passing in a non-XA DataSource that will not participate in global transactions.
	 * @see #setNonTransactionalDataSource
	 * @see #setQuartzProperties
	 * @see #setTransactionManager
	 * @see LocalDataSourceJobStore
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set the DataSource to be used by the Scheduler <i>for non-transactional access</i>.
	 * <p>This is only necessary if the default DataSource is an XA DataSource that will
	 * always participate in transactions: A non-XA version of that DataSource should
	 * be specified as "nonTransactionalDataSource" in such a scenario.
	 * <p>This is not relevant with a local DataSource instance and Spring transactions.
	 * Specifying a single default DataSource as "dataSource" is sufficient there.
	 * @see #setDataSource
	 * @see LocalDataSourceJobStore
	 */
	public void setNonTransactionalDataSource(DataSource nonTransactionalDataSource) {
		this.nonTransactionalDataSource = nonTransactionalDataSource;
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
	 * Set the Quartz JobFactory to use for this Scheduler.
	 * <p>Default is Spring's {@link AdaptableJobFactory}, which supports
	 * {@link java.lang.Runnable} objects as well as standard Quartz
	 * {@link org.quartz.Job} instances. Note that this default only applies
	 * to a <i>local</i> Scheduler, not to a RemoteScheduler (where setting
	 * a custom JobFactory is not supported by Quartz).
	 * <p>Specify an instance of Spring's {@link SpringBeanJobFactory} here
	 * (typically as an inner bean definition) to automatically populate a job's
	 * bean properties from the specified job data map and scheduler context.
	 * @see AdaptableJobFactory
	 * @see SpringBeanJobFactory
	 */
	public void setJobFactory(JobFactory jobFactory) {
		this.jobFactory = jobFactory;
		this.jobFactorySet = true;
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
		// Use modifiable ArrayList here, to allow for further adding of
		// JobDetail objects during autodetection of JobDetailAwareTriggers.
		this.jobDetails = new ArrayList(Arrays.asList(jobDetails));
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
	 * Specify Quartz SchedulerListeners to be registered with the Scheduler.
	 */
	public void setSchedulerListeners(SchedulerListener[] schedulerListeners) {
		this.schedulerListeners = schedulerListeners;
	}

	/**
	 * Specify global Quartz JobListeners to be registered with the Scheduler.
	 * Such JobListeners will apply to all Jobs in the Scheduler.
	 */
	public void setGlobalJobListeners(JobListener[] globalJobListeners) {
		this.globalJobListeners = globalJobListeners;
	}

	/**
	 * Specify named Quartz JobListeners to be registered with the Scheduler.
	 * Such JobListeners will only apply to Jobs that explicitly activate
	 * them via their name.
	 * @see org.quartz.JobListener#getName
	 * @see org.quartz.JobDetail#addJobListener
	 * @see JobDetailBean#setJobListenerNames
	 */
	public void setJobListeners(JobListener[] jobListeners) {
		this.jobListeners = jobListeners;
	}

	/**
	 * Specify global Quartz TriggerListeners to be registered with the Scheduler.
	 * Such TriggerListeners will apply to all Triggers in the Scheduler.
	 */
	public void setGlobalTriggerListeners(TriggerListener[] globalTriggerListeners) {
		this.globalTriggerListeners = globalTriggerListeners;
	}

	/**
	 * Specify named Quartz TriggerListeners to be registered with the Scheduler.
	 * Such TriggerListeners will only apply to Triggers that explicitly activate
	 * them via their name.
	 * @see org.quartz.TriggerListener#getName
	 * @see org.quartz.Trigger#addTriggerListener
	 * @see CronTriggerBean#setTriggerListenerNames
	 * @see SimpleTriggerBean#setTriggerListenerNames
	 */
	public void setTriggerListeners(TriggerListener[] triggerListeners) {
		this.triggerListeners = triggerListeners;
	}


	/**
	 * Set whether to automatically start the scheduler after initialization.
	 * Default is "true"; set this to "false" to allow for manual startup.
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
	 * <p>Default is "false". Switch this to "true" if you prefer
	 * fully completed jobs at the expense of a longer shutdown phase.
	 * @see org.quartz.Scheduler#shutdown(boolean)
	 */
	public void setWaitForJobsToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
		this.waitForJobsToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
	}


	//---------------------------------------------------------------------
	// Implementation of InitializingBean interface
	//---------------------------------------------------------------------

	public void afterPropertiesSet() throws Exception {
		if (this.dataSource == null && this.nonTransactionalDataSource != null) {
			this.dataSource = this.nonTransactionalDataSource;
		}

		// Create SchedulerFactory instance.
		SchedulerFactory schedulerFactory = (SchedulerFactory)
				BeanUtils.instantiateClass(this.schedulerFactoryClass);

		initSchedulerFactory(schedulerFactory);

		if (this.taskExecutor != null) {
			// Make given TaskExecutor available for SchedulerFactory configuration.
			configTimeTaskExecutorHolder.set(this.taskExecutor);
		}
		if (this.dataSource != null) {
			// Make given DataSource available for SchedulerFactory configuration.
			configTimeDataSourceHolder.set(this.dataSource);
		}
		if (this.nonTransactionalDataSource != null) {
			// Make given non-transactional DataSource available for SchedulerFactory configuration.
			configTimeNonTransactionalDataSourceHolder.set(this.nonTransactionalDataSource);
		}


		// Get Scheduler instance from SchedulerFactory.
		try {
			this.scheduler = createScheduler(schedulerFactory, this.schedulerName);
			if (!this.jobFactorySet && !(this.scheduler instanceof RemoteScheduler)) {
				// Use AdaptableJobFactory as default for a local Scheduler, unless when
				// explicitly given a null value through the "jobFactory" bean property.
				this.jobFactory = new AdaptableJobFactory();
			}
			if (this.jobFactory != null) {
				if (this.jobFactory instanceof SchedulerContextAware) {
					((SchedulerContextAware) this.jobFactory).setSchedulerContext(this.scheduler.getContext());
				}
				this.scheduler.setJobFactory(this.jobFactory);
			}
		}

		finally {
			if (this.taskExecutor != null) {
				configTimeTaskExecutorHolder.set(null);
			}
			if (this.dataSource != null) {
				configTimeDataSourceHolder.set(null);
			}
			if (this.nonTransactionalDataSource != null) {
				configTimeNonTransactionalDataSourceHolder.set(null);
			}
		}

		populateSchedulerContext();

		registerListeners();

		registerJobsAndTriggers();

		// Start Scheduler immediately, if demanded.
		if (this.autoStartup) {
			startScheduler(this.scheduler, this.startupDelay);
		}
	}


	/**
	 * Load and/or apply Quartz properties to the given SchedulerFactory.
	 * @param schedulerFactory the SchedulerFactory to initialize
	 */
	private void initSchedulerFactory(SchedulerFactory schedulerFactory)
			throws SchedulerException, IOException {

		if (this.configLocation != null || this.quartzProperties != null ||
				this.dataSource != null || this.schedulerName != null || this.taskExecutor != null) {

			if (!(schedulerFactory instanceof StdSchedulerFactory)) {
				throw new IllegalArgumentException("StdSchedulerFactory required for applying Quartz properties");
			}

			Properties mergedProps = new Properties();

			// Set necessary default properties here, as Quartz will not apply
			// its default configuration when explicitly given properties.
			if (this.taskExecutor != null) {
				mergedProps.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, LocalTaskExecutorThreadPool.class.getName());
			}
			else {
				mergedProps.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
				mergedProps.setProperty(PROP_THREAD_COUNT, Integer.toString(DEFAULT_THREAD_COUNT));
			}

			if (this.configLocation != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading Quartz config from [" + this.configLocation + "]");
				}
				PropertiesLoaderUtils.fillProperties(mergedProps, this.configLocation);
			}

			CollectionUtils.mergePropertiesIntoMap(this.quartzProperties, mergedProps);

			if (this.dataSource != null) {
				mergedProps.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, LocalDataSourceJobStore.class.getName());
			}

			// Make sure to set the scheduler name as configured in the Spring configuration.
			if (this.schedulerName != null) {
				mergedProps.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, this.schedulerName);
			}

			((StdSchedulerFactory) schedulerFactory).initialize(mergedProps);
		}
	}

	/**
	 * Create the Scheduler instance for the given factory and scheduler name.
	 * Called by afterPropertiesSet.
	 * <p>Default implementation invokes SchedulerFactory's <code>getScheduler</code>
	 * method. Can be overridden for custom Scheduler creation.
	 * @param schedulerFactory the factory to create the Scheduler with
	 * @param schedulerName the name of the scheduler to create
	 * @return the Scheduler instance
	 * @throws SchedulerException if thrown by Quartz methods
	 * @see #afterPropertiesSet
	 * @see org.quartz.SchedulerFactory#getScheduler
	 */
	protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName)
			throws SchedulerException {

		// StdSchedulerFactory's default "getScheduler" implementation
		// uses the scheduler name specified in the Quartz properties,
		// which we have set before (in "initSchedulerFactory").
		return schedulerFactory.getScheduler();
	}

	/**
	 * Expose the specified context attributes and/or the current
	 * ApplicationContext in the Quartz SchedulerContext.
	 */
	private void populateSchedulerContext() throws SchedulerException {
		// Put specified objects into Scheduler context.
		if (this.schedulerContextMap != null) {
			this.scheduler.getContext().putAll(this.schedulerContextMap);
		}

		// Register ApplicationContext in Scheduler context.
		if (this.applicationContextSchedulerContextKey != null) {
			if (this.applicationContext == null) {
				throw new IllegalStateException(
				    "SchedulerFactoryBean needs to be set up in an ApplicationContext " +
				    "to be able to handle an 'applicationContextSchedulerContextKey'");
			}
			this.scheduler.getContext().put(this.applicationContextSchedulerContextKey, this.applicationContext);
		}
	}


	/**
	 * Register all specified listeners with the Scheduler.
	 */
	private void registerListeners() throws SchedulerException {
		if (this.schedulerListeners != null) {
			for (int i = 0; i < this.schedulerListeners.length; i++) {
				this.scheduler.addSchedulerListener(this.schedulerListeners[i]);
			}
		}
		if (this.globalJobListeners != null) {
			for (int i = 0; i < this.globalJobListeners.length; i++) {
				this.scheduler.addGlobalJobListener(this.globalJobListeners[i]);
			}
		}
		if (this.jobListeners != null) {
			for (int i = 0; i < this.jobListeners.length; i++) {
				this.scheduler.addJobListener(this.jobListeners[i]);
			}
		}
		if (this.globalTriggerListeners != null) {
			for (int i = 0; i < this.globalTriggerListeners.length; i++) {
				this.scheduler.addGlobalTriggerListener(this.globalTriggerListeners[i]);
			}
		}
		if (this.triggerListeners != null) {
			for (int i = 0; i < this.triggerListeners.length; i++) {
				this.scheduler.addTriggerListener(this.triggerListeners[i]);
			}
		}
	}

	/**
	 * Register jobs and triggers (within a transaction, if possible).
	 */
	private void registerJobsAndTriggers() throws SchedulerException {
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
					dataProcessor.processFileAndScheduleJobs(
					    this.jobSchedulingDataLocations[i], this.scheduler, this.overwriteExistingJobs);
				}
			}

			// Register JobDetails.
			if (this.jobDetails != null) {
				for (Iterator it = this.jobDetails.iterator(); it.hasNext();) {
					JobDetail jobDetail = (JobDetail) it.next();
					addJobToScheduler(jobDetail);
				}
			}
			else {
				// Create empty list for easier checks when registering triggers.
				this.jobDetails = new LinkedList();
			}

			// Register Calendars.
			if (this.calendars != null) {
				for (Iterator it = this.calendars.keySet().iterator(); it.hasNext();) {
					String calendarName = (String) it.next();
					Calendar calendar = (Calendar) this.calendars.get(calendarName);
					this.scheduler.addCalendar(calendarName, calendar, true, true);
				}
			}

			// Register Triggers.
			if (this.triggers != null) {
				for (Iterator it = this.triggers.iterator(); it.hasNext();) {
					Trigger trigger = (Trigger) it.next();
					addTriggerToScheduler(trigger);
				}
			}
		}

		catch (Throwable ex) {
			if (transactionStatus != null) {
				try {
					this.transactionManager.rollback(transactionStatus);
				}
				catch (TransactionException tex) {
					logger.error("Job registration exception overridden by rollback exception", ex);
					throw tex;
				}
			}
			if (ex instanceof SchedulerException) {
				throw (SchedulerException) ex;
			}
			if (ex instanceof Exception) {
				throw new SchedulerException(
						"Registration of jobs and triggers failed: " + ex.getMessage(), (Exception) ex);
			}
			throw new SchedulerException("Registration of jobs and triggers failed: " + ex.getMessage());
		}

		if (transactionStatus != null) {
			this.transactionManager.commit(transactionStatus);
		}
	}

	/**
	 * Add the given job to the Scheduler, if it doesn't already exist.
	 * Overwrites the job in any case if "overwriteExistingJobs" is set.
	 * @param jobDetail the job to add
	 * @return <code>true</code> if the job was actually added,
	 * <code>false</code> if it already existed before
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
	 * Add the given trigger to the Scheduler, if it doesn't already exist.
	 * Overwrites the trigger in any case if "overwriteExistingJobs" is set.
	 * @param trigger the trigger to add
	 * @return <code>true</code> if the trigger was actually added,
	 * <code>false</code> if it already existed before
	 * @see #setOverwriteExistingJobs
	 */
	private boolean addTriggerToScheduler(Trigger trigger) throws SchedulerException {
		boolean triggerExists = (this.scheduler.getTrigger(trigger.getName(), trigger.getGroup()) != null);
		if (!triggerExists || this.overwriteExistingJobs) {
			// Check if the Trigger is aware of an associated JobDetail.
			if (trigger instanceof JobDetailAwareTrigger) {
				JobDetail jobDetail = ((JobDetailAwareTrigger) trigger).getJobDetail();
				// Automatically register the JobDetail too.
				if (!this.jobDetails.contains(jobDetail) && addJobToScheduler(jobDetail)) {
					this.jobDetails.add(jobDetail);
				}
			}
			if (!triggerExists) {
				try {
					this.scheduler.scheduleJob(trigger);
				}
				catch (ObjectAlreadyExistsException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Unexpectedly found existing trigger, assumably due to cluster race condition: " +
								ex.getMessage() + " - can safely be ignored");
					}
					if (this.overwriteExistingJobs) {
						this.scheduler.rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
					}
				}
			}
			else {
				this.scheduler.rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
			}
			return true;
		}
		else {
			return false;
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
			logger.info("Starting Quartz Scheduler now");
			scheduler.start();
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("Will start Quartz Scheduler [" + scheduler.getSchedulerName() +
						"] in " + startupDelay + " seconds");
			}
			Thread schedulerThread = new Thread() {
				public void run() {
					try {
						Thread.sleep(startupDelay * 1000);
					}
					catch (InterruptedException ex) {
						// simply proceed
					}
					if (logger.isInfoEnabled()) {
						logger.info("Starting Quartz Scheduler now, after delay of " + startupDelay + " seconds");
					}
					try {
						scheduler.start();
					}
					catch (SchedulerException ex) {
						throw new SchedulingException("Could not start Quartz Scheduler after delay", ex);
					}
				}
			};
			schedulerThread.setName("Quartz Scheduler [" + scheduler.getSchedulerName() + "]");
			schedulerThread.start();
		}
	}


	//---------------------------------------------------------------------
	// Implementation of FactoryBean interface
	//---------------------------------------------------------------------

	public Object getObject() {
		return this.scheduler;
	}

	public Class getObjectType() {
		return (this.scheduler != null) ? this.scheduler.getClass() : Scheduler.class;
	}

	public boolean isSingleton() {
		return true;
	}


	//---------------------------------------------------------------------
	// Implementation of Lifecycle interface
	//---------------------------------------------------------------------

	public void start() throws SchedulingException {
		if (this.scheduler != null) {
			try {
				this.scheduler.start();
			}
			catch (SchedulerException ex) {
				throw new SchedulingException("Could not start Quartz Scheduler", ex);
			}
		}
	}

	public void stop() throws SchedulingException {
		if (this.scheduler != null) {
			try {
				this.scheduler.standby();
			}
			catch (SchedulerException ex) {
				throw new SchedulingException("Could not stop Quartz Scheduler", ex);
			}
		}
	}

	public boolean isRunning() throws SchedulingException {
		if (this.scheduler != null) {
			try {
				return !this.scheduler.isInStandbyMode();
			}
			catch (SchedulerException ex) {
				return false;
			}
		}
		return false;
	}


	//---------------------------------------------------------------------
	// Implementation of DisposableBean interface
	//---------------------------------------------------------------------

	/**
	 * Shut down the Quartz scheduler on bean factory shutdown,
	 * stopping all scheduled jobs.
	 */
	public void destroy() throws SchedulerException {
		logger.info("Shutting down Quartz Scheduler");
		this.scheduler.shutdown(this.waitForJobsToCompleteOnShutdown);
	}

}
