package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

/**
 * Interceptor for accessing conventional or transparent RMI services.
 * The service URL must be a valid RMI URL like "rmi://localhost:1099/myservice".
 *
 * <p>"Transparent RMI" works at the RemoteInvocationHandler level, needing only one
 * stub for any service. Service interfaces do not have to extend java.rmi.Remote or
 * throw RemoteException; Spring's unchecked RemoteAccessException will be thrown on
 * remote invocation failure. Of course, in and out parameters have to be serializable.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see RmiServiceExporter
 * @see org.springframework.remoting.RemoteAccessException
 * @see java.rmi.RemoteException
 * @see java.rmi.Remote
 */
public class RmiClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor, InitializingBean {

	private Remote rmiProxy;

	public void afterPropertiesSet() throws Exception {
		if (getServiceUrl() == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
		Remote remoteObj = createRmiProxy();
		if (remoteObj instanceof RemoteInvocationHandler) {
			logger.info("RMI object [" + getServiceUrl() + "] is an RMI invoker");
		}
		else if (getServiceInterface() != null) {
			boolean isImpl = getServiceInterface().isInstance(remoteObj);
			logger.info("Using service interface [" + getServiceInterface().getName() + "] for RMI object [" +
									getServiceUrl() + "] - " + (!isImpl ? "not " : "") + "directly implemented");
		}
		this.rmiProxy = remoteObj;
	}

	/**
	 * Create the RMI proxy. Default implementation looks up the service URL
	 * via java.rmi.Naming. Can be overridden in subclasses.
	 * @see java.rmi.Naming#lookup
	 */
	protected Remote createRmiProxy() throws Exception {
		return Naming.lookup(getServiceUrl());
	}

	/**
	 * Return the underlying RMI proxy that this interceptor delegates to.
	 */
	protected Remote getRmiProxy() {
		return rmiProxy;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			if (this.rmiProxy instanceof RemoteInvocationHandler) {
				RemoteInvocationHandler invoker = (RemoteInvocationHandler) this.rmiProxy;
				return invoker.invokeRemote(invocation.getMethod().getName(),
																		invocation.getMethod().getParameterTypes(),
																		invocation.getArguments());
			}
			else {
				Method method = invocation.getMethod();
				if (method.getDeclaringClass().isInstance(this.rmiProxy)) {
					// directly implemented
					return method.invoke(this.rmiProxy, invocation.getArguments());
				}
				else {
					// not directly implemented
					Method proxyMethod = this.rmiProxy.getClass().getMethod(method.getName(), method.getParameterTypes());
					return proxyMethod.invoke(this.rmiProxy, invocation.getArguments());
				}
			}
		}
		catch (RemoteException ex) {
			logger.debug("RMI invoker for service [" + getServiceUrl() + "] threw exception", ex);
			if (!Arrays.asList(invocation.getMethod().getExceptionTypes()).contains(RemoteException.class)) {
				throw new RemoteAccessException("Cannot access RMI invoker for [" + getServiceUrl() + "]", ex);
			}
			else {
				throw ex;
			}
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			logger.debug("RMI method of service [" + getServiceUrl() + "] threw exception", targetException);
			if (targetException instanceof RemoteException &&
					!Arrays.asList(invocation.getMethod().getExceptionTypes()).contains(RemoteException.class)) {
				throw new RemoteAccessException("Cannot access RMI service [" + getServiceUrl() + "]", targetException);
			}
			else {
				throw targetException;
			}
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke RMI service [" + getServiceUrl() + "]", ex);
		}
	}

}
