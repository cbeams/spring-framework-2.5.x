/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
 
/**
 * Convenient abstract superclass for ApplicationContext implementations
 * drawing their configuration from XML documents containing bean definitions
 * understood by an XMLBeanFactory.
 * @author Rod Johnson
 * @version $Revision: 1.4 $
 * @see org.springframework.beans.factory.xml.XmlBeanFactory
 */
public abstract class AbstractXmlApplicationContext extends AbstractApplicationContext  {

	/** Default BeanFactory for this context */
	private XmlBeanFactory xmlBeanFactory;

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
	
	protected void refreshBeanFactory() throws ApplicationContextException, BeansException {
		String identifier = "application context [" + getDisplayName() + "]";
		try {
			this.xmlBeanFactory = new XmlBeanFactory(getParent());
			this.xmlBeanFactory.setEntityResolver(new ResourceBaseEntityResolver(this));
			loadBeanDefinitions(this.xmlBeanFactory);
			if (logger.isInfoEnabled()) {
				logger.info("BeanFactory for application context: " + this.xmlBeanFactory);
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("IOException parsing XML document for " + identifier, ex);
		} 
	}
	
	/**
	 * Return the default BeanFactory for this context.
	 */
	public ConfigurableListableBeanFactory getBeanFactory() {
		return xmlBeanFactory;
	}

	/**
	 * Load the bean definitions for the given XmlBeanFactory.
	 * <p>The lifecycle of the bean factory is handled by refreshBeanFactory;
	 * therefore an implemention of this template method is just supposed
	 * to load and/or register bean definitions.
	 * @exception IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 */
	protected abstract void loadBeanDefinitions(XmlBeanFactory beanFactory) throws IOException;
	
}
