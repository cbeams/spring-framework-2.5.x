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

package org.springframework.beans.factory.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;

/**
 * A property resource configurer that overrides bean property values in an application
 * context definition. It <i>pushes</i> values from a properties file into bean definitions.
 *
 * <p>Configuration lines are expected to be of the following form:
 *
 * <pre>
 * beanName.property=value</pre>
 *
 * Example properties file:
 *
 * <pre>
 * dataSource.driverClassName=com.mysql.jdbc.Driver
 * dataSource.url=jdbc:mysql:mydb</pre>
 *
 * In contrast to PropertyPlaceholderConfigurer, the original definition can have default
 * values or no values at all for such bean properties. If an overriding properties file does
 * not have an entry for a certain bean property, the default context definition is used.
 *
 * <p>Note that the context definition <i>is not</i> aware of being overridden;
 * so this is not immediately obvious when looking at the XML definition file.
 *
 * <p>In case of multiple PropertyOverrideConfigurers that define different values for
 * the same bean property, the <i>last</i> one will win (due to the overriding mechanism).
 *
 * @author Juergen Hoeller
 * @since 12.03.2003
 * @see PropertyPlaceholderConfigurer
 */
public class PropertyOverrideConfigurer extends PropertyResourceConfigurer {

	/** Contains names of beans that have overrides */
	private Set beanNames = Collections.synchronizedSet(new HashSet());
	
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {
		for (Enumeration enum = props.propertyNames(); enum.hasMoreElements();) {
			String key = (String) enum.nextElement();
			processKey(beanFactory, key, props.getProperty(key));
		}
	}

	/**
	 * Process the given key as 'beanName.property' entry.
	 */
	protected void processKey(ConfigurableListableBeanFactory factory, String key, String value)
			throws BeansException {
		int dotIndex = key.indexOf('.');
		if (dotIndex == -1) {
			throw new FatalBeanException("Invalid key [" + key + "]: expected 'beanName.property'");
		}
		String beanName = key.substring(0, dotIndex);
		String beanProperty = key.substring(dotIndex+1);
		beanNames.add(beanName);
		applyPropertyValue(factory, beanName, beanProperty, value);
		logger.debug("Property '" + key + "' set to [" + value + "]");
	}
	
	protected void applyPropertyValue(ConfigurableListableBeanFactory factory, String beanName, String property, String value) {
		BeanDefinition bd = factory.getBeanDefinition(beanName);
		bd.getPropertyValues().addPropertyValue(property, value);
	}
	
	/**
	 * Were there overrides for this bean?
	 * Only valid after processing has occurred at least once
	 * @param beanName name of the bean to query status for
	 * @return whether there were property overrides for
	 * the named bean
	 */
	public boolean hasPropertyOverridesFor(String beanName) {
		return beanNames.contains(beanName);
	}

}
