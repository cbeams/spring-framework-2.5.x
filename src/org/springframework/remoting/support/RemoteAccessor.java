/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.remoting.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for classes that proxy a remote service.
 * Exposes the proxy when used as bean reference. Used e.g. by the
 * Caucho and RMI proxy factory implementations.
 *
 * <p>Subclasses just need to implement createProxy, using the properties
 * of the factory instance. Note that such a proxy should throw unchecked
 * RemoteAccessException, to be able to transparently expose the service
 * to client objects via a plain Java business interface.
 *
 * <p>Note that the service interface being used will show some signs of
 * remotability, like the granularity of method calls that it offers.
 * Furthermore, it has to require serializable arguments etc.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see org.springframework.remoting.RemoteAccessException
 */
public abstract class RemoteAccessor {

	protected final Log logger = LogFactory.getLog(getClass());

	private Class serviceInterface;

	/**
	 * Set the interface of the service that this factory should create a proxy for.
	 * The interface must be suitable for the particular service and remoting tool.
	 */
	public void setServiceInterface(Class serviceInterface) {
		if (!serviceInterface.isInterface()) {
			throw new IllegalArgumentException("serviceInterface must be an interface");
		}
		this.serviceInterface = serviceInterface;
	}

	/**
	 * Return the interface of the service that this factory should create a proxy for.
	 */
	protected Class getServiceInterface() {
		return serviceInterface;
	}

}
