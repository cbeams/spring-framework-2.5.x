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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.jmx.support.ObjectNameManager;

/**
 * <code>MethodInterceptor</code> implementation that routes calls to an MBean
 * running on the supplied <code>MBeanServerConnection</code>. Works for both
 * local and remote <code>MBeanServerConnection</code>s.
 *
 * <p>By default, the <code>MBeanClientInterceptor</code> will connect to the
 * <code>MBeanServer</code> and cache MBean metadata at startup. This can
 * be undesirable when running against a remote <code>MBeanServer</code>
 * that may not be running when the application starts. Through setting the
 * {@link #setConnectOnStartup(boolean) connectOnStartup} property to "false",
 * you can defer this process until the first invocation against the proxy.
 *
 * <p>This functionality is usually used through <code>MBeanProxyFactoryBean</code>.
 * See the javadoc of that class for more information.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see MBeanProxyFactoryBean
 * @see #setConnectOnStartup
 * @since 1.2
 */
public class MBeanClientInterceptor implements MethodInterceptor, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The <code>MBeanServerConnection</code> hosting the MBean which calls are forwarded to.
	 */
	private MBeanServerConnection server;

	/**
	 * The <code>JMXServiceURL</code> of the remote <code>MBeanServer</code> to connect to.
	 * Optional.
	 * @see #setServiceUrl(String)
	 */
	private JMXServiceURL serviceUrl;

	/**
	 * Indicates whether the proxy should connect to the <code>MBeanServer</code> at startup.
	 * @see #setConnectOnStartup(boolean)
	 */
	private boolean connectOnStartup = true;

	/**
	 * The <code>ObjectName</code> of the MBean to forward calls to.
	 */
	private ObjectName objectName;

	/**
	 * Indicates whether or not strict casing is being used for attributes.
	 */
	private boolean useStrictCasing = true;


	/**
	 * The <code>JMXConnector</code> used when connecting to a remote JMX server using an
	 * internally configured connection.
	 * @see #setServiceUrl(String)
	 */
	private JMXConnector connector;

	/**
	 * Caches the list of attributes exposed on the management interface of
	 * the managed resouce.
	 */
	private Map allowedAttributes;

	/**
	 * Caches the list of operations exposed on the management interface of
	 * the managed resouce.
	 */
	private Map allowedOperations;

	/**
	 * Caches method signatures for use during invocation.
	 */
	private final Map signatureCache = new HashMap();


	/**
	 * Set the <code>MBeanServerConnection</code> used to connect to the
	 * MBean which all invocations are routed to.
	 */
	public void setServer(MBeanServerConnection server) {
		this.server = server;
	}

	/**
	 * Set the service URL of the remote <code>MBeanServer</code>.
	 */
	public void setServiceUrl(String url) throws MalformedURLException {
		this.serviceUrl = new JMXServiceURL(url);
	}

	/**
	 * Set whether or not the proxy should connect to the <code>MBeanServer</code>
	 * at creation time ("true") or the first time it is invoked ("false").
	 * Default is "true".
	 */
	public void setConnectOnStartup(boolean connectOnStartup) {
		this.connectOnStartup = connectOnStartup;
	}

	/**
	 * Set the <code>ObjectName</code> of the MBean which calls are
	 * routed to.
	 */
	public void setObjectName(String objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}

	/**
	 * Set whether to use strict casing for attributes. Enabled by default.
	 * <p>When using strict casing, a JavaBean property with a getter such as
	 * <code>getFoo()</code> translates to an attribute called <code>Foo</code>.
	 * With strict casing disabled, <code>getFoo()</code> would translate to just
	 * <code>foo</code>.
	 */
	public void setUseStrictCasing(boolean useStrictCasing) {
		this.useStrictCasing = useStrictCasing;
	}


	/**
	 * Ensures that an <code>MBeanServerConnection</code> is configured and attempts to
	 * detect a local connection if one is not supplied.
	 */
	public void afterPropertiesSet() throws MBeanServerNotFoundException, MBeanInfoRetrievalException {
		if (this.connectOnStartup) {
			if (this.server == null) {
				connect();
			}
			retrieveMBeanInfo();
		}
	}

	/**
	 * Connects to the remote <code>MBeanServer</code> using the configured <code>JMXServiceURL</code>.
	 * @see #setServiceUrl(String)
	 * @see #setConnectOnStartup(boolean)
	 */
	private void connect() throws MBeanServerNotFoundException {
		if (this.serviceUrl != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Connecting to remote MBeanServer at URL [" + this.serviceUrl + "]");
			}
			try {
				this.connector = JMXConnectorFactory.connect(this.serviceUrl);
				this.server = this.connector.getMBeanServerConnection();
			}
			catch (IOException ex) {
				throw new MBeanServerNotFoundException(
						"Could not connect to remote MBeanServer at URL [" + this.serviceUrl + "]", ex);
			}
		}
		else {
			logger.debug("Attempting to locate local MBeanServer");
			this.server = JmxUtils.locateMBeanServer();
		}
	}

	/**
	 * Loads the management interface info for the configured MBean into the caches.
	 * This information is used by the proxy when determining whether an invocation matches
	 * a valid operation or attribute on the management interface of the managed resource.
	 */
	private void retrieveMBeanInfo() throws MBeanServerNotFoundException, MBeanInfoRetrievalException {
		try {
			MBeanInfo info = this.server.getMBeanInfo(this.objectName);

			// get attributes
			MBeanAttributeInfo[] attributeInfo = info.getAttributes();
			this.allowedAttributes = new HashMap(attributeInfo.length);

			for (int x = 0; x < attributeInfo.length; x++) {
				this.allowedAttributes.put(attributeInfo[x].getName(), attributeInfo[x]);
			}

			// get operations
			MBeanOperationInfo[] operationInfo = info.getOperations();
			this.allowedOperations = new HashMap(operationInfo.length);

			for (int x = 0; x < operationInfo.length; x++) {
				MBeanOperationInfo opInfo = operationInfo[x];
				this.allowedOperations.put(
						new MethodCacheKey(
								opInfo.getName(), JmxUtils.parameterInfoToTypes(opInfo.getSignature())), opInfo);
			}
		}
		catch (ClassNotFoundException ex) {
			throw new MBeanInfoRetrievalException("Unable to locate class specified in method signature", ex);
		}
		catch (IntrospectionException ex) {
			throw new MBeanInfoRetrievalException("Unable to obtain MBean info for bean [" + this.objectName + "]", ex);
		}
		catch (InstanceNotFoundException ex) {
			// if we are this far this shouldn't happen, but...
			throw new MBeanInfoRetrievalException("Unable to obtain MBean info for bean [" + this.objectName +
					"]: it is likely that this bean was unregistered during the proxy creation process",
					ex);
		}
		catch (ReflectionException ex) {
			throw new MBeanInfoRetrievalException("Unable to read MBean info for bean [ " + this.objectName + "]", ex);
		}
		catch (IOException ex) {
			throw new MBeanInfoRetrievalException(
					"An IOException occurred when communicating with the MBeanServer. " +
							"It is likely that you are communicating with a remote MBeanServer. " +
							"Check the inner exception for exact details.", ex);
		}
	}


	/**
	 * Route the invocation to the configured managed resource. Correctly routes JavaBean property
	 * access to <code>MBeanServerConnection.get/setAttribute</code> and method invocation to
	 * <code>MBeanServerConnection.invoke</code>. Any attempt to invoke a method that does not
	 * correspond to an attribute or operation defined in the management interface of the managed
	 * resource results in an <code>InvalidInvocationException</code>.
	 * @param invocation the <code>MethodInvocation</code> to re-route.
	 * @return the value returned as a result of the re-routed invocation.
	 * @throws InvocationFailureException if the invocation does not match an attribute or
	 * operation on the management interface of the resource.
	 * @throws Throwable typically as the result of an error during invocation
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Lazily connect to MBeanServer?
		if (!this.connectOnStartup) {
			synchronized (this) {
				if (this.server == null) {
					logger.debug("Lazily establishing MBeanServer connection");
					connect();
				}

				if (this.allowedAttributes == null) {
					logger.debug("Lazily initializing MBeanInfo cache");
					retrieveMBeanInfo();
				}
			}
		}

		try {
			PropertyDescriptor pd = BeanUtils.findPropertyForMethod(invocation.getMethod());
			if (pd != null) {
				return invokeAttribute(pd, invocation);
			}
			else {
				return invokeOperation(invocation.getMethod(), invocation.getArguments());
			}
		}
		catch (JMException ex) {
			throw new InvocationFailureException("JMX access failed", ex);
		}
		catch (IOException ex) {
			throw new InvocationFailureException("JMX access failed", ex);
		}
	}

	private Object invokeAttribute(PropertyDescriptor pd, MethodInvocation invocation)
			throws JMException, IOException {

		String attributeName = JmxUtils.getAttributeName(pd, this.useStrictCasing);
		MBeanAttributeInfo inf = (MBeanAttributeInfo) this.allowedAttributes.get(attributeName);

		// If no attribute is returned, we know that it is not defined in the
		// management interface.
		if (inf == null) {
			throw new InvalidInvocationException(
					"Attribute '" + pd.getName() + "' is not exposed on the management interface");
		}
		if (invocation.getMethod().equals(pd.getReadMethod())) {
			if (inf.isReadable()) {
				return this.server.getAttribute(this.objectName, attributeName);
			}
			else {
				throw new InvalidInvocationException("Attribute '" + attributeName + "' is not readable");
			}
		}
		else if (invocation.getMethod().equals(pd.getWriteMethod())) {
			if (inf.isWritable()) {
				server.setAttribute(this.objectName, new Attribute(attributeName, invocation.getArguments()[0]));
				return null;
			}
			else {
				throw new InvalidInvocationException("Attribute '" + attributeName + "' is not writable");
			}
		}
		else {
			throw new IllegalStateException(
					"Method [" + invocation.getMethod() + "] is neither a bean property getter nor a setter");
		}
	}

	/**
	 * Routes a method invocation (not a property get/set) to the corresponding
	 * operation on the managed resource.
	 * @param method the method corresponding to operation on the managed resource.
	 * @param args the invocation arguments
	 * @return the value returned by the method invocation.
	 */
	private Object invokeOperation(Method method, Object[] args) throws JMException, IOException {
		MethodCacheKey key = new MethodCacheKey(method.getName(), method.getParameterTypes());
		MBeanOperationInfo info = (MBeanOperationInfo) this.allowedOperations.get(key);
		if (info == null) {
			throw new InvalidInvocationException("Operation '" + method.getName() +
					"' is not exposed on the management interface");
		}

		String[] signature = null;
		synchronized (this.signatureCache) {
			signature = (String[]) this.signatureCache.get(method);
			if (signature == null) {
				signature = JmxUtils.getMethodSignature(method);
				this.signatureCache.put(method, signature);
			}
		}
		return this.server.invoke(this.objectName, method.getName(), args, signature);
	}

	/**
	 * Closes any <code>JMXConnector</code> that may be managed by this interceptor.
	 */
	public void destroy() throws Exception {
		if (this.connector != null) {
			this.connector.close();
		}
	}


	/**
	 * Simple wrapper class around a method name and its signature.
	 * Used as the key when caching methods.
	 */
	private static class MethodCacheKey {

		/**
		 * the name of the method
		 */
		private final String name;

		/**
		 * the arguments in the method signature.
		 */
		private final Class[] parameters;

		/**
		 * Create a new instance of <code>MethodCacheKey</code> with the supplied
		 * method name and parameter list.
		 *
		 * @param name the name of the method.
		 * @param parameters the arguments in the method signature.
		 */
		public MethodCacheKey(String name, Class[] parameters) {
			this.name = name;
			if (parameters == null) {
				this.parameters = new Class[]{};
			}
			else {
				this.parameters = parameters;
			}
		}

		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other == this) {
				return true;
			}
			MethodCacheKey otherKey = null;
			if (other instanceof MethodCacheKey) {
				otherKey = (MethodCacheKey) other;
				return this.name.equals(otherKey.name) && Arrays.equals(this.parameters, otherKey.parameters);
			}
			else {
				return false;
			}
		}

		public int hashCode() {
			return this.name.hashCode();
		}
	}

}
