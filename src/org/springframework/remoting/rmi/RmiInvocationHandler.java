package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.springframework.remoting.support.RemoteInvocation;

/**
 * Interface for RmiInvocationWrapper instances on the server,
 * wrapping exported services. A client uses a stub implementing
 * this interface to access such a service.
 *
 * <p>This is an SPI interface, not to be used directly by applications.
 *
 * @author Juergen Hoeller
 * @since 14.05.2003
 * @see RmiInvocationWrapper
 */
public interface RmiInvocationHandler extends Remote {

	/**
	 * Invoke the given method with the given parameters on the actual object.
	 * Called by RmiClientInterceptor.
	 * @param invocation object that encapsulates invocation parametres
	 * @return the object returned from the invoked method, if any
	 * @throws RemoteException in case of communication errors
	 * @throws NoSuchMethodException if the method name could not be resolved
	 * @throws IllegalAccessException if the method could not be accessed
	 * @throws InvocationTargetException if the method invocation resulted in an exception
	 * @see RmiClientInterceptor
	 */
	public Object invoke(RemoteInvocation invocation)
	    throws RemoteException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;

}
