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

package org.springframework.remoting.jaxrpc;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.support.AopUtils;
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

	private boolean lookupServiceOnStartup = true;

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
	 * Set the username to specify on the stub or call.
	 * @see javax.xml.rpc.Stub#USERNAME_PROPERTY
	 * @see javax.xml.rpc.Call#USERNAME_PROPERTY
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Return the username to specify on the stub or call.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the password to specify on the stub or call.
	 * @see javax.xml.rpc.Stub#PASSWORD_PROPERTY
	 * @see javax.xml.rpc.Call#PASSWORD_PROPERTY
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return the password to specify on the stub or call.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the endpoint address to specify on the stub or call.
	 * @see javax.xml.rpc.Stub#ENDPOINT_ADDRESS_PROPERTY
	 * @see javax.xml.rpc.Call#setTargetEndpointAddress
	 */
	public void setEndpointAddress(String endpointAddress) {
		this.endpointAddress = endpointAddress;
	}

	/**
	 * Return the endpoint address to specify on the stub or call.
	 */
	public String getEndpointAddress() {
		return endpointAddress;
	}

	/**
	 * Set the maintain session flag to specify on the stub or call.
	 * @see javax.xml.rpc.Stub#SESSION_MAINTAIN_PROPERTY
	 * @see javax.xml.rpc.Call#SESSION_MAINTAIN_PROPERTY
	 */
	public void setMaintainSession(boolean maintainSession) {
		this.maintainSession = maintainSession;
	}

	/**
	 * Return the maintain session flag to specify on the stub or call.
	 */
	public boolean isMaintainSession() {
		return maintainSession;
	}

	/**
	 * Set custom properties to be set on the stub or call.
	 * @see javax.xml.rpc.Stub#_setProperty
	 * @see javax.xml.rpc.Call#setProperty
	 */
	public void setCustomProperties(Properties customProperties) {
		this.customProperties = customProperties;
	}

	/**
	 * Return custom properties to be set on the stub or call.
	 */
	public Properties getCustomProperties() {
		return customProperties;
	}

	/**
	 * Set the interface of the service that this factory should create a proxy for.
	 * This will typically be a non-RMI business interface, although you can also
	 * use an RMI port interface as recommended by JAX-RPC here.
	 * <p>If the specified service interface is a non-RMI business interface,
	 * invocations will either be translated to the underlying RMI port interface
	 * (in case of a "portInterface" being specified) or to JAX-RPC dynamic calls.
	 * <p>The dynamic call mechanism has the advantage that you don't need to
	 * maintain an RMI port interface in addition to an existing non-RMI business
	 * interface. In terms of configuration, specifying the business interface
	 * as "serviceInterface" will be enough; this interceptor will automatically
	 * switch to dynamic calls in such a scenario.
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
	 * a non-RMI business interface as service interface for exposed proxies,
	 * and if the JAX-RPC dynamic call mechanism is not desirable. See the
	 * javadoc of the "serviceInterface" property for more details.
	 * <p>The interface must be suitable for a JAX-RPC port, i.e. it must be an
	 * RMI service interface (that extends <code>java.rmi.Remote</code>).
	 * @see #setServiceInterface
	 * @see java.rmi.Remote
	 */
	public void setPortInterface(Class portInterface) {
		if (portInterface != null &&
				(!portInterface.isInterface() || !Remote.class.isAssignableFrom(portInterface))) {
			throw new IllegalArgumentException(
					"portInterface must be an interface derived from [java.rmi.Remote]");
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
	 * Set whether to look up the JAX-RPC service on startup. Default is "true".
	 * <p>Can be turned off to allow for late start of the target server.
	 * In this case, the JAX-RPC service will be fetched on first access.
	 */
	public void setLookupServiceOnStartup(boolean lookupServiceOnStartup) {
		this.lookupServiceOnStartup = lookupServiceOnStartup;
	}


	/**
	 * Prepares the JAX-RPC service and port if the "lookupServiceOnStartup"
	 * is turned on (which it is by default).
	 */
	public void afterPropertiesSet() throws ServiceException {
		if (this.lookupServiceOnStartup) {
			prepare();
		}
	}

	/**
	 * Create and initialize the JAX-RPC service for the specified port.
	 * <p>Prepares a JAX-RPC stub if possible (if an RMI interface is available);
	 * falls back to JAX-RPC dynamic calls else. Using dynamic calls can be
	 * enforced through overriding <code>alwaysUseJaxRpcCall</code> to return true.
	 * <p><code>postProcessJaxRpcService</code> and <code>postProcessPortStub</code>
	 * hooks are available for customization in subclasses. When using dynamic calls,
	 * each can be post-processed via <code>postProcessJaxRpcCall</code>.
	 * @see #alwaysUseJaxRpcCall
	 * @see #postProcessJaxRpcService
	 * @see #postProcessPortStub
	 * @see #postProcessJaxRpcCall
	 */
	public void prepare() throws ServiceException {
		if (this.portName == null) {
			throw new IllegalArgumentException("portName is required");
		}

		// Cache the QName for the port.
		this.portQName = getQName(this.portName);

		if (this.jaxRpcService == null) {
			this.jaxRpcService = createJaxRpcService();
		}
		else {
			postProcessJaxRpcService(this.jaxRpcService);
		}

		// Determine interface to use at the JAX-RPC port level:
		// Use portInterface if specified, else fall back to serviceInterface.
		Class actualInterface = (this.portInterface != null ? this.portInterface : this.serviceInterface);

		if (actualInterface != null && Remote.class.isAssignableFrom(actualInterface) &&
				!alwaysUseJaxRpcCall()) {
			// JAX-RPC-compliant port interface -> using JAX-RPC stub for port.

			if (logger.isInfoEnabled()) {
				logger.info("Creating JAX-RPC proxy for JAX-RPC port [" + this.portQName +
						"], using port interface [" + actualInterface.getName() + "]");
			}
			Remote remoteObj = this.jaxRpcService.getPort(this.portQName, actualInterface);

			if (logger.isInfoEnabled()) {
				if (this.serviceInterface != null) {
					boolean isImpl = this.serviceInterface.isInstance(remoteObj);
					logger.info("Using service interface [" + this.serviceInterface.getName() + "] for JAX-RPC port [" +
							this.portQName + "] - " + (!isImpl ? "not" : "") + " directly implemented");
				}
			}

			if (!(remoteObj instanceof Stub)) {
				throw new ServiceException("Port stub of class [" + remoteObj.getClass().getName() +
						"] is not a valid JAX-RPC stub: it does not implement interface [javax.xml.rpc.Stub]");
			}
			Stub stub = (Stub) remoteObj;

			// Apply properties to JAX-RPC stub.
			preparePortStub(stub);

			// Allow for custom post-processing in subclasses.
			postProcessPortStub(stub);

			this.portStub = remoteObj;
		}

		else {
			// No JAX-RPC-compliant port interface -> using JAX-RPC dynamic calls.
			if (logger.isInfoEnabled()) {
				logger.info("Using JAX-RPC dynamic calls for JAX-RPC port [" + this.portQName + "]");
			}
		}
	}

	/**
	 * Return whether this client interceptor has already been prepared,
	 * i.e. has already looked up the JAX-RPC service and port.
	 */
	protected boolean isPrepared() {
		return (this.portQName != null);
	}

	/**
	 * Return the prepared QName for the port.
	 * @see #setPortName
	 * @see #getQName
	 */
	protected QName getPortQName() {
		return portQName;
	}

	/**
	 * Return whether to always use JAX-RPC dynamic calls.
	 * Called by <code>afterPropertiesSet</code>.
	 * <p>Default is "false"; if an RMI interface is specified as "portInterface"
	 * or "serviceInterface", it will be used to create a JAX-RPC port stub.
	 * <p>Can be overridden to enforce the use of the JAX-RPC Call API,
	 * for example if there is a need to customize at the Call level.
	 * This just necessary if you you want to use an RMI interface as
	 * "serviceInterface", though; in case of only a non-RMI interface being
	 * available, this interceptor will fall back to the Call API anyway.
	 * @see #postProcessJaxRpcCall
	 */
	protected boolean alwaysUseJaxRpcCall() {
		return false;
	}


	/**
	 * Prepare the given JAX-RPC port stub, applying properties to it.
	 * Called by <code>afterPropertiesSet</code>.
	 * <p>Just applied when actually creating a JAX-RPC port stub,
	 * in case of a specified JAX-RPC-compliant port interface.
	 * Else, JAX-RPC dynamic calls will be used.
	 * @param stub the current JAX-RPC port stub
	 * @see #afterPropertiesSet
	 * @see #setUsername
	 * @see #setPassword
	 * @see #setEndpointAddress
	 * @see #setMaintainSession
	 * @see #setCustomProperties
	 * @see #setPortInterface
	 * @see #prepareJaxRpcCall
	 */
	protected void preparePortStub(Stub stub) {
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
			stub._setProperty(Stub.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
		}
		if (this.customProperties != null) {
			Enumeration en = this.customProperties.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				stub._setProperty(key, this.customProperties.getProperty(key));
			}
		}
	}

	/**
	 * Post-process the given JAX-RPC port stub.
	 * Default implementation is empty. Called by <code>prepare</code>.
	 * <p>Just applied when actually creating a JAX-RPC port stub,
	 * in case of a specified JAX-RPC-compliant port interface.
	 * Else, JAX-RPC dynamic calls will be used.
	 * @param stub the current JAX-RPC port stub
	 * (can be cast to an implementation-specific class if necessary)
	 * @see #prepare
	 * @see #setPortInterface
	 * @see #postProcessJaxRpcCall
	 */
	protected void postProcessPortStub(Stub stub) {
	}

	/**
	 * Return the underlying JAX-RPC port stub that this interceptor delegates to
	 * for each method invocation on the proxy.
	 */
	protected Remote getPortStub() {
		return portStub;
	}


	/**
	 * Translates the method invocation into a JAX-RPC service invocation.
	 * Uses traditional RMI stub invocation if a JAX-RPC port stub is available;
	 * falls back to JAX-RPC dynamic calls else.
	 * @see #getPortStub()
	 * @see org.springframework.remoting.rmi.RmiClientInterceptorUtils
	 * @see #performJaxRpcCall
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (AopUtils.isToStringMethod(invocation.getMethod())) {
			return "JAX-RPC proxy for port [" + getPortName() + "] of service [" + getServiceName() + "]";
		}

		// Lazily prepare service and stub if appropriate.
		if (!this.lookupServiceOnStartup) {
			synchronized (this) {
				if (!isPrepared()) {
					try {
						prepare();
					}
					catch (ServiceException ex) {
						throw RmiClientInterceptorUtils.convertRmiAccessException(
								invocation.getMethod(), ex, this.portName);
					}
				}
			}
		}
		else {
			if (!isPrepared()) {
				throw new IllegalStateException("JaxRpcClientInterceptor is not properly initialized - " +
						"invoke 'prepare' before attempting any operations");
			}
		}

		Remote stub = getPortStub();
		if (stub != null) {
			// JAX-RPC port stub available -> traditional RMI stub invocation.
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking operation '" + invocation.getMethod().getName() +
						"' on JAX-RPC port stub");
			}
			return RmiClientInterceptorUtils.invoke(invocation, stub, getPortQName().toString());
		}

		else {
			// No JAX-RPC stub -> using JAX-RPC dynamic calls.
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking operation '" + invocation.getMethod().getName() +
						"' as JAX-RPC dynamic call");
			}
			return performJaxRpcCall(invocation);
		}
	}


	/**
	 * Perform a JAX-RPC dynamic call for the given AOP method invocation.
	 * Delegates to <code>prepareJaxRpcCall</code> and
	 * <code>postProcessJaxRpcCall</code> for setting up the call object.
	 * <p>Default implementation uses method name as JAX-RPC operation name
	 * and method arguments as arguments for the JAX-RPC call. Can be
	 * overridden in subclasses for custom operation names and/or arguments.
	 * @param invocation the current AOP MethodInvocation that should
	 * be converted to a JAX-RPC call
	 * @return the return value of the invocation, if any
	 * @throws Throwable the exception thrown by the invocation, if any
	 * @see #getJaxRpcService
	 * @see #getPortQName
	 * @see #prepareJaxRpcCall
	 * @see #postProcessJaxRpcCall
	 */
	protected Object performJaxRpcCall(MethodInvocation invocation) throws Throwable {
		Service service = getJaxRpcService();
		QName portQName = getPortQName();

		// Create JAX-RPC call object, using the method name as operation name.
		// Synchronized because of non-thread-safe Axis implementation!
		Call call = null;
		synchronized (service) {
			call = service.createCall(portQName, invocation.getMethod().getName());
		}

		// Apply properties to JAX-RPC stub.
		prepareJaxRpcCall(call);

		// Allow for custom post-processing in subclasses.
		postProcessJaxRpcCall(call, invocation);

		// Perform actual invocation.
		try {
			return call.invoke(invocation.getArguments());
		}
		catch (RemoteException ex) {
			throw RmiClientInterceptorUtils.convertRmiAccessException(
					invocation.getMethod(), ex, portQName.toString());
		}
	}

	/**
	 * Prepare the given JAX-RPC call, applying properties to it.
	 * Called by <code>invoke</code>.
	 * <p>Just applied when actually using JAX-RPC dynamic calls,
	 * i.e. if no JAX-RPC-compliant port interface was specified.
	 * Else, a JAX-RPC port stub will be used.
	 * @param call the current JAX-RPC call object
	 * @see #invoke
	 * @see #setUsername
	 * @see #setPassword
	 * @see #setEndpointAddress
	 * @see #setMaintainSession
	 * @see #setCustomProperties
	 * @see #setPortInterface
	 * @see #preparePortStub
	 */
	protected void prepareJaxRpcCall(Call call) {
		if (this.username != null) {
			call.setProperty(Call.USERNAME_PROPERTY, this.username);
		}
		if (this.password != null) {
			call.setProperty(Call.PASSWORD_PROPERTY, this.password);
		}
		if (this.endpointAddress != null) {
			call.setTargetEndpointAddress(this.endpointAddress);
		}
		if (this.maintainSession) {
			call.setProperty(Call.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
		}
		if (this.customProperties != null) {
			Enumeration en = this.customProperties.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				call.setProperty(key, this.customProperties.getProperty(key));
			}
		}
	}

	/**
	 * Post-process the given JAX-RPC call.
	 * Default implementation is empty. Called by <code>invoke</code>.
	 * <p>Just applied when actually using JAX-RPC dynamic calls,
	 * i.e. if no JAX-RPC-compliant port interface was specified.
	 * Else, a JAX-RPC port stub will be used.
	 * @param call the current JAX-RPC call object
	 * (can be cast to an implementation-specific class if necessary)
	 * @param invocation the current AOP MethodInvocation that the call was
	 * created for (can be used to check method name, method parameters
	 * and/or passed-in arguments)
	 * @see #invoke
	 * @see #setPortInterface
	 * @see #postProcessPortStub
	 */
	protected void postProcessJaxRpcCall(Call call, MethodInvocation invocation) {
	}

}
