package org.springframework.remoting.rmi;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for transparent RMI proxies. Behaves like the proxied service when
 * used as bean reference, exposing the specified service interface.
 *
 * <p>The service URL must be a valid RMI URL like "rmi://localhost:1099/myservice".
 * For details, see RmiClientInterceptor docs.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see RmiServiceExporter
 */
public class RmiProxyFactoryBean extends RmiClientInterceptor implements FactoryBean {

	private Object serviceProxy;

	public void afterPropertiesSet() throws MalformedURLException, NotBoundException, RemoteException {
		super.afterPropertiesSet();
		this.serviceProxy = ProxyFactory.getProxy(getServiceInterface(), this);
	}

	public Object getObject() {
		return this.serviceProxy;
	}

	public boolean isSingleton() {
		return true;
	}

}
