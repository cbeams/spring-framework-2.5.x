/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * <p>Convenient factory for remote SLSB proxies.
 * If you want control over interceptor chaining, use an AOP ProxyFactoryBean
 * with SimpleRemoteSlsbInvokerInterceptor rather than rely on this class.
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
 * <p>This proxy factory is typically used with an RMI business interface, which serves
 * as super-interface of the EJB component interface. Alternatively, this factory
 * can also proxy a remote SLSB with a matching non-RMI business interface, i.e. an
 * interface that mirrors the EJB business methods but does not declare RemoteExceptions.
 * In the latter case, RemoteExceptions thrown by the EJB stub will automatically get
 * converted to Spring's unchecked RemoteAccessException.
 *
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 09-May-2003
 * @version $Id: SimpleRemoteStatelessSessionProxyFactoryBean.java,v 1.8 2004-03-17 17:19:42 jhoeller Exp $
 * @see org.springframework.remoting.RemoteAccessException
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
	 * This will normally be a super-interface of the EJB remote component interface.
	 * Using a business methods interface is a best practice when implementing EJBs.
	 * <p>You can also specify a matching non-RMI business interface, i.e. an interface
	 * that mirrors the EJB business methods but does not declare RemoteExceptions.
	 * In this case, RemoteExceptions thrown by the EJB stub will automatically get
	 * converted to Spring's generic RemoteAccessException.
	 * @param businessInterface the business interface of the EJB
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
