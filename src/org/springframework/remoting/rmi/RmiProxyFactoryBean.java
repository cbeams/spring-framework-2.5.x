package org.springframework.remoting.rmi;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for RMI proxies, supporting both plain and transparent RMI.
 * Behaves like the proxied service when used as bean reference, exposing the
 * specified service interface.
 *
 * <p>The service URL must be a valid RMI URL like "rmi://localhost:1099/myservice".
 * For details on transparent RMI, see RmiClientInterceptor docs.
 *
 * <p>The major advantage of RMI, compared to Hessian and Burlap, is serialization.
 * Effectively, any serializable Java object can be transported without hassle.
 * Hessian and Burlap have their own (de-)serialization mechanisms, but are
 * HTTP-based and thus much easier to setup than RMI.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see RmiServiceExporter
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
