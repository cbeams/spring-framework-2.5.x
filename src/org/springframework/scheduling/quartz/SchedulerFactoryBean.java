package org.springframework.scheduling.quartz;

import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.core.io.Resource;

/**
 * FactoryBean that sets up a Quartz Scheduler and exposes it for
 * bean references.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 */
public class SchedulerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Class schedulerFactoryClass = StdSchedulerFactory.class;

	private String schedulerName;

	private Resource configLocation;

	private Properties quartzProperties;

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
		if (this.schedulerName != null) {
			this.scheduler = schedulerFactory.getScheduler(this.schedulerName);
		}
		else {
			this.scheduler = schedulerFactory.getScheduler();
		}

		// register JobDetails
		if (this.jobDetails != null) {
			for (Iterator it = this.jobDetails.iterator(); it.hasNext();) {
				JobDetail jobDetail = (JobDetail) it.next();
				this.scheduler.addJob(jobDetail, true);
			}
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
				JobDetail jobDetail = null;
				if (trigger instanceof JobDetailAwareTrigger) {
					jobDetail = ((JobDetailAwareTrigger) trigger).getJobDetail();
				}
				if (jobDetail != null && (this.jobDetails == null || !this.jobDetails.contains(jobDetail))) {
					// automatically register the JobDetail too
					this.scheduler.scheduleJob(jobDetail, trigger);
				}
				else {
					this.scheduler.scheduleJob(trigger);
				}
			}
		}

		logger.info("Starting Quartz Scheduler");
		this.scheduler.start();
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
