package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.springframework.remoting.support.RemoteInvocation;

/**
 * Server-side implementation of RmiInvocationHandler. An instance
 * of this class exists for each remote object. Automatically created
 * by RmiServiceExporter for non-RMI service implementations.
 *
 * <p>This is an SPI class, not to be used directly by applications.
 *
 * @author Juergen Hoeller
 * @since 14.05.2003
 * @see RmiServiceExporter
 */
class RmiInvocationWrapper implements RmiInvocationHandler {

	private final Object wrappedObject;

	private final RmiServiceExporter rmiServiceExporter;

	public RmiInvocationWrapper(Object wrappedObject, RmiServiceExporter rmiServiceExporter) {
		this.wrappedObject = wrappedObject;
		this.rmiServiceExporter = rmiServiceExporter;
	}

	public Object invoke(RemoteInvocation invocation)
	    throws RemoteException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return this.rmiServiceExporter.invoke(invocation, this.wrappedObject);
	}

}
