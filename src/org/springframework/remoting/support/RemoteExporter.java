/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.remoting.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract base class for classes that export a remote service.
 * Provides a "serviceInterface" bean property.
 *
 * <p>Note that the service interface being used will show some signs of
 * remotability, like the granularity of method calls that it offers.
 * Furthermore, it has to require serializable arguments etc.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 */
public abstract class RemoteExporter implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Class serviceInterface;

	private Object service;

	/**
	 * Set the interface of the service to export.
	 * Typically optional: If not set, all implement interfaces will be exported.
	 * The interface must be suitable for the particular service and remoting tool.
	 */
	public void setServiceInterface(Class serviceInterface) {
		if (!serviceInterface.isInterface()) {
			throw new IllegalArgumentException("serviceInterface must be an interface");
		}
		this.serviceInterface = serviceInterface;
	}

	/**
	 * Return the interface of the service to export.
	 */
	protected Class getServiceInterface() {
		return serviceInterface;
	}

	/**
	 * Set the service to export.
	 * Typically populated via a bean reference.
	 */
	public void setService(Object service) {
		this.service = service;
	}

	/**
	 * Return the service to export.
	 */
	protected Object getService() {
		return service;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.service == null) {
			throw new IllegalArgumentException("service is required");
		}
		if (this.serviceInterface != null && !this.serviceInterface.isInstance(service)) {
			throw new IllegalArgumentException("serviceInterface [" + this.serviceInterface.getName() +
																				 "] needs to be implemented by service [" + this.service + "]");
		}
	}

	/**
	 * Get a proxy for the given service object, either just implementing the specified
	 * service interface or all the interfaces implemented by the service object.
	 * <p>Used to export a proxy that does not expose any internals but just specific
	 * interfaces intended for remote access.
	 * @return the proxy
	 * @see #setServiceInterface
	 */
	protected Object getProxyForService() {
		ProxyFactory proxyFactory = new ProxyFactory();
		if (this.serviceInterface != null) {
			proxyFactory.addInterface(this.serviceInterface);
		}
		else {
			proxyFactory.setInterfaces(AopUtils.getAllInterfaces(this.service));
		}
		proxyFactory.setTarget(this.service);
		return proxyFactory.getProxy();
	}

}
