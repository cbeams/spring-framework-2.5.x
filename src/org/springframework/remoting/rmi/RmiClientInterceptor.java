package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteProxySupport;

/**
 * Interceptor for accessing transparent RMI services.
 * The service URL must be a valid RMI URL like "rmi://localhost:1099/myservice".
 *
 * <p>Transparent means that RMI communication works on the RemoteInvocationHandler
 * level, needing only one stub for any service. Service interfaces do not have to
 * extend java.rmi.Remote or throw RemoteException on all methods, but in and out
 * parameters have to be serializable.
 *
 * <p>This interceptor can only access RMI objects that got exported with a
 * RemoteInvocationWrapper, i.e. working on the RemoteInvocationHandler level.
 *
 * <p>The major advantage of RMI, compared to Hessian and Burlap, is serialization.
 * Effectively, any serializable Java object can be transported without hassle.
 * Hessian and Burlap have their own (de-)serialization mechanisms, but are
 * HTTP-based and thus much easier to setup than RMI.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class RmiClientInterceptor extends RemoteProxySupport implements MethodInterceptor, InitializingBean {

	private RemoteInvocationHandler rmiProxy;

	public void afterPropertiesSet() throws MalformedURLException, NotBoundException, RemoteException {
		Remote remoteObj = Naming.lookup(getServiceUrl());
		if (!(remoteObj instanceof RemoteInvocationHandler)) {
			throw new NotBoundException("Bound RMI object isn't a RemoteInvocationHandler (no transparent RMI handler)");
		}
		this.rmiProxy = (RemoteInvocationHandler) remoteObj;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return this.rmiProxy.invokeRemote(invocation.getMethod().getName(),
			                                  invocation.getMethod().getParameterTypes(),
			                                  invocation.getArguments());
		}
		catch (RemoteException ex) {
			throw new RemoteAccessException("Cannot access transparent RMI service", ex);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

}
