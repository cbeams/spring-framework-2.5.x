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

package org.springframework.beans.factory.access;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.StringUtils;

/**
 * BeanFactoryLocator implementation that creates the BeanFactory
 * from file locations specified as JNDI environment variable.
 *
 * <p>This default implementation creates a DefaultListableBeanFactory,
 * populated via an XmlBeanDefinitionReader. Subclasses may override
 * createFactory for custom instantiation.
 *
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @version $Revision: 1.2 $
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 */
public class JndiBeanFactoryLocator implements BeanFactoryLocator {

	/**
	 * Any number of these characters are considered delimiters
	 * between multiple bean factory paths in a single-String value.
	 */
	public static final String BEAN_FACTORY_PATH_DELIMITERS = ",; ";

	protected Log logger = LogFactory.getLog(getClass());

	/**
	 * Load/use a bean factory, as specified by a factoryKey which is a JNDI address,
	 * of the form <code>java:comp/env/ejb/BeanFactoryPath</code>.
	 */
	public BeanFactoryReference useBeanFactory(String factoryKey) throws BeansException {
		String beanFactoryPath = null;
		try {
			beanFactoryPath = (String) (new JndiTemplate()).lookup(factoryKey);
			logger.info("BeanFactoryPath from JNDI is [" + beanFactoryPath + "]");
			String[] paths = StringUtils.tokenizeToStringArray(beanFactoryPath,
					BEAN_FACTORY_PATH_DELIMITERS, true, true);
			return createBeanFactory(paths);
		}
		catch (NamingException ex) {
			throw new BootstrapException("Define an environment variable 'ejb/BeanFactoryPath' containing " +
			                             "the class path locations of XML bean definition files", ex);
		}
	}
	
	/**
	 * Actually create the BeanFactory, given an array of classpath resource strings
	 * which should be combined. This is split out as a separate method so that subclasses
	 * can override the actual type uses (to be an ApplicationContext, for example).
	 * @param resources an array of Strings representing classpath resource names
	 * @return the created BeanFactory, wrapped in a BeanFactoryReference
	 */
	protected BeanFactoryReference createBeanFactory(String[] resources) throws BeansException {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
		for (int i = 0; i < resources.length; ++i) {
			reader.loadBeanDefinitions(new ClassPathResource(resources[i]));
		}
		bf.preInstantiateSingletons();
		return new DefaultBeanFactoryReference(bf);
	}

}
