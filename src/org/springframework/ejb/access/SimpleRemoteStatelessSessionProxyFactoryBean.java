/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Convenient factory for remote SLSB proxies.
 * If you want control over interceptor chaining, use an AOP ProxyFactoryBean
 * rather than rely on this class.
 * @author Rod Johnson
 * @since 09-May-2003
 * @version $Id: SimpleRemoteStatelessSessionProxyFactoryBean.java,v 1.5 2003-12-19 11:28:17 jhoeller Exp $
 */
public class SimpleRemoteStatelessSessionProxyFactoryBean extends SimpleRemoteSlsbInvokerInterceptor
    implements FactoryBean {
	
	/*
	 * Instead of a separate subclass for each type of SLSBInvoker, we could have added
	 * this functionality to AbstractSlsbInvokerInterceptor. However, the avoiding of
	 * code duplication would be outweighed by the confusion this would produce over the
	 * purpose of AbstractSlsbInvokerInterceptor.
	 */
	
	/** The business interface of the EJB we're proxying */
	private Class businessInterface;

	/** EJBObject */
	private Object proxy;

	/**
	 * Set the business interface of the EJB we're proxying.
	 * This* will normally be the superinterface of the EJB remote component interface.
	 * Using a business methods interface is a best practice when implementing EJBs.
	 * @param businessInterface set the business interface of the EJB
	 */
	public void setBusinessInterface(Class businessInterface) {
		this.businessInterface = businessInterface;
	}

	/**
	 * Returns the business interface of the EJB we're proxying.
	 */
	public Class getBusinessInterface() {
		return businessInterface;
	}

	public void afterLocated() {
		if (this.businessInterface == null) {
			throw new IllegalArgumentException("businessInterface is required");
		}
		ProxyFactory pf = new ProxyFactory(new Class[] { this.businessInterface });
		pf.addInterceptor(this);
		this.proxy = pf.getProxy();
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
