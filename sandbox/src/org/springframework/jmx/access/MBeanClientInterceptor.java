/*
 * Copyright 2002-2004 the original author or authors.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.util.JmxUtils;
import org.springframework.jmx.util.ObjectNameManager;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class MBeanClientInterceptor implements MethodInterceptor, InitializingBean {

	private MBeanServerConnection server;

	private ObjectName objectName;

	private Map allowedAttributes;

	private Map allowedOperations;

	private Map signatureCache = new HashMap();


	public void setServer(MBeanServerConnection server) {
		this.server = server;
	}

	public void setObjectName(String objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}


	public void afterPropertiesSet() throws Exception {
		// No server specified - locate.
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}
		// No server found - error.
		if (this.server == null) {
			throw new IllegalArgumentException(
					"'server' property is required when not running in an environment with an existing MBeanServer instance");
		}
		queryMetadata();
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(invocation.getMethod());
		if (pd != null) {
			MBeanAttributeInfo inf = (MBeanAttributeInfo) allowedAttributes.get(pd.getName());
			// If no attribute is returned, we know that it is not defined in the
			// management interface.
			if (inf == null) {
				throw new InvalidInvocationException(
						"Attribute '" + pd.getName() + "' is not exposed on the management interface");
			}
			if (invocation.getMethod().equals(pd.getReadMethod())) {
				if (inf.isReadable()) {
					return server.getAttribute(objectName, pd.getName());
				}
				else {
					throw new InvalidInvocationException("Attribute '" + pd.getName() + "' is not readable");
				}
			}
			else if (invocation.getMethod().equals(pd.getWriteMethod())) {
				if (inf.isWritable()) {
					server.setAttribute(objectName, new Attribute(pd.getName(), invocation.getArguments()[0]));
					return null;
				}
				else {
					throw new InvalidInvocationException("Attribute '" + pd.getName() + "' is not writable");
				}
			}
			else {
				throw new IllegalStateException(
						"Method '" + invocation.getMethod() + "' is neither a bean property getter nor a setter");
			}
		}
		else {
			return invokeMethod(invocation.getMethod(), invocation.getArguments());
		}
	}

	private Object invokeMethod(Method method, Object[] args) throws Exception {
		InternalMethodCacheKey key = new InternalMethodCacheKey(method.getName(), method.getParameterTypes());
		MBeanOperationInfo info = (MBeanOperationInfo) allowedOperations.get(key);
		if (info == null) {
			throw new InvalidInvocationException("Operation '" + method.getName() +
					"' is not exposed on the management interface");
		}
		else {
			String[] signature = (String[]) signatureCache.get(method);
			if (signature == null) {
				signature = JmxUtils.getMethodSignature(method);
				synchronized (signatureCache) {
					signatureCache.put(method, signature);
				}
			}
			return server.invoke(objectName, method.getName(), args, signature);
		}
	}

	private void queryMetadata() {
		try {
			MBeanInfo inf = server.getMBeanInfo(objectName);

			// get attributes
			MBeanAttributeInfo[] attributeInfo = inf.getAttributes();
			allowedAttributes = new HashMap(attributeInfo.length);

			for (int x = 0; x < attributeInfo.length; x++) {
				allowedAttributes.put(attributeInfo[x].getName(), attributeInfo[x]);
			}

			// get operations
			MBeanOperationInfo[] operationInfo = inf.getOperations();
			allowedOperations = new HashMap(operationInfo.length);

			try {
				for (int x = 0; x < operationInfo.length; x++) {
					MBeanOperationInfo opInfo = operationInfo[x];

					allowedOperations.put(new InternalMethodCacheKey(
							opInfo.getName(), JmxUtils.parameterInfoToTypes(opInfo.getSignature())), opInfo);
				}
			}
			catch (ClassNotFoundException ex) {
				throw new MetadataRetrievalException("Unable to locate class specified in method signature", ex);
			}

		}
		catch (IntrospectionException ex) {
			throw new MetadataRetrievalException("Unable to obtain MBean metadata for bean: " + objectName, ex);
		}
		catch (InstanceNotFoundException ex) {
			// if we are this far this shouldn't happen, but...
			throw new MetadataRetrievalException(
					"Unable to obtain MBean metadata for bean: " + objectName +
					". It is likely that this bean was unregistered during the proxy creation process.",
					ex);
		}
		catch (ReflectionException ex) {
			throw new MetadataRetrievalException(
					"Unable to read MBean metadata for bean: " + objectName, ex);
		}
		catch (IOException ex) {
			throw new MetadataRetrievalException(
					"An IOException occurred when communicating with the MBeanServer. " +
					"It is likely that you are communicating with a remote MBeanServer. " +
					"Check the inner exception for exact details.", ex);
		}
	}


	private static class InternalMethodCacheKey {

		private final String name;

		private final Class[] params;

		public InternalMethodCacheKey(String name, Class[] params) {
			this.name = name;
			if (params == null) {
				this.params = new Class[] {};
			}
			else {
				this.params = params;
			}
		}

		public int hashCode() {
			return name.hashCode();
		}

		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other == this) {
				return true;
			}

			InternalMethodCacheKey otherKey = null;
			if (other instanceof InternalMethodCacheKey) {
				otherKey = (InternalMethodCacheKey) other;
				return name.equals(otherKey.name)
						&& Arrays.equals(params, otherKey.params);
			}
			else {
				return false;
			}
		}
	}

}
