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

package org.springframework.remoting.jaxrpc;

import java.rmi.Remote;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.rmi.RmiClientInterceptorUtils;

/**
 * Interceptor for accessing a specific port of a JAX-RPC service.
 * Uses either LocalJaxRpcServiceFactory's facilities underneath,
 * or takes an explicit reference to an existing JAX-RPC Service instance
 * (for example looked up via JndiObjectFactoryBean).
 *
 * <p>Allows to set JAX-RPC's standard stub properties directly, via the
 * "username", "password", "endpointAddress" and "maintainSession" properties.
 * For typical usage, it is not necessary to specify those, though.
 *
 * <p>This invoker is typically used with an RMI service interface. Alternatively,
 * this invoker can also proxy a JAX-RPC service with a matching non-RMI business
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
 * @see #setPortName
 * @see #setServiceInterface
 * @see #setPortInterface
 * @see javax.xml.rpc.Service#getPort
 * @see javax.xml.rpc.Stub
 * @see org.springframework.remoting.RemoteAccessException
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public class JaxRpcPortClientInterceptor extends LocalJaxRpcServiceFactory
		implements MethodInterceptor, InitializingBean {

	private Service jaxRpcService;

	private String portName;

	private String username;

	private String password;

	private String endpointAddress;

	private boolean maintainSession;

	private Properties customProperties;

	private Class serviceInterface;

	private Class portInterface;

	private QName portQName;

	private Remote portStub;


	/**
	 * Set a reference to an existing JAX-RPC Service instance,
	 * for example looked up via JndiObjectFactoryBean.
	 * If not set, LocalJaxRpcServiceFactory's properties have to be specified.
	 * @see #setServiceFactoryClass
	 * @see #setWsdlDocumentUrl
	 * @see #setNamespaceUri
	 * @see #setServiceName
	 * @see org.springframework.jndi.JndiObjectFactoryBean
	 */
	public void setJaxRpcService(Service jaxRpcService) {
		this.jaxRpcService = jaxRpcService;
	}

	/**
	 * Return a reference to an existing JAX-RPC Service instance, if any.
	 */
	public Service getJaxRpcService() {
		return jaxRpcService;
	}

	/**
	 * Set the name of the port.
	 * Corresponds to the "wsdl:port" name.
	 */
	public void setPortName(String portName) {
		this.portName = portName;
	}

	/**
	 * Return the name of the port.
	 */
	public String getPortName() {
		return portName;
	}

	/**
	 * Set the username to specify on the stub.
	 * @see javax.xml.rpc.Stub#USERNAME_PROPERTY
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Return the username to specify on the stub.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the password to specify on the stub.
	 * @see javax.xml.rpc.Stub#PASSWORD_PROPERTY
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return the password to specify on the stub.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the endpoint address to specify on the stub.
	 * @see javax.xml.rpc.Stub#ENDPOINT_ADDRESS_PROPERTY
	 */
	public void setEndpointAddress(String endpointAddress) {
		this.endpointAddress = endpointAddress;
	}

	/**
	 * Return the endpoint address to specify on the stub.
	 */
	public String getEndpointAddress() {
		return endpointAddress;
	}

	/**
	 * Set the maintain session flag to specify on the stub.
	 * @see javax.xml.rpc.Stub#SESSION_MAINTAIN_PROPERTY
	 */
	public void setMaintainSession(boolean maintainSession) {
		this.maintainSession = maintainSession;
	}

	/**
	 * Return the maintain session flag to specify on the stub.
	 */
	public boolean isMaintainSession() {
		return maintainSession;
	}

	/**
	 * Set custom properties to be set on the stub for the port.
	 * @see javax.xml.rpc.Stub#_setProperty
	 */
	public void setCustomProperties(Properties customProperties) {
		this.customProperties = customProperties;
	}

	/**
	 * Return custom properties to be set on the stub for the port.
	 */
	public Properties getCustomProperties() {
		return customProperties;
	}

	/**
	 * Set the interface of the service that this factory should create a proxy for.
	 * Can be different from the JAX-RPC port interface, if using a non-RMI business
	 * interface for exposed proxies.
	 * <p>The interface must be suitable for a JAX-RPC port, if "portInterface"
	 * is not set. Else, it must match the methods in the port interface but can
	 * be a non-RMI business interface.
	 * @see #setPortInterface
	 */
	public void setServiceInterface(Class serviceInterface) {
		if (serviceInterface != null && !serviceInterface.isInterface()) {
			throw new IllegalArgumentException("serviceInterface must be an interface");
		}
		this.serviceInterface = serviceInterface;
	}

	/**
	 * Return the interface of the service that this factory should create a proxy for.
	 */
	public Class getServiceInterface() {
		return serviceInterface;
	}

	/**
	 * Set the JAX-RPC port interface to use. Only needs to be set if the exposed
	 * service interface is different from the port interface, i.e. when using
	 * a non-RMI business interface as service interface for exposed proxies.
	 * <p>The interface must be suitable for a JAX-RPC port, i.e. it must be an
	 * RMI service interface (that extends <code>java.rmi.Remote</code>).
	 * @see #setServiceInterface
	 * @see java.rmi.Remote
	 */
	public void setPortInterface(Class portInterface) {
		if (portInterface != null &&
				(!portInterface.isInterface() || !Remote.class.isAssignableFrom(portInterface))) {
			throw new IllegalArgumentException("portInterface must be an interface derived from java.rmi.Remote");
		}
		this.portInterface = portInterface;
	}

	/**
	 * Return the JAX-RPC port interface to use.
	 */
	public Class getPortInterface() {
		return portInterface;
	}


	/**
	 * Create and initialize the JAX-RPC stub for the specified port.
	 */
	public void afterPropertiesSet() throws ServiceException {
		if (this.portName == null) {
			throw new IllegalArgumentException("portName is required");
		}
		if (this.serviceInterface == null && this.portInterface == null) {
			throw new IllegalArgumentException("Either serviceInterface or portInterface is required");
		}

		if (this.jaxRpcService == null) {
			this.jaxRpcService = createJaxRpcService();
			postProcessJaxRpcService(this.jaxRpcService);
		}

		this.portQName = getQName(this.portName);
		Class actualInterface = (this.portInterface != null ? this.portInterface : this.serviceInterface);
		Remote remoteObj = this.jaxRpcService.getPort(this.portQName, actualInterface);

		if (this.serviceInterface != null) {
			boolean isImpl = this.serviceInterface.isInstance(remoteObj);
			if (logger.isInfoEnabled()) {
				logger.info("Using service interface [" + this.serviceInterface.getName() + "] for JAX-RPC object [" +
						this.portQName + "] - " + (!isImpl ? "not" : "") + " directly implemented");
			}
		}

		if (!(remoteObj instanceof Stub)) {
			throw new ServiceException("Returned port stub [" + remoteObj + "] does not implement javax.xml.rpc.Stub");
		}

		// apply properties to stub
		Stub stub = (Stub) remoteObj;
		if (this.username != null) {
			stub._setProperty(Stub.USERNAME_PROPERTY, this.username);
		}
		if (this.password != null) {
			stub._setProperty(Stub.PASSWORD_PROPERTY, this.password);
		}
		if (this.endpointAddress != null) {
			stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, this.endpointAddress);
		}
		if (this.maintainSession) {
			stub._setProperty(Stub.SESSION_MAINTAIN_PROPERTY, new Boolean(this.maintainSession));
		}
		if (this.customProperties != null) {
			Enumeration en = this.customProperties.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				stub._setProperty(key, this.customProperties.getProperty(key));
			}
		}

		postProcessPortStub(stub);
		this.portStub = remoteObj;
	}

	/**
	 * Post-process the given JAX-RPC Service. Called by afterPropertiesSet.
	 * Useful for example to register custom type mappings.
	 * <p>Just applied when creating a JAX-RPC Service instance locally,
	 * i.e. not applied when an existing Service reference is passed in.
	 * @param service the current JAX-RPC Service
	 * @see #afterPropertiesSet
	 * @see #setJaxRpcService
	 * @see javax.xml.rpc.Service#getTypeMappingRegistry
	 */
	protected void postProcessJaxRpcService(Service service) {
	}

	/**
	 * Post-process the given JAX-RPC port stub. Called by afterPropertiesSet.
	 * @param portStub the current JAX-RPC port stub
	 * @see #afterPropertiesSet
	 */
	protected void postProcessPortStub(Stub portStub) {
	}

	/**
	 * Return the underlying JAX-RPC port stub that this interceptor delegates to
	 * for each method invocation on the proxy.
	 */
	protected Remote getPortStub() {
		return portStub;
	}


	public Object invoke(MethodInvocation invocation) throws Throwable {
		// traditional RMI stub invocation
		return RmiClientInterceptorUtils.invoke(invocation, getPortStub(), this.portQName.toString());
	}

}
