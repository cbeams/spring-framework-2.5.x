/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * <p>Convenient factory for local Stateless Session Bean (SLSB) proxies.
 * If you want control over interceptor chaining, use an AOP ProxyFactoryBean
 * with LocalSlsbInvokerInterceptor rather than rely on this class.
 * 
 * <p>See {@link org.springframework.jndi.AbstractJndiLocator} for info on
 * how to specify the JNDI location of the target EJB.
 * 
 * <p>In a bean container, this class is normally best used as a singleton. However,
 * if that bean container pre-instantiates singletons (as do the XML ApplicationContext
 * variants) you may have a problem if the bean container is loaded before the EJB
 * container loads the target EJB. That is because the JNDI lookup will be performed in
 * the init method of this class and cached, but the EJB will not have been bound at the
 * target location yet. The solution is to not pre-instantiate this factory object, but
 * allow it to be created on first use. In the XML containers, this is controlled via
 * the "lazy-init" attribute.
 *  
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @since 09-May-2003
 * @version $Id: LocalStatelessSessionProxyFactoryBean.java,v 1.9 2004-03-17 17:19:42 jhoeller Exp $
 */
public class LocalStatelessSessionProxyFactoryBean extends LocalSlsbInvokerInterceptor
    implements FactoryBean {
	
	/*
	 * Instead of a separate subclass for each type of SlsbInvoker, we could have added
	 * this functionality to AbstractSlsbInvokerInterceptor. However, the avoiding of
	 * code duplication would be outweighed by the confusion this would produce over the
	 * purpose of AbstractSlsbInvokerInterceptor.
	 */
	
	/** EJBLocalObject */
	private Object proxy;
	
	/**
	 * The business interface of the EJB we're proxying.
	 */
	private Class businessInterface;

	/**
	 * Set the business interface of the EJB we're proxying.
	 * This will normally be a super-interface of the EJB local component interface.
	 * Using a business methods interface is a best practice when implementing EJBs.
	 * @param businessInterface set the business interface of the EJB
	 */
	public void setBusinessInterface(Class businessInterface) {
		this.businessInterface = businessInterface;
	}

	/**
	 * Return the business interface of the EJB we're proxying.
	 */
	public Class getBusinessInterface() {
		return businessInterface;
	}

	public void afterLocated() {
		if (this.businessInterface == null) {
			throw new IllegalArgumentException("businessInterface is required");
		}
		this.proxy = ProxyFactory.getProxy(this.businessInterface, this);
	}

	public Object getObject() {
		return this.proxy;
	}

	public Class getObjectType() {
		return (this.proxy != null) ? this.proxy.getClass() : this.businessInterface;
	}

	public boolean isSingleton() {
		return true;
	}

}
