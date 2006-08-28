/*
 * Copyright 2002-2006 the original author or authors.
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

import org.quartz.SchedulerContext;
import org.quartz.spi.TriggerFiredBundle;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;

/**
 * Subclass of AdaptableBeanFactory that also supports Spring-style
 * dependency injection on bean properties. This is essentially the
 * direct equivalent of Spring's QuartzJobBean in the shape of a
 * Quartz 1.5 JobFactory.
 *
 * <p>Applies scheduler context, job data map and trigger data map
 * entries as bean property values. If no matching bean property
 * is found, the entry is by default simply ignored. This is
 * analogous to QuartzJobBean's behavior.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see QuartzJobBean
 */
public class SpringBeanJobFactory extends AdaptableJobFactory implements SchedulerContextAware {

	private boolean ignoredUnknownProperties = true;

	private SchedulerContext schedulerContext;


	/**
	 * Set whether unknown properties (not found in the bean) should be ignored.
	 * Default is "true".
	 */
	public void setIgnoredUnknownProperties(boolean ignoredUnknownProperties) {
		this.ignoredUnknownProperties = ignoredUnknownProperties;
	}

	public void setSchedulerContext(SchedulerContext schedulerContext) {
		this.schedulerContext = schedulerContext;
	}


	/**
	 * Create the job instance, populating it with property values taken
	 * from the scheduler context, job data map and trigger data map.
	 */
	protected Object createJobInstance(TriggerFiredBundle bundle) {
		BeanWrapper bw = new BeanWrapperImpl(bundle.getJobDetail().getJobClass());
		if (isEligibleForPropertyPopulation(bw.getWrappedInstance())) {
			MutablePropertyValues pvs = new MutablePropertyValues();
			if (this.schedulerContext != null) {
				pvs.addPropertyValues(this.schedulerContext);
			}
			pvs.addPropertyValues(bundle.getJobDetail().getJobDataMap());
			pvs.addPropertyValues(bundle.getTrigger().getJobDataMap());
			bw.setPropertyValues(pvs, this.ignoredUnknownProperties);
		}
		return bw.getWrappedInstance();
	}

	/**
	 * Return whether the given job object is eligible for having
	 * its bean properties populated.
	 * <p>The default implementation ignores QuartzJobBeans,
	 * which will inject bean properties themselves.
	 * @param jobObject the job object to introspect
	 */
	protected boolean isEligibleForPropertyPopulation(Object jobObject) {
		return (!(jobObject instanceof QuartzJobBean));
	}

}
