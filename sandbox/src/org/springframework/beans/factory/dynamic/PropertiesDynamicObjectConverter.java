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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

/**
 * TODO implements DynamicObject?
 * @author Rod Johnson
 */
public class PropertiesDynamicObjectConverter extends AbstractDynamicObjectConverter implements DynamicObject, BeanFactoryPostProcessor {
	
	private PropertyOverrideConfigurer poc = new DynamicPropertyOverrideConfigurer();
	
	private DefaultListableBeanFactory childFactory;
	
	public PropertiesDynamicObjectConverter() {
		// Change the default
		setProxyTargetClass(true);
	}
	
	// TODO others
	public void setLocation(Resource location) {
		poc.setLocation(location);
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.AbstractDynamicObjectConverter#createRefreshableTargetSource(java.lang.Object, org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.lang.String)
	 */
	protected AbstractRefreshableTargetSource createRefreshableTargetSource(Object bean,
			ConfigurableListableBeanFactory beanFactory, String beanName) {
		if (poc.hasPropertyOverridesFor(beanName)) {
			DynamicBeanTargetSource ts = new DynamicBeanTargetSource(bean, beanFactory, beanName, childFactory);
			return ts;
		}
		else {
			return null;
		}
	}
	
	// TODO synching?
	public void refresh() {
		poc.postProcessBeanFactory(childFactory);
	}
	
	private void refresh(String beanName) {
		((DynamicObject) childFactory.getParentBeanFactory().getBean(beanName)).refresh();
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		childFactory = new DefaultListableBeanFactory(beanFactory);
		poc.postProcessBeanFactory(beanFactory);
	}

	private class DynamicPropertyOverrideConfigurer extends PropertyOverrideConfigurer {
		protected void applyPropertyValue(ConfigurableListableBeanFactory factory, String beanName, String property, String value) {
			super.applyPropertyValue(factory, beanName, property, value);
			// TODO optimize if unchanged
			if (factory == childFactory) {
				refresh(beanName);
			}
		}
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#getExpiry()
	 */
	public long getExpiry() {
		
		// TODO is this right?
		return getExpirySeconds();
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#isAutoRefresh()
	 */
	public boolean isAutoRefresh() {
		return false;
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#setAutoRefresh(boolean)
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#getLoads()
	 */
	public int getLoads() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#getLastRefreshMillis()
	 */
	public long getLastRefreshMillis() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#isModified()
	 */
	public boolean isModified() {
		throw new UnsupportedOperationException();
	}
}
