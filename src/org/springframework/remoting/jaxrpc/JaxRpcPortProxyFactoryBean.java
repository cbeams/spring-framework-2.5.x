package org.springframework.remoting.jaxrpc;

import javax.xml.rpc.ServiceException;

import org.springframework.aop.framework.ProxyFactory;

/**
 * FactoryBean for a specific port of a JAX-RPC service.
 * Exposes a proxy for the port, to be used for bean references.
 * Inherits configuration properties from JaxRpcPortClientInterceptor.
 *
 * <p>Can either expose the JAX-RPC port interface - i.e. an RMI interface - directly,
 * or expose a non-RMI business interface. In the former case, setting "serviceInterface"
 * is sufficient; in the latter case, the business interface needs to be set as
 * "serviceInterface", and the JAX-RPC port interface as "portInterface".
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see #setServiceInterface
 * @see #setPortInterface
 * @see LocalJaxRpcServiceFactoryBean
 */
public class JaxRpcPortProxyFactoryBean extends JaxRpcPortClientInterceptor {

	private Object serviceProxy;

	public void afterPropertiesSet() throws ServiceException {
		super.afterPropertiesSet();
		this.serviceProxy = ProxyFactory.getProxy(getServiceInterface(), this);
	}

	public Object getObject() {
		return this.serviceProxy;
	}

	public Class getObjectType() {
		return (this.serviceProxy != null) ? this.serviceProxy.getClass() : getServiceInterface();
	}

	public boolean isSingleton() {
		return true;
	}

}
