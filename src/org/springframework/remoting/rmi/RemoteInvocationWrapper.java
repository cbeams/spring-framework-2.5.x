package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Server-side implementation of RemoteInvocationHandler.
 * An instance of this class exists for each remote object.
 * @author Juergen Hoeller
 * @since 14.05.2003
 */
class RemoteInvocationWrapper implements RemoteInvocationHandler {

	private final Object wrappedObject;

	/**
	 * Create a new RemoteInvocationWrapper.
	 * @param wrappedObject	the locally wrapped object which remote
	 * invocations are delegated to
	 */
	protected RemoteInvocationWrapper(Object wrappedObject) {
		this.wrappedObject = wrappedObject;
	}

	public Object invokeRemote(String methodName, Class[] paramTypes, Object[] params)
	    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method = this.wrappedObject.getClass().getMethod(methodName, paramTypes);
		return method.invoke(this.wrappedObject, params);
	}

}
