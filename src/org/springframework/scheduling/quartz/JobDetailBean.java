package org.springframework.scheduling.quartz;

import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.Scheduler;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Convenience subclass of Quartz' JobDetail class that eases bean-style
 * usage.
 *
 * <p>JobDetail itself is already a JavaBean but lacks sensible defaults.
 * This class uses the Spring bean name as job name, and the Quartz
 * default group ("DEFAULT") as job group if not specified.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see #setName
 * @see #setGroup
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.quartz.Scheduler#DEFAULT_GROUP
 */
public class JobDetailBean extends JobDetail implements BeanNameAware, InitializingBean {

	private String beanName;

	public void setJobDataAsMap(Map jobDataAsMap) {
		getJobDataMap().putAll(jobDataAsMap);
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void afterPropertiesSet() {
		if (getName() == null) {
			setName(this.beanName);
		}
		if (getGroup() == null) {
			setGroup(Scheduler.DEFAULT_GROUP);
		}
	}

}
