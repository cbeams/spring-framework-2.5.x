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
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;

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
 * HTTP-based and thus much easier to setup than RMI. Alternatively, consider
 * Spring's HTTP invoker to combine Java serialization with HTTP-based transport.
 *
 * <p>Note: RMI makes a best-effort attempt to obtain the fully qualified host name.
 * If one cannot be determined, it will fall back and use the IP address. Depending
 * on your network configuration, in some cases it will resolve the IP to the loopback
 * address. Ensuring that RMI will use the host name bound to the correct network
 * interface you should pass the <code>java.rmi.server.hostname</code> property to the
 * JVM that will export the registry and/or the service using the "-D" JVM argument.
 * For example: <code>-Djava.rmi.server.hostname=myserver.com</code>
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see RmiClientInterceptor
 * @see RmiProxyFactoryBean
 * @see java.rmi.Remote
 * @see java.rmi.RemoteException
 * @see org.springframework.remoting.caucho.HessianServiceExporter
 * @see org.springframework.remoting.caucho.BurlapServiceExporter
 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 */
public class RmiServiceExporter extends RemoteInvocationBasedExporter
		implements InitializingBean, DisposableBean {

	private String registryHost;

	private int registryPort = Registry.REGISTRY_PORT;

	private String serviceName;

	private int servicePort = 0;  // anonymous port

	private RMIClientSocketFactory clientSocketFactory;

	private RMIServerSocketFactory serverSocketFactory;

	private RMIClientSocketFactory registryClientSocketFactory;

	private RMIServerSocketFactory registryServerSocketFactory;

	private Remote exportedObject;


	/**
	 * Set the port of the registry for the exported RMI service,
	 * i.e. <code>rmi://HOST:port/name</code>
	 * <p>Default is localhost.
	 */
	public void setRegistryHost(String registryHost) {
		this.registryHost = registryHost;
	}

	/**
	 * Set the port of the registry for the exported RMI service,
	 * i.e. <code>rmi://host:PORT/name</code>
	 * <p>Default is Registry.REGISTRY_PORT (1099).
	 */
	public void setRegistryPort(int registryPort) {
		this.registryPort = registryPort;
	}

	/**
	 * Set the name of the exported RMI service,
	 * i.e. <code>rmi://host:port/NAME</code>
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Set the port that the exported RMI service will use.
	 * <p>Default is 0 (anonymous port).
	 */
	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	/**
	 * Set a custom RMI client socket factory to use for exporting the service.
	 * <p>If the given object also implements <code>java.rmi.server.RMIServerSocketFactory</code>,
	 * it will automatically be registered as server socket factory too.
	 * @see #setServerSocketFactory
	 * @see java.rmi.server.RMIClientSocketFactory
	 * @see java.rmi.server.RMIServerSocketFactory
	 * @see UnicastRemoteObject#exportObject(Remote, int, RMIClientSocketFactory, RMIServerSocketFactory)
	 */
	public void setClientSocketFactory(RMIClientSocketFactory clientSocketFactory) {
		this.clientSocketFactory = clientSocketFactory;
	}

	/**
	 * Set a custom RMI server socket factory to use for exporting the service.
	 * <p>Only needs to be specified when the client socket factory does not
	 * implement <code>java.rmi.server.RMIServerSocketFactory</code> already.
	 * @see #setClientSocketFactory
	 * @see java.rmi.server.RMIClientSocketFactory
	 * @see java.rmi.server.RMIServerSocketFactory
	 * @see UnicastRemoteObject#exportObject(Remote, int, RMIClientSocketFactory, RMIServerSocketFactory)
	 */
	public void setServerSocketFactory(RMIServerSocketFactory serverSocketFactory) {
		this.serverSocketFactory = serverSocketFactory;
	}

	/**
	 * Set a custom RMI client socket factory to use for the RMI registry.
	 * <p>If the given object also implements <code>java.rmi.server.RMIServerSocketFactory</code>,
	 * it will automatically be registered as server socket factory too.
	 * @see #setRegistryServerSocketFactory
	 * @see java.rmi.server.RMIClientSocketFactory
	 * @see java.rmi.server.RMIServerSocketFactory
	 * @see LocateRegistry#getRegistry(String, int, RMIClientSocketFactory)
	 */
	public void setRegistryClientSocketFactory(RMIClientSocketFactory registryClientSocketFactory) {
		this.registryClientSocketFactory = registryClientSocketFactory;
	}

	/**
	 * Set a custom RMI server socket factory to use for the RMI registry.
	 * <p>Only needs to be specified when the client socket factory does not
	 * implement <code>java.rmi.server.RMIServerSocketFactory</code> already.
	 * @see #setRegistryClientSocketFactory
	 * @see java.rmi.server.RMIClientSocketFactory
	 * @see java.rmi.server.RMIServerSocketFactory
	 * @see LocateRegistry#createRegistry(int, RMIClientSocketFactory, RMIServerSocketFactory)
	 */
	public void setRegistryServerSocketFactory(RMIServerSocketFactory registryServerSocketFactory) {
		this.registryServerSocketFactory = registryServerSocketFactory;
	}


	/**
	 * Register the service as RMI object.
	 * Creates an RMI registry on the specified port if none exists.
	 */
	public void afterPropertiesSet() throws RemoteException {
		checkService();

		if (this.serviceName == null) {
			throw new IllegalArgumentException("serviceName is required");
		}

		// Check socket factories for exported object.
		if (this.clientSocketFactory instanceof RMIServerSocketFactory) {
			this.serverSocketFactory = (RMIServerSocketFactory) this.clientSocketFactory;
		}
		if ((this.clientSocketFactory != null && this.serverSocketFactory == null) ||
				(this.clientSocketFactory == null && this.serverSocketFactory != null)) {
			throw new IllegalArgumentException(
					"Both RMIClientSocketFactory and RMIServerSocketFactory or none required");
		}

		// Check socket factories for RMI registry.
		if (this.registryClientSocketFactory instanceof RMIServerSocketFactory) {
			this.registryServerSocketFactory = (RMIServerSocketFactory) this.registryClientSocketFactory;
		}
		if (this.registryClientSocketFactory == null && this.registryServerSocketFactory != null) {
			throw new IllegalArgumentException(
					"RMIServerSocketFactory without RMIClientSocketFactory for registry not supported");
		}

		// Determine RMI registry to use.
		Registry registry = getRegistry(this.registryHost, this.registryPort,
				this.registryClientSocketFactory, this.registryServerSocketFactory);

		// Initialize and cache exported object.
		this.exportedObject = getObjectToExport();

		// Export remote object and bind it to RMI registry.
		if (logger.isInfoEnabled()) {
			logger.info("Binding RMI service '" + this.serviceName +
					"' to registry at port '" + this.registryPort + "'");
		}
		if (this.clientSocketFactory != null) {
			UnicastRemoteObject.exportObject(
					this.exportedObject, this.servicePort, this.clientSocketFactory, this.serverSocketFactory);
		}
		else {
			UnicastRemoteObject.exportObject(this.exportedObject, this.servicePort);
		}
		registry.rebind(this.serviceName, this.exportedObject);
	}

	/**
	 * Locate or create the RMI registry for this exporter.
	 * @param registryHost the registry host to use (if this is specified,
	 * no implicit creation of a RMI registry will happen)
	 * @param registryPort the registry port to use
	 * @param clientSocketFactory the RMI client socket factory for the registry (if any)
	 * @param serverSocketFactory the RMI server socket factory for the registry (if any)
	 * @return the RMI registry
	 * @throws RemoteException if the registry couldn't be located or created
	 */
	protected Registry getRegistry(String registryHost, int registryPort,
			RMIClientSocketFactory clientSocketFactory, RMIServerSocketFactory serverSocketFactory)
			throws RemoteException {

		if (registryHost != null) {
			// Host explictly specified: only lookup possible.
			Registry registry = null;
			if (clientSocketFactory != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Looking for RMI registry at port '" + registryPort + "' of host [" + registryHost +
							"], using custom socket factory");
				}
				registry = LocateRegistry.getRegistry(registryHost, registryPort, clientSocketFactory);
			}
			else {
				if (logger.isInfoEnabled()) {
					logger.info("Looking for RMI registry at port '" + registryPort + "' of host [" + registryHost + "]");
				}
				registry = LocateRegistry.getRegistry(registryHost, registryPort);
			}
			testRegistry(registry);
			return registry;
		}
		else {
			return getRegistry(registryPort, clientSocketFactory, serverSocketFactory);
		}
	}

	/**
	 * Locate or create the RMI registry for this exporter.
	 * @param registryPort the registry port to use
	 * @param clientSocketFactory the RMI client socket factory for the registry (if any)
	 * @param serverSocketFactory the RMI server socket factory for the registry (if any)
	 * @return the RMI registry
	 * @throws RemoteException if the registry couldn't be located or created
	 */
	protected Registry getRegistry(
			int registryPort, RMIClientSocketFactory clientSocketFactory, RMIServerSocketFactory serverSocketFactory)
			throws RemoteException {

		if (clientSocketFactory != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Looking for RMI registry at port '" + registryPort + "', using custom socket factory");
			}
			try {
				// Retrieve existing registry.
				Registry registry = LocateRegistry.getRegistry(null, registryPort, clientSocketFactory);
				testRegistry(registry);
				return registry;
			}
			catch (RemoteException ex) {
				logger.debug("RMI registry access threw exception", ex);
				logger.warn("Could not detect RMI registry - creating new one");
				// Assume no registry found -> create new one.
				return LocateRegistry.createRegistry(registryPort, clientSocketFactory, serverSocketFactory);
			}
		}
		else {
			return getRegistry(registryPort);
		}
	}

	/**
	 * Locate or create the RMI registry for this exporter.
	 * @param registryPort the registry port to use
	 * @return the RMI registry
	 * @throws RemoteException if the registry couldn't be located or created
	 */
	protected Registry getRegistry(int registryPort) throws RemoteException {
		if (logger.isInfoEnabled()) {
			logger.info("Looking for RMI registry at port '" + registryPort + "'");
		}
		Registry registry;
		try {
			// Retrieve existing registry.
			registry = LocateRegistry.getRegistry(registryPort);
			testRegistry(registry);
		}
		catch (RemoteException ex) {
			logger.debug("RMI registry access threw exception", ex);
			logger.warn("Could not detect RMI registry - creating new one");
			// Assume no registry found -> create new one.
			registry = LocateRegistry.createRegistry(registryPort);
		}
		return registry;
	}

	/**
	 * Test the given RMI registry, calling some operation on it to
	 * check whether it is still active.
	 * @param registry the RMI registry to test
	 * @throws RemoteException if thrown by registry methods
	 */
	protected void testRegistry(Registry registry) throws RemoteException {
		registry.list();
	}

	/**
	 * Determine the object to export: either the service object itself
	 * or a RmiInvocationWrapper in case of a non-RMI service object.
	 * @return the RMI object to export
	 */
	protected Remote getObjectToExport() {
		// determine remote object
		if (getService() instanceof Remote &&
				((getServiceInterface() == null) || Remote.class.isAssignableFrom(getServiceInterface()))) {
			// conventional RMI service
			return (Remote) getService();
		}
		else {
			// RMI invoker
			if (logger.isInfoEnabled()) {
				logger.info("RMI object '" + this.serviceName + "' is an RMI invoker");
			}
			return new RmiInvocationWrapper(getProxyForService(), this);
		}
	}


	/**
	 * Redefined here to be visible to RmiInvocationWrapper.
	 * Simply delegates to the corresponding superclass method.
	 */
	protected Object invoke(RemoteInvocation invocation, Object targetObject)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
		return super.invoke(invocation, targetObject);
	}


	/**
	 * Unbind the RMI service from the registry at bean factory shutdown.
	 */
	public void destroy() throws RemoteException, NotBoundException {
		if (logger.isInfoEnabled()) {
			logger.info("Unbinding RMI service '" + this.serviceName +
					"' from registry at port '" + this.registryPort + "'");
		}
		Registry registry = LocateRegistry.getRegistry(
				this.registryHost, this.registryPort, this.registryClientSocketFactory);
		registry.unbind(this.serviceName);
		UnicastRemoteObject.unexportObject(this.exportedObject, true);
	}

}
