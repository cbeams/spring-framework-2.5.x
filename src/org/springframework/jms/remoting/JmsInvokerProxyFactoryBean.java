/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jms.remoting;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for JMS proxies.
 * 
 * <p>Behaves like the proxied service when used as a bean reference,
 * exposing the specified {@link #setServiceInterface(Class) service interface}.
 *
 * <p>For configuration details, see the
 * {@link JmsInvokerClientInterceptor} javadoc.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setConnectionFactory
 * @see #setQueueName
 * @see org.springframework.jms.remoting.JmsInvokerClientInterceptor
 * @see org.springframework.jms.remoting.JmsInvokerServiceExporter
 */
public class JmsInvokerProxyFactoryBean extends JmsInvokerClientInterceptor implements FactoryBean {

	private Class serviceInterface;

	private Object serviceProxy;


	/**
	 * Set the interface that the proxy must implement.
	 * @param serviceInterface the interface that the proxy must implement
	 * @throws IllegalArgumentException if the supplied <code>serviceInterface</code>
	 * is <code>null</code>, or if the supplied <code>serviceInterface</code>
	 * is not an interface type
	 */
	public void setServiceInterface(Class serviceInterface) {
		if (serviceInterface == null || !serviceInterface.isInterface()) {
			throw new IllegalArgumentException("serviceInterface must be an interface");
		}
		this.serviceInterface = serviceInterface;
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (this.serviceInterface == null) {
			throw new IllegalArgumentException("serviceInterface is required");
		}
		this.serviceProxy = ProxyFactory.getProxy(this.serviceInterface, this);
	}


	public Object getObject() {
		return this.serviceProxy;
	}

	public Class getObjectType() {
		return (this.serviceProxy != null) ? this.serviceProxy.getClass() : this.serviceInterface;
	}

	public boolean isSingleton() {
		return true;
	}

}
