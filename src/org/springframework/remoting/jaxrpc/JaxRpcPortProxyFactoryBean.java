package org.springframework.remoting.jaxrpc;

import javax.xml.rpc.ServiceException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean for a specific port of a JAX-RPC service.
 * Exposes a proxy for the port, to be used for bean references.
 * Inherits configuration properties from JaxRpcPortClientInterceptor.
 *
 * <p>This factory is typically used with an RMI service interface. Alternatively,
 * this factory can also proxy a JAX-RPC service with a matching non-RMI business
 * interface, i.e. an interface that mirrors the RMI service methods but does not
 * declare RemoteExceptions. In the latter case, RemoteExceptions thrown by the
 * JAX-RPC stub will automatically get converted to Spring's unchecked
 * RemoteAccessException.
 *
 * <p>If exposing the JAX-RPC port interface (i.e. an RMI interface) directly,
 * setting "serviceInterface" is sufficient. If exposing a non-RMI business
 * interface, the business interface needs to be set as "serviceInterface",
 * and the JAX-RPC port interface as "portInterface".
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see #setServiceInterface
 * @see #setPortInterface
 * @see LocalJaxRpcServiceFactoryBean
 */
public class JaxRpcPortProxyFactoryBean extends JaxRpcPortClientInterceptor implements FactoryBean {

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
