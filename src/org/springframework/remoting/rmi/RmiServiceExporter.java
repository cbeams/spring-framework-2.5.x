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

package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.remoting.support.RemoteInvocation;

/**
 * RMI exporter that exposes the specified service as RMI object with the specified
 * name. Such services can be accessed via plain RMI or via RmiProxyFactoryBean.
 * Also supports exposing any non-RMI service via RMI invokers, to be accessed via
 * RmiClientInterceptor/RmiProxyFactoryBean's automatic detection of such invokers.
 *
 * <p>With an RMI invoker, RMI communication works on the RmiInvocationHandler
 * level, needing only one stub for any service. Service interfaces do not have to
 * extend java.rmi.Remote or throw RemoteException on all methods, but in and out
 * parameters have to be serializable.
 *
 * <p>The major advantage of RMI, compared to Hessian and Burlap, is serialization.
 * Effectively, any serializable Java object can be transported without hassle.
 * Hessian and Burlap have their own (de-)serialization mechanisms, but are
 * HTTP-based and thus much easier to setup than RMI. 
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see RmiClientInterceptor
 * @see RmiProxyFactoryBean
 * @see org.springframework.remoting.caucho.HessianServiceExporter
 * @see org.springframework.remoting.caucho.BurlapServiceExporter
 */
public class RmiServiceExporter extends RemoteExporter implements InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private String serviceName;

	private int servicePort = 0;  // anonymous port

	private int registryPort = Registry.REGISTRY_PORT;

	private RMIClientSocketFactory clientSocketFactory;

	private RMIServerSocketFactory serverSocketFactory;

	private Remote exportedObject;


	/**
	 * Set the name of the exported RMI service,
	 * i.e. rmi://localhost:port/NAME
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Set the port that the exported RMI service will use.
	 * Default is 0 (anonymous port).
	 */
	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	/**
	 * Set the port of the registry for the exported RMI service,
	 * i.e. rmi://localhost:PORT/name
	 * Default is Registry.REGISTRY_PORT (1099).
	 */
	public void setRegistryPort(int registryPort) {
		this.registryPort = registryPort;
	}

	/**
	 * Set a custom RMI client socket factory to use for exporting.
	 * If the given object also implement RMIServerSocketFactory,
	 * it will automatically be registered as server socket factory too.
	 * @see #setServerSocketFactory
	 * @see UnicastRemoteObject#exportObject(Remote, int, RMIClientSocketFactory, RMIServerSocketFactory)
	 */
	public void setClientSocketFactory(RMIClientSocketFactory clientSocketFactory) {
		this.clientSocketFactory = clientSocketFactory;
	}

	/**
	 * Set a custom RMI server socket factory to use for exporting.
	 * @see #setClientSocketFactory
	 */
	public void setServerSocketFactory(RMIServerSocketFactory serverSocketFactory) {
		this.serverSocketFactory = serverSocketFactory;
	}


	/**
	 * Register the service as RMI object.
	 * Creates an RMI registry on the specified port if none exists.
	 */
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		if (this.serviceName == null) {
			throw new IllegalArgumentException("serviceName is required");
		}
		if (this.clientSocketFactory instanceof RMIServerSocketFactory) {
			this.serverSocketFactory = (RMIServerSocketFactory) this.clientSocketFactory;
		}
		if ((this.clientSocketFactory != null && this.serverSocketFactory == null) ||
				(this.clientSocketFactory == null && this.serverSocketFactory != null)) {
			throw new IllegalArgumentException("Both RMIClientSocketFactory and RMIServerSocketFactory or none required");
		}

		Registry registry = null;
		logger.info("Looking for RMI registry at port '" + this.registryPort + "'");
		try {
			// retrieve registry
			registry = LocateRegistry.getRegistry(this.registryPort);
			registry.list();
		}
		catch (RemoteException ex) {
			logger.debug("RMI registry access threw exception", ex);
			logger.warn("Could not detect RMI registry - creating new one");
			// assume no registry found -> create new one
			registry = LocateRegistry.createRegistry(this.registryPort);
		}

		// determine remote object
		if (getService() instanceof Remote) {
			// conventional RMI service
			this.exportedObject = (Remote) getService();
		}
		else {
			// RMI invoker
			logger.info("RMI object '" + this.serviceName + "' is an RMI invoker");
			this.exportedObject = new RmiInvocationWrapper(getProxyForService(), this);
		}

		// export remote object and bind it to registry
		logger.info("Binding RMI service '" + this.serviceName + "' to registry at port '" + this.registryPort + "'");
		if (this.clientSocketFactory != null) {
			UnicastRemoteObject.exportObject(this.exportedObject, this.servicePort,
																			 this.clientSocketFactory, this.serverSocketFactory);
		}
		else {
			UnicastRemoteObject.exportObject(this.exportedObject, this.servicePort);
		}
		registry.rebind(this.serviceName, this.exportedObject);
	}

	/**
	 * Apply the given remote invocation to the given target object.
	 * The default implementation performs a plain method invocation.
	 * <p>Can be overridden in subclasses for custom invocation behavior,
	 * possibly for applying additional invocation parameters from a
	 * custom RemoteInvocation subclass. Will typically match a corresponding
	 * custom invoke implementation in RmiClientInterceptor/RmiProxyFactoryBean.
	 * @param invocation the remote invocation
	 * @param targetObject the target object to apply the invocation to
	 * @return the invocation result
	 * @throws NoSuchMethodException if the method name could not be resolved
	 * @throws IllegalAccessException if the method could not be accessed
	 * @throws InvocationTargetException if the method invocation resulted in an exception
	 * @see RmiClientInterceptor#invoke
	 */
	protected Object invoke(RemoteInvocation invocation, Object targetObject)
	    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method = targetObject.getClass().getMethod(invocation.getMethodName(),
		                                                  invocation.getParameterTypes());
		return method.invoke(targetObject, invocation.getArguments());
	}

	public void destroy() throws RemoteException, NotBoundException {
		logger.info("Unbinding RMI service '" + this.serviceName + "' from registry at port '" + this.registryPort + "'");
		Registry registry = LocateRegistry.getRegistry(this.registryPort);
		registry.unbind(this.serviceName);
		UnicastRemoteObject.unexportObject(this.exportedObject, true);
	}

}
