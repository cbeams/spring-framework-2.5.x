/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.support;

import javax.ejb.EnterpriseBean;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextJndiBeanFactoryLocator;

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
 * @author Colin Sampaleanu
 * @version $Id: AbstractEnterpriseBean.java,v 1.9 2004-02-13 17:54:51 jhoeller Exp $
 */
abstract class AbstractEnterpriseBean implements EnterpriseBean {
	
	public static final String BEAN_FACTORY_PATH_ENVIRONMENT_KEY = "java:comp/env/ejb/BeanFactoryPath";

	/**
	 * Helper strategy that knows how to load a Spring BeanFactory
	 * (or ApplicationContext subclass).
	 */
	private BeanFactoryLocator beanFactoryLocator;
	
	/** factoryKey to be used with BeanFactoryLocator */
	private String beanFactoryLocatorKey;

	/** Spring BeanFactory that provides the namespace for this EJB */
	private BeanFactoryReference beanFactoryReference;

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
	 * <p>In case of the default BeanFactoryLocator implementation,
	 * JndiBeanFactoryLocator, this is the JNDI path. The
	 * default value of this property is
	 * {@link #BEAN_FACTORY_PATH_ENVIRONMENT_KEY}
	 */
	public void setBeanFactoryLocatorKey(String factoryKey) {
		this.beanFactoryLocatorKey = factoryKey;
	}

	/**
	 * Load a Spring BeanFactory namespace.
	 * Subclasses must invoke this method.
	 * <p>Package-visible as it shouldn't be called directly by
	 * user-created subclasses.
	 * @see org.springframework.ejb.support.AbstractStatelessSessionBean#ejbCreate()
	 */
	void loadBeanFactory() throws BeansException {
		if (this.beanFactoryLocator == null) {
			this.beanFactoryLocator = new ContextJndiBeanFactoryLocator();
		}
		if (this.beanFactoryLocatorKey == null) {
			this.beanFactoryLocatorKey = BEAN_FACTORY_PATH_ENVIRONMENT_KEY;
		}
		this.beanFactoryReference = this.beanFactoryLocator.useBeanFactory(this.beanFactoryLocatorKey);
	}
	
	/**
	 * Unload the Spring BeanFactory instance.
	 * The default ejbRemove method invokes this method, but subclasses which
	 * override ejbRemove must invoke this method themselves.
	 * <p>Package-visible as it shouldn't be called directly by
	 * user-created subclasses.
	 */
	void unloadBeanFactory() throws FatalBeanException {
		if (this.beanFactoryReference != null) {
			this.beanFactoryReference.release();
			this.beanFactoryReference = null;
		}
	}

	/**
	 * May be called after ejbCreate().
	 * @return the bean gactory
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactoryReference.getFactory();
	}

	/**
	 * EJB lifecycle method, implemented to invoke onEjbRemote and
	 * unload the BeanFactory afterwards.
	 * <p>Don't override it (although it can't be made final):
	 * code your shutdown in onEjbRemove.
	 * @see #onEjbRemove
	 */
	public void ejbRemove() {
		onEjbRemove();
		unloadBeanFactory();
	}

	/**
	 * Subclasses must implement this method to do any initialization
	 * they would otherwise have done in an ejbRemove() method.
	 * The BeanFactory will be unloaded afterwards.
	 * <p>This implementation is empty, to be overridden in subclasses.
	 * The same restrictions apply to the work of this method as to
	 * an ejbRemove() method.
	 */
	protected void onEjbRemove() {
		// empty
	}

}
