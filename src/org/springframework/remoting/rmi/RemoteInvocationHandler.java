package org.springframework.remoting.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface for RemoteInvocationWrapper instances on the server.
 * A client's StubInvocationHandler uses a stub implementing this interface.
 * @author Juergen Hoeller
 * @since 14.05.2003
 */
interface RemoteInvocationHandler extends Remote {

	/**
	 * Called by the StubInvocationHandler on each invocation.
	 * Invokes the given method with the given parameters on the actual object.
	 * @param methodName the name of the invoked method
	 * @param paramTypes the method's parameter types.
	 * @param params the method's parameters
	 * @return the object returned from the invoked method, if any
	 * @throws RemoteException in case of communication errors
	 * @throws NoSuchMethodException if the method name could not be resolved
	 * @throws IllegalAccessException if the method could not be accessed
	 * @throws InvocationTargetException if the method invocation resulted in an exception
	 */
	public Object invokeRemote(String methodName, Class[] paramTypes, Object[] params)
	    throws RemoteException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;

}
