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

package org.springframework.beans.factory.dynamic;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Superclass for bean post processors that create TargetSources of type AbstractRefreshableTargetSource
 * for selected managed beans. The beans are chosen by whether the subclass's
 * createRefreshableTargetSource() returns an AbstractRefreshableTargetSource,
 * rather than null, for the object.
 * @author Rod Johnson
 */
public abstract class AbstractDynamicObjectAutoProxyCreator extends AbstractAutoProxyCreator {
	
	private int expirySeconds;
	
	public void setExpirySeconds(int defaultPollIntervalSeconds) {
		this.expirySeconds = defaultPollIntervalSeconds;
	}
	
	public int getExpirySeconds() {
		return expirySeconds;
	}

	public void setCustomTargetSourceCreators(TargetSourceCreator[] targetSourceCreators) {
		throw new UnsupportedOperationException(
				"Custom target sources are not supported for DynamicObjectAutoProxyCreators");
	}

	protected Object[] getAdvicesAndAdvisorsForBean(
			Class beanClass, String beanName, TargetSource targetSource) throws BeansException {

		if (targetSource == null) {
			return DO_NOT_PROXY;
		}
		else if (!(targetSource instanceof AbstractRefreshableTargetSource)) {
			return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
		}
		else {
			// This is a dynamic object. Get the target source, which is also an introduction
			// advice, and add it.
			AbstractRefreshableTargetSource ats = (AbstractRefreshableTargetSource) targetSource;
			ats.refresh();
			// TargetSource must have been created by this class
			return new Object[] { ats.getIntroductionAdvisor() };
		}
	}
	
	/**
	 * Return null if the object should not be managed as a dynamic object.
	 */
	protected abstract AbstractRefreshableTargetSource createRefreshableTargetSource(
			Object bean, ConfigurableListableBeanFactory beanFactory, String beanName);

	protected TargetSource getCustomTargetSource(Object bean, String beanName) {
		AbstractRefreshableTargetSource ts = createRefreshableTargetSource(
				bean, (ConfigurableListableBeanFactory) getBeanFactory(), beanName);
		if (ts != null) {
			ts.setExpirySeconds(expirySeconds);
		}
		return ts;
	}

}
