/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * <p>Convenient factory for local Stateless Session Bean (SLSB) proxies.
 * If you want control over interceptor chaining, use an AOP
 * ProxyFactoryBean rather than to rely on this class.</p>
 * 
 * <p>See {@link org.springframework.jndi.AbstractJndiLocator} for info on
 * how to specify the JNDI location of the target EJB</p>
 * 
 * <p>In a bean container, this class is normally best used as a singleton. However,
 * if that bean container pre-instantiates singletons (as do the XML ApplicationContext
 * variants) you may have a problem if the bean container is loaded before the EJB
 * container loads the target EJB. That is because the JNDI lookup will be performed in
 * the init method of this class and cached, but the EJB will not have been bound at the
 * target location yet. The solution is to not pre-instantiate this factory object, but
 * allow it to be created on first use. In the XML containers, this is controlled via
 * the lazy-init attribute.</p>
 *  
 * @author Rod Johnson
 * @author colin sampaleanu
 * @since 09-May-2003
 * @version $Id: LocalStatelessSessionProxyFactoryBean.java,v 1.8 2004-02-18 03:51:11 colins Exp $
 */
public class LocalStatelessSessionProxyFactoryBean extends LocalSlsbInvokerInterceptor
    implements FactoryBean {
	
	/*
	 * Instead of a separate subclass for each type of SLSBInvoker, we could have added
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
	 * Set the business interface of the EJB we're proxying
	 * @param clazz set the business interface of the EJB
	 */
	public void setBusinessInterface(Class clazz) {
		this.businessInterface = clazz;
	}

	/**
	 * @return the business interface of the EJB. Note that this
	 * will normally be the superinterface of the EJBLocal interface.
	 * Using a business methods interface is a best practice
	 * when implementing EJBs.
	 */
	public Class getBusinessInterface() {
		return businessInterface;
	}

	public void afterLocated() {
		if (this.businessInterface == null) {
			throw new IllegalArgumentException("businessInterface property must be set in LocalStatelessSessionProxyFactoryBean");
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
