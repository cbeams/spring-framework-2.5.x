/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jmx.access;

import javax.management.JMException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates a proxy to a managed resource running either locally or remotely.
 * The <code>proxyInterface</code> property defines the interface that the
 * generated proxy should implement. This interface should define methods and
 * properties that correspond to operations and attributes in the management
 * interface of the resource you wish to proxy.
 *
 * <p>There is no need for the managed resource to implement the proxy interface,
 * although you may find it convenient to do. It is not required that every
 * operation and attribute in the management interface is matched by a
 * corresponding property or method in the proxy interface.
 *
 * <p>Attempting to invoke or access any method or property on the proxy
 * interface that does not correspond to the management interface will lead
 * to an <code>InvalidInvocationException</code>.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see MBeanClientInterceptor
 * @see InvalidInvocationException
 */
public class MBeanProxyFactoryBean extends MBeanClientInterceptor implements FactoryBean, InitializingBean {

	/**
	 * The interface to proxy.
	 */
	private Class proxyInterface;

	/**
	 * The generated proxy.
	 */
	private Object mbeanProxy;

	/**
	 * Sets the interface that the generated proxy will implement.
	 *
	 * @param managementInterface
	 */
	public void setProxyInterface(Class managementInterface) {
		this.proxyInterface = managementInterface;
	}

	/**
	 * Checks that the <code>proxyInterface</code> has been specified and then
	 * generate the proxy.
	 */
	public void afterPropertiesSet() throws JMException {
		super.afterPropertiesSet();

		if (this.proxyInterface == null) {
			throw new IllegalArgumentException("proxyInterface is required");
		}
		this.mbeanProxy = ProxyFactory.getProxy(this.proxyInterface, this);
	}


	public Object getObject() {
		return this.mbeanProxy;
	}

	public Class getObjectType() {
		return (this.mbeanProxy != null ? this.mbeanProxy.getClass() : null);
	}

	public boolean isSingleton() {
		return true;
	}

}
