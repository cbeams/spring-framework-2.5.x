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

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Post processor that can make any bean dynamic.
 * 
 * @author Rod Johnson
 */
public abstract class AbstractDynamicObjectConverter extends ProxyConfig implements BeanFactoryAware,
		BeanPostProcessor {

	private int expirySeconds;

	private ConfigurableListableBeanFactory beanFactory;


	/**
	 * @param defaultPollIntervalSeconds
	 *            The defaultPollIntervalSeconds to set.
	 */
	public void setExpirySeconds(int defaultPollIntervalSeconds) {
		this.expirySeconds = defaultPollIntervalSeconds;
	}
	
	public int getExpirySeconds() {
		return expirySeconds;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}


	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	
	/**
	 * Return null if not managed
	 * @return
	 */
	protected abstract AbstractRefreshableTargetSource createRefreshableTargetSource(Object bean, ConfigurableListableBeanFactory beanFactory, String beanName);
	
	protected void customizeProxyFactory(Object bean, ProxyFactory pf) {
		// Optional
	}
	
	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException { 

		AbstractRefreshableTargetSource targetSource = createRefreshableTargetSource(bean, beanFactory, beanName);
		
		if (targetSource != null) {
			ProxyFactory pf = new ProxyFactory();
			
			pf.copyFrom(this);
	
			targetSource.setExpirySeconds(expirySeconds);
			pf.setTargetSource(targetSource);
			
			pf.addAdvisor(new DefaultIntroductionAdvisor(targetSource));//, DynamicScript.class));
			
			if (!getProxyTargetClass()) {
				Class[] intfs = AopUtils.getAllInterfaces(bean);
				for (int i = 0; i < intfs.length; i++) {
					pf.addInterface(intfs[i]);
				}
			}
			
			customizeProxyFactory(bean, pf);
			
			logger.info("Installed refreshable TargetSource " + pf.getTargetSource() + " for bean '" + beanName + "'");
	
			Object wrapped = pf.getProxy();
			return wrapped;
		}
		else {
			return bean;
		}
	}
}