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

package org.springframework.beans.factory.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.StringUtils;

/**
 * Static factory that allows to register existing singleton instances programmatically.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 06-Jan-03
 */
public class StaticListableBeanFactory implements ListableBeanFactory {

	/** Map from bean name to bean instance */
	private final Map beans = new HashMap();

	public Object getBean(String name) throws BeansException {
		Object bean = this.beans.get(name);
		if (bean instanceof FactoryBean) {
			try {
				return ((FactoryBean) bean).getObject();
			}
			catch (Exception ex) {
				throw new BeanCreationException("FactoryBean threw exception on object creation", ex);
			}
		}
		if (bean == null)
			throw new NoSuchBeanDefinitionException(name, "defined beans are [" +
																										StringUtils.collectionToCommaDelimitedString(this.beans.keySet()) + "]");
		return bean;
	}
	
	public Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBean(name);
		if (!requiredType.isAssignableFrom(bean.getClass())) {
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);
		}
		return bean;
	}

	public boolean containsBean(String name) {
		return this.beans.containsKey(name);
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		Object bean = getBean(name);
		// in case of FactoryBean, return singleton status of created object
		if (bean instanceof FactoryBean) {
			return ((FactoryBean) bean).isSingleton();
		}
		else {
			return true;
		}
	}

	public String[] getAliases(String name) {
		return null;
	}

	public int getBeanDefinitionCount() {
		return this.beans.size();
	}

	public String[] getBeanDefinitionNames() {
		return (String[]) this.beans.keySet().toArray(new String[this.beans.keySet().size()]);
	}

	public String[] getBeanDefinitionNames(Class type) {
		List matches = new ArrayList();
		Set keys = this.beans.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			Class clazz = this.beans.get(name).getClass();
			if (type.isAssignableFrom(clazz)) {
				matches.add(name);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	public boolean containsBeanDefinition(String name) {
		return this.beans.containsKey(name);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) {
		Map matches = new HashMap();
		Set keys = this.beans.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			Object bean = this.beans.get(name);
			if (bean instanceof FactoryBean && includeFactoryBeans) {
				FactoryBean factory = (FactoryBean) bean;
				Class objectType = factory.getObjectType();
				if ((objectType == null && factory.isSingleton()) ||
						((factory.isSingleton() || includePrototypes) &&
						objectType != null && type.isAssignableFrom(objectType))) {
					Object createdObject = getBean(name);
					if (type.isInstance(createdObject)) {
						matches.put(name, createdObject);
					}
				}
			}
			else if (type.isAssignableFrom(bean.getClass())) {
				matches.put(name, bean);
			}
		}
		return matches;
	}

	/**
	 * Add a new singleton bean.
	 */
	public void addBean(String name, Object bean) {
		this.beans.put(name, bean);
	}

}
