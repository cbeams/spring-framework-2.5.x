package org.springframework.remoting.caucho;

import java.net.MalformedURLException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for Burlap proxies. Behaves like the proxied service when
 * used as bean reference, exposing the specified service interface.
 *
 * <p>The service URL must be an HTTP URL exposing a Burlap service.
 * For details, see BurlapClientInterceptor docs.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see BurlapServiceExporter
 */
public class BurlapProxyFactoryBean extends BurlapClientInterceptor implements FactoryBean {

	private Object serviceProxy;

	public void afterPropertiesSet() throws MalformedURLException {
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
