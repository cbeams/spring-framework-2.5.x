/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.support;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanFactoryLoader;
import org.springframework.beans.factory.support.BootstrapException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.StringUtils;

/**
 * Implementation of the BeanFactoryLoader interface useful in EJBs
 * (although not tied to the EJB API), used to load an ApplicationContext.
 *
 * <p>This class will look for the JNDI environment key
 * "java:comp/env/ejb/BeanFactoryPath" for classpath locations
 * of XML Application Context definitions, which will be loaded as
 * one ApplicationContext via ClassPathXmlApplicationContext's
 * constructor. Multiple locations can be separated by any name
 * of commas or spaces.
 *
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @since 20-Jul-2003
 * @version $Id: XmlApplicationContextBeanFactoryLoader.java,v 1.1 2004-01-18 00:09:10 colins Exp $
 */
public class XmlApplicationContextBeanFactoryLoader implements BeanFactoryLoader {
	
	public static final String BEAN_FACTORY_PATH_ENVIRONMENT_KEY = "java:comp/env/ejb/BeanFactoryPath";

	/**
	 * Any number of these characters are considered delimiters
	 * between multiple bean factory paths in a single-String value.
	 */
	public static final String BEAN_FACTORY_PATH_DELIMITERS = ",; ";

	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Load the bean factory.
	 * @throws BootstrapException if the JNDI key is missing or if
	 * the factory cannot be loaded from this location
	 */
	public BeanFactory loadBeanFactory() throws BootstrapException {		
		String beanFactoryPath = null;
		try {
			beanFactoryPath = (String) (new JndiTemplate()).lookup(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
			logger.info("BeanFactoryPath from JNDI is [" + beanFactoryPath + "]");
			
//			DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
//			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
			String[] paths = StringUtils.tokenizeToStringArray(beanFactoryPath, BEAN_FACTORY_PATH_DELIMITERS, true, true);
			ClassPathXmlApplicationContext beanFactory = new ClassPathXmlApplicationContext(paths);
			
			logger.info("Loaded ApplicationContext [" + beanFactory + "]");
			return beanFactory;
		}
		catch (NamingException ex) {
			throw new BootstrapException("Define an environment variable 'ejb/BeanFactoryPath' containing the location on the class path of an XmlBeanFactory"
						+ ex.getMessage(), ex);
		}		
		catch (BeanDefinitionStoreException ex) {
			throw new BootstrapException("Found resource at '" + beanFactoryPath + "' but it's not a valid Spring bean definition XML file: " + ex.getMessage(), null);
		}
	}

	public void unloadBeanFactory(BeanFactory beanFactory) throws FatalBeanException {
		// nothing to do in default implementation
	}

}
