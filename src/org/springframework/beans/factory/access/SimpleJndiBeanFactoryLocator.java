/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.access;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.StringUtils;

/**
 * @author Rod Johnson
 * @author colin sampaleanu
 * @version $Revision: 1.2 $
 */
public class SimpleJndiBeanFactoryLocator implements BeanFactoryLocator {

	/**
	 * Any number of these characters are considered delimiters
	 * between multiple bean factory paths in a single-String value.
	 */
	public static final String BEAN_FACTORY_PATH_DELIMITERS = ",; ";

	protected static final Log logger = LogFactory.getLog(SimpleJndiBeanFactoryLocator.class);

	/**
	 * Load/use a bean factory, as specified by a factoryKey which is a JNDI address,
	 * of the form <code>java:comp/env/ejb/BeanFactoryPath</code>.
	 */
	public BeanFactoryReference useFactory(String factoryKey) throws BootstrapException {
		String beanFactoryPath = null;
		try {
			beanFactoryPath = (String) (new JndiTemplate()).lookup(factoryKey);
			logger.info("BeanFactoryPath from JNDI is [" + beanFactoryPath + "]");

			String[] paths = StringUtils.tokenizeToStringArray(beanFactoryPath,
					BEAN_FACTORY_PATH_DELIMITERS, true, true);

			final BeanFactory beanFactory = createFactory(paths);
			logger.info("Loaded BeanFactory [" + beanFactory + "]");

			return new BeanFactoryReference() {
				public BeanFactory getFactory() {
					return beanFactory;
				}

				public void release() throws FatalBeanException {
					// nothing to do in default implementation
				}
			};
		}
		catch (NamingException ex) {
			throw new BootstrapException(
					"Define an environment variable 'ejb/BeanFactoryPath' containing the location on the class path of an XmlBeanFactory"
							+ ex.getMessage(), ex);
		}
		catch (BeanDefinitionStoreException ex) {
			throw new BootstrapException("Found resource at '" + beanFactoryPath
					+ "' but it's not a valid Spring bean definition XML file: "
					+ ex.getMessage(), null);
		}
	}
	
	/**
	 * Actually creates the BeanFactory, given an array of classpath resource strings
	 * which should be combined. This is split out as a separate method so that subclasses
	 * can override the actual type uses (to be an ApplicationContext, for example).
	 *  
	 * @param resources an array of Strings representing classpath resource names
	 * @return the created BeanFactory
	 */
	protected BeanFactory createFactory(String[] resources) throws FatalBeanException {
		DefaultListableBeanFactory fac = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(fac);
		for (int i = 0; i < resources.length; ++i) {
			reader.loadBeanDefinitions(new ClassPathResource(resources[i]));
		}
		fac.preInstantiateSingletons();
		return fac;
	}
		
	
}
