/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
 
/**
 * Convenient abstract superclass for ApplicationContext implementations
 * drawing their configuration from XML documents containing bean definitions
 * understood by an XmlBeanDefinitionParser.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Revision: 1.9 $
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionParser
 */
public abstract class AbstractXmlApplicationContext extends AbstractApplicationContext  {

	/** Bean factory for this context */
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

	protected void refreshBeanFactory() throws BeansException {
		try {
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
			beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
			initBeanDefinitionReader(beanDefinitionReader);
			loadBeanDefinitions(beanDefinitionReader);
			this.beanFactory = beanFactory;
			if (logger.isInfoEnabled()) {
				logger.info("Bean factory for application context '" + getDisplayName() + "': " + beanFactory);
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing XML document for application context [" +
			                                      getDisplayName() + "]", ex);
		} 
	}

	/**
	 * Create the bean factory for this context.
	 * Default implementation creates a DefaultListableBeanFactory with this
	 * context's parent as parent bean factory. Can be overridden in subclasses.
	 * @return the bean factory for this context
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
	 */
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getParent());
	}

	public ConfigurableListableBeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Initialize the bean definition reader used for loading the bean
	 * definitions of this context. Default implementation is empty.
	 * <p>Can be overridden in subclasses, e.g. for turning off XML validation
	 * or using a different XmlBeanDefinitionParser implementation.
	 * @param beanDefinitionReader the bean definition reader used by this context
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setValidating
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setParserClass
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
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
	protected abstract void loadBeanDefinitions(XmlBeanDefinitionReader reader)
	    throws BeansException, IOException;
	
}
