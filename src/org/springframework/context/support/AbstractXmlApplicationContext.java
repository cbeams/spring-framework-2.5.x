/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.AbstractXmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
 
/**
 * Convenient abstract superclass for ApplicationContext implementations
 * drawing their configuration from XML documents containing bean definitions
 * understood by an DefaultXmlBeanDefinitionReader.
 * @author Rod Johnson
 * @version $Revision: 1.6 $
 * @see org.springframework.beans.factory.xml.XmlBeanFactory
 */
public abstract class AbstractXmlApplicationContext extends AbstractApplicationContext  {

	/** BeanFactory for this context */
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * Create a new AbstractXmlApplicationContext with no parent.
	 */
	public AbstractXmlApplicationContext() {
	}
	
	/**
	 * Create a new AbstractXmlApplicationContext with the given parent context.
	 * @param parent parent context
	 */
	public AbstractXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Return the default BeanFactory for this context.
	 */
	public ConfigurableListableBeanFactory getBeanFactory() {
		return beanFactory;
	}

	protected void refreshBeanFactory() throws BeansException {
		String identifier = "application context [" + getDisplayName() + "]";
		try {
			DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory(getParent());
			DefaultXmlBeanDefinitionReader reader = new DefaultXmlBeanDefinitionReader(beanFactory);
			reader.setEntityResolver(new ResourceBaseEntityResolver(this));
			loadBeanDefinitions(reader);
			this.beanFactory = beanFactory;
			if (logger.isInfoEnabled()) {
				logger.info("Bean factory for application context: " + beanFactory);
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing XML document for " + identifier, ex);
		} 
	}

	/**
	 * Load the bean definitions with the given DefaultXmlBeanDefinitionReader.
	 * <p>The lifecycle of the bean factory is handled by refreshBeanFactory;
	 * therefore an implemention of this template method is just supposed
	 * to load and/or register bean definitions.
	 * @throws BeansException in case of bean registration errors
	 * @throws IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 */
	protected abstract void loadBeanDefinitions(AbstractXmlBeanDefinitionReader reader) throws BeansException, IOException;
	
}
