package org.springframework.remoting.rmi;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Server-side implementation of RemoteInvocationHandler.
 * An instance of this class exists for each remote object.
 * @author Juergen Hoeller
 * @since 14.05.2003
 */
class RemoteInvocationWrapper extends UnicastRemoteObject implements RemoteInvocationHandler {

	private final Object wrappedObject;

	/**
	 * Create a new RemoteInvocationWrapper.
	 * @param wrappedObject	the locally wrapped object, on which methods are invoked
	 * @param port the port number on which the remote object receives calls
	 * (if 0, an anonymous port is chosen)
	 */
	protected RemoteInvocationWrapper(Object wrappedObject, int port) throws RemoteException {
		// the super() invocation will export this object as remote service
		super(port);
		this.wrappedObject = wrappedObject;
	}

	public Object invokeRemote(String methodName, Class[] paramTypes, Object[] params)
	    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method = this.wrappedObject.getClass().getMethod(methodName, paramTypes);
		return method.invoke(this.wrappedObject, params);
	}

}
