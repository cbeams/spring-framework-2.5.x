package org.springframework.remoting.rmi;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for RMI proxies, supporting both conventional RMI services and
 * RMI invokers. Behaves like the proxied service when used as bean reference,
 * exposing the specified service interface. Proxies will throw RemoteAccessException
 * on remote invocation failure instead of RMI's RemoteException.
 *
 * <p>The service URL must be a valid RMI URL like "rmi://localhost:1099/myservice".
 * RMI invokers work at the RmiInvocationHandler level, needing only one stub
 * for any service. Service interfaces do not have to extend java.rmi.Remote or
 * throw RemoteException. Of course, in and out parameters have to be serializable.
 *
 * <p>With conventional RMI services, this proxy factory is typically used with the
 * RMI service interface. Alternatively, this factory can also proxy a remote RMI
 * service with a matching non-RMI business interface, i.e. an interface that mirrors
 * the RMI service methods but does not declare RemoteExceptions. In the latter case,
 * RemoteExceptions thrown by the RMI stub will automatically get converted to
 * Spring's unchecked RemoteAccessException.
 *
 * <p>The major advantage of RMI, compared to Hessian and Burlap, is serialization.
 * Effectively, any serializable Java object can be transported without hassle.
 * Hessian and Burlap have their own (de-)serialization mechanisms, but are
 * HTTP-based and thus much easier to setup than RMI.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see #setServiceInterface
 * @see #setServiceUrl
 * @see RmiServiceExporter
 * @see org.springframework.remoting.RemoteAccessException
 * @see java.rmi.RemoteException
 * @see java.rmi.Remote
 */
public class RmiProxyFactoryBean extends RmiClientInterceptor implements FactoryBean {

	private Object serviceProxy;

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (getServiceInterface() == null) {
			throw new IllegalArgumentException("serviceInterface is required");
		}
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
