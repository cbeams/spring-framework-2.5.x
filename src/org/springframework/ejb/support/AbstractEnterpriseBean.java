/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.support;

import javax.ejb.EnterpriseBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryRef;
import org.springframework.beans.factory.support.BootstrapException;
import org.springframework.context.access.JndiBeanFactoryLocator;

/** 
 * Superclass for all EJBs. Package-visible: not intended for direct
 * subclassing. Provides a logger and a standard way of loading a
 * BeanFactory. Subclasses act as a facade, with the business logic
 * deferred to beans in the BeanFactory.
 *
 * <p>Default is to use an XmlApplicationContextBeanFactoryLoader, which
 * will initialize an XmlApplicationContext from the classpath (based
 * on a JNDI name specified). For a lighter weight implementation when
 * ApplicationContext usage is not required, setBeanFactoryLoader() may
 * be called (<i>before</i> your EJB's ejbCreate() method is invoked, 
 * for example, in setSessionContext()) with a value of XmlBeanFactoryLoader,
 * which will load an XML bean factory from the classpath. Alternately,
 * setBeanFactoryLoader() may be called with a completely custom
 * implementation of the BeanFactoryLoader.
 *
 * <p>Note that we cannot use final for our implementation of
 * EJB lifecycle methods, as this violates the EJB specification.
 *
 * @author Rod Johnson
 * @version $Id: AbstractEnterpriseBean.java,v 1.7 2004-01-27 00:03:45 colins Exp $
 */
abstract class AbstractEnterpriseBean implements EnterpriseBean {
	
	public static final String BEAN_FACTORY_PATH_ENVIRONMENT_KEY = "java:comp/env/ejb/BeanFactoryPath";

	/**
	 * Logger, available to subclasses. Not final since stateful session beans
	 * will have to remove it and restore it in ejbPassivate/ejbActivate.
	 */
	protected Log logger = LogFactory.getLog(getClass());

	/**
	 * Helper strategy that knows how to load Spring BeanFactory (or
	 * ApplicationContext subclass).
	 */
	private BeanFactoryLocator beanFactoryLocator;
	
	/**
	 * FactoryKey to be used with BeanFactoryLocator
	 */
	private String factoryKey;

	/** Spring BeanFactory that provides the namespace for this EJB */
	private BeanFactoryRef beanFactoryRef;

	/**
	 * Load a Spring BeanFactory namespace.
	 * Subclasses must invoke this method. Package-visible as it
	 * shouldn't be called directly by user-created subclasses.
	 * @see org.springframework.ejb.support.AbstractStatelessSessionBean#ejbCreate()
	 */
	void loadBeanFactory() throws BootstrapException {
		if (this.beanFactoryLocator == null) {
			this.beanFactoryLocator = new JndiBeanFactoryLocator();
			this.factoryKey = BEAN_FACTORY_PATH_ENVIRONMENT_KEY;
 
		}
		this.beanFactoryRef = this.beanFactoryLocator.useFactory(this.factoryKey);
	}
	
	/**
	 * Unload the Spring BeanFactory instance.
	 * The default ejbRemove method invokes this method, but subclasses which
	 * override ejbRemove must invoke this method themselves.
	 * Package-visible as it shouldn't be called directly by user-created
	 * subclasses.
	 */
	void unloadBeanFactory() throws FatalBeanException {
		if (this.beanFactoryRef != null) {
			beanFactoryRef.release();
			beanFactoryRef = null;
		}
	}

	/**
	 * Can be invoked before loadBeanFactory.
	 * Invoke in constructor or setXXXXContext() if you want
	 * to override the default bean factory loader.
	 */
	public void setBeanFactoryLocator(BeanFactoryLocator beanFactoryLocator) {
		this.beanFactoryLocator = beanFactoryLocator;
	}
	
	/**
	 * Can be invoked before loadBeanFactory.
	 * Invoke in constructor or setXXXXContext() if you want
	 * to override the default bean factory factory key.
	 * If the default BeanFactoryLocator implementation,
	 * JndiBeanFactoryLocator, this is the JNDI path. The
	 * default value of this property is
	 * {@link #BEAN_FACTORY_PATH_ENVIRONMENT_KEY} 
	 */
	public void setBeanFactoryLocatorKey(String factoryKey) {
		this.factoryKey = factoryKey;
	}
	

	/**
	 * May be called after ejbCreate().
	 * @return the bean Factory
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactoryRef.getFactory();
	}

	/**
	 * Useful EJB lifecycle method. Override if necessary.
	 */
	public void ejbRemove() {
		// Empty
	}

}
