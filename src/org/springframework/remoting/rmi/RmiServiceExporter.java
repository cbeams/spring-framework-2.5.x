package org.springframework.remoting.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * RMI exporter that exposes the specified service as transparent RMI object
 * with the specified name. Such services can be accessed via RmiProxyFactoryBean.
 *
 * <p>Transparent means that RMI communication works on the RemoteInvocationHandler
 * level, needing only one stub for any service. Service interfaces do not have to
 * extend java.rmi.Remote or throw RemoteException on all methods, but in and out
 * parameters have to be serializable.
 *
 * <p>A conventional RMI lookup will return a RemoteInvocationHandler instance
 * instead of a proxy implementing the service interface. To access the service,
 * a java.lang.reflect.Proxy has to be created, using a StubInvocationHandler
 * configured for the RemoteInvocationHandler instance. Of course, a helper like
 * RmiProxyFactoryBean makes this much simpler!
 *
 * <p>The major advantage of RMI, compared to Hessian and Burlap, is serialization.
 * Effectively, any serializable Java object can be transported without hassle.
 * Hessian and Burlap have their own (de-)serialization mechanisms, but are
 * HTTP-based and thus much easier to setup than RMI. 
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see RmiProxyFactoryBean
 */
public class RmiServiceExporter implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Object service;

	private String serviceName;

	private int servicePort = 0;  // anonymous port

	private int registryPort = Registry.REGISTRY_PORT;

	/**
	 * Set the service to export via RMI.
	 * Typically populated via a bean reference.
	 */
	public void setService(Object service) {
		this.service = service;
	}

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
	 * Register the service as RMI object.
	 * Creates an RMI registry on the specified port if none exists.
	 */
	public void afterPropertiesSet() throws RemoteException, AlreadyBoundException {
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

		// bind wrapper to registry
		logger.info("Binding RMI service '" + this.serviceName + "' to registry at port '" + this.registryPort + "'");
		if (this.service instanceof Remote) {
			// plain RMI service
			Remote exportedObject = UnicastRemoteObject.exportObject((Remote) this.service, this.servicePort);
			registry.rebind(this.serviceName, exportedObject);
		}
		else {
			// RMI invoker
			logger.info("RMI object '" + this.serviceName + "' is an RMI invoker");
			Remote wrapper = new RemoteInvocationWrapper(this.service, this.servicePort);
			registry.rebind(this.serviceName, wrapper);
		}
	}

}
