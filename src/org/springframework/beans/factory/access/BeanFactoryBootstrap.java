/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.access;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;

/**
 * One singleton to rule them all. Reads System properties, which
 * must contain the definition of a bootstrap bean factory using
 * the Properties syntax supported by PropertiesBeanDefinitionReader.
 *
 * <oo>The name of the bootstrap factory must be "bootstrapBeanFactory".
 * 
 * Thus a typical definition might be:
 * <code>
 * bootstrapBeanFactory.class=com.mycompany.MyBeanFactory
 * </code>
 *
 * <p>Use as follows:
 * <code>
 * BeanFactory bf = BeanFactoryBootstrap.getInstance().getBeanFactory();
 * </code>
 *
 * @author Rod Johnson
 * @since December 2, 2002
 * @see org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
 * @version $Id: BeanFactoryBootstrap.java,v 1.2 2004-02-13 17:54:13 jhoeller Exp $
 */
public class BeanFactoryBootstrap {
	
	public static final String BEAN_FACTORY_BEAN_NAME = "bootstrapBeanFactory";

	private static BeanFactoryBootstrap instance;
	
	private static BeansException startupException;
	
	private static void initializeSingleton() {
		try {
			instance = new BeanFactoryBootstrap();
		}
		catch (BeansException ex) {
			startupException = ex;
		}
	}
	
	// Do initialization when this class is loaded to avoid
	// potential concurrency issues or the need to synchronize later
	static {
		initializeSingleton();
	}

	/**
	 * Return the singleton instance of the bootstrap factory
	 * @return BeanFactoryBootstrap
	 * @throws org.springframework.beans.BeansException
	 */
	public static BeanFactoryBootstrap getInstance() throws BeansException {
		if (startupException != null)
			throw startupException;
		// Really an assertion
		if (instance == null)
			throw new BootstrapException("Anomaly: instance and exception null", null);
		return instance;
	}
	
	/**
	 * <b>For testing only. Cleans and reinitalizes the instance.
	 * Do not use in a production application!</b>
	 */
	protected static void reinitialize() {
		instance = null;
		startupException = null;
		initializeSingleton();
	}


	/** The Singleton instance */
	private BeanFactory bootstrapFactory;
	
	/**
	 * Apply rules to load factory.
	 */
	private BeanFactoryBootstrap() throws BeansException {
		DefaultListableBeanFactory startupFactory = new DefaultListableBeanFactory();
		PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(startupFactory);
		try {
			propReader.registerBeanDefinitions(System.getProperties());
			this.bootstrapFactory = (BeanFactory) startupFactory.getBean(BEAN_FACTORY_BEAN_NAME);
		}
		catch (ClassCastException ex) {
			throw new BootstrapException("Bootstrap bean factory class does not implement BeanFactory interface", ex);
		}
		catch (NoSuchBeanDefinitionException ex) {
			throw new BootstrapException("No bean named '" + BEAN_FACTORY_BEAN_NAME + "' in system properties: [" + startupFactory + "]", null);
		}
		catch (BeansException ex) {
			throw new BootstrapException("Failed to bootstrap bean factory", ex);
		}
	}

	/**
	 * Return the BeanFactory managed by the Bootstrap.
	 */
	public BeanFactory getBeanFactory() {
		return bootstrapFactory;
	}
	
}
