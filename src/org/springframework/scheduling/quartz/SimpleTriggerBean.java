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

import java.text.ParseException;
import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;

/**
 * Convenience subclass of Quartz' SimpleTrigger class that eases
 * bean-style usage.
 *
 * <p>SimpleTrigger itself is already a JavaBean but lacks sensible
 * defaults. This class uses the Spring bean name as job name, the
 * Quartz default group ("DEFAULT") as job group, the current time
 * as start time, and indefinite repetition, if not specified.
 *
 * <p>This class will also register the trigger with the job name
 * and group of a given JobDetail. This allows SchedulerFactoryBean
 * to automatically register a trigger for the respective JobDetail,
 * instead of registering the JobDetail separately.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see #setName
 * @see #setGroup
 * @see #setStartTime
 * @see #setJobName
 * @see #setJobGroup
 * @see #setJobDetail
 * @see SchedulerFactoryBean#setTriggers
 * @see SchedulerFactoryBean#setJobDetails
 */
public class SimpleTriggerBean extends SimpleTrigger
    implements JobDetailAwareTrigger, BeanNameAware, InitializingBean {

	private static final Constants constants = new Constants(SimpleTrigger.class);

	private JobDetail jobDetail;

	private long startDelay = 0;

	private String beanName;

	public SimpleTriggerBean() {
		setRepeatCount(REPEAT_INDEFINITELY);
	}

	/**
	 * Set the JobDetail that this trigger should be associated with.
	 * <p>This is typically used with a bean reference if the JobDetail
	 * is a Spring-managed bean. Alternatively, the trigger can also
	 * be associated with a job by name and group.
	 * @see #setJobName
	 * @see #setJobGroup
	 */
	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public JobDetail getJobDetail() {
		return jobDetail;
	}

	/**
	 * Set the delay before starting the job for the first time.
	 * The given number of milliseconds will be added to the current
	 * time to calculate the start time. Default is 0.
	 * <p>This delay will just be applied if no custom start time was
	 * specified. However, in typical usage within a Spring context,
	 * the start time will be the container startup time anyway.
	 * Specifying a relative delay is appropriate in that case.
	 * @see #setStartTime
	 */
	public void setStartDelay(long startDelay) {
		this.startDelay = startDelay;
	}

	/**
	 * Set the misfire instruction via the name of the corresponding
	 * constant in the SimpleTrigger class. Default is
	 * MISFIRE_INSTRUCTION_SMART_POLICY.
	 * @see org.quartz.SimpleTrigger#MISFIRE_INSTRUCTION_FIRE_NOW
	 * @see org.quartz.Trigger#MISFIRE_INSTRUCTION_SMART_POLICY
	 */
	public void setMisfireInstructionName(String constantName) {
		setMisfireInstruction(constants.asNumber(constantName).intValue());
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void afterPropertiesSet() throws ParseException {
		if (getName() == null) {
			setName(this.beanName);
		}
		if (getGroup() == null) {
			setGroup(Scheduler.DEFAULT_GROUP);
		}
		if (getStartTime() == null) {
			setStartTime(new Date(System.currentTimeMillis() + this.startDelay));
		}
		if (this.jobDetail != null) {
			setJobName(this.jobDetail.getName());
			setJobGroup(this.jobDetail.getGroup());
		}
	}

}
