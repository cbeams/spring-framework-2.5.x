/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.remoting.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.ProxyFactory;
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
		if (this.serviceInterface == null) {
			throw new IllegalArgumentException("serviceInterface is required");
		}
		if (this.service == null) {
			throw new IllegalArgumentException("service is required");
		}
		if (!this.serviceInterface.isInstance(this.service)) {
			throw new IllegalArgumentException("serviceInterface [" + this.serviceInterface.getName() +
																				 "] needs to be implemented by service [" + this.service + "]");
		}
	}

	/**
	 * Get a proxy for the given service object, implementing the specified
	 * service interface.
	 * <p>Used to export a proxy that does not expose any internals but just
	 * a specific interface intended for remote access. Only applied if the
	 * remoting tool itself does not offer such means itself.
	 * @return the proxy
	 * @see #setServiceInterface
	 */
	protected Object getProxyForService() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.addInterface(this.serviceInterface);
		proxyFactory.setTarget(this.service);
		return proxyFactory.getProxy();
	}

}
