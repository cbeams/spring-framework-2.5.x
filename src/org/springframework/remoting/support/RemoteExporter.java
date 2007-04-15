/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.remoting.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for classes that export a remote service.
 * Provides "service" and "serviceInterface" bean properties.
 *
 * <p>Note that the service interface being used will show some signs of
 * remotability, like the granularity of method calls that it offers.
 * Furthermore, it has to have serializable arguments etc.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 */
public abstract class RemoteExporter implements BeanClassLoaderAware {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private Object service;

	private Class serviceInterface;

	private boolean registerTraceInterceptor = true;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


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
	public Object getService() {
		return this.service;
	}

	/**
	 * Set the interface of the service to export.
	 * The interface must be suitable for the particular service and remoting strategy.
	 */
	public void setServiceInterface(Class serviceInterface) {
		if (serviceInterface != null && !serviceInterface.isInterface()) {
			throw new IllegalArgumentException("'serviceInterface' must be an interface");
		}
		this.serviceInterface = serviceInterface;
	}

	/**
	 * Return the interface of the service to export.
	 */
	public Class getServiceInterface() {
		return this.serviceInterface;
	}

	/**
	 * Set whether to register a RemoteInvocationTraceInterceptor for exported
	 * services. Only applied when a subclass uses <code>getProxyForService</code>
	 * for creating the proxy to expose.
	 * <p>Default is "true". RemoteInvocationTraceInterceptor's most important value
	 * is that it logs exception stacktraces on the server, before propagating
	 * an exception to the client.
	 * @see #getProxyForService
	 * @see RemoteInvocationTraceInterceptor
	 */
	public void setRegisterTraceInterceptor(boolean registerTraceInterceptor) {
		this.registerTraceInterceptor = registerTraceInterceptor;
	}

	/**
	 * Return whether to register a RemoteInvocationTraceInterceptor for
	 * exported services.
	 */
	protected boolean isRegisterTraceInterceptor() {
		return this.registerTraceInterceptor;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	/**
	 * Check whether the service reference has been set.
	 * @see #setService
	 */
	protected void checkService() throws IllegalArgumentException {
		if (this.service == null) {
			throw new IllegalArgumentException("Property 'service' is required");
		}
	}

	/**
	 * Check whether a service reference has been set,
	 * and whether it matches the specified service.
	 * @see #setServiceInterface
	 * @see #setService
	 */
	protected void checkServiceInterface() throws IllegalArgumentException {
		if (this.serviceInterface == null) {
			throw new IllegalArgumentException("Property 'serviceInterface' is required");
		}
		if (this.service instanceof String) {
			throw new IllegalArgumentException("Service [" + this.service + "] is a String " +
					"rather than an actual service reference: Have you accidentally specified " +
					"the service bean name as value instead of as reference?");
		}
		if (!this.serviceInterface.isInstance(this.service)) {
			throw new IllegalArgumentException(
					"Service interface [" + this.serviceInterface.getName() +
					"] needs to be implemented by service [" + this.service +
					"] of class [" + this.service.getClass().getName() + "]");
		}
	}

	/**
	 * Get a proxy for the given service object, implementing the specified
	 * service interface.
	 * <p>Used to export a proxy that does not expose any internals but just
	 * a specific interface intended for remote access. Furthermore, a
	 * {@link RemoteInvocationTraceInterceptor} will be registered (by default).
	 * @return the proxy
	 * @see #setServiceInterface
	 * @see #setRegisterTraceInterceptor
	 * @see RemoteInvocationTraceInterceptor
	 */
	protected Object getProxyForService() {
		checkService();
		checkServiceInterface();
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.addInterface(getServiceInterface());
		if (isRegisterTraceInterceptor()) {
			proxyFactory.addAdvice(new RemoteInvocationTraceInterceptor(getExporterName()));
		}
		proxyFactory.setTarget(getService());
		return proxyFactory.getProxy(this.beanClassLoader);
	}

	/**
	 * Return a short name for this exporter.
	 * Used for tracing of remote invocations.
	 * <p>Default is the unqualified class name (without package).
	 * Can be overridden in subclasses.
	 * @see #getProxyForService
	 * @see RemoteInvocationTraceInterceptor
	 * @see org.springframework.util.ClassUtils#getShortName
	 */
	protected String getExporterName() {
		return ClassUtils.getShortName(getClass());
	}

}
