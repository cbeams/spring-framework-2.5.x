package org.springframework.remoting.caucho;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;

import com.caucho.burlap.client.BurlapProxyFactory;
import com.caucho.burlap.client.BurlapRuntimeException;
import com.caucho.hessian.client.HessianRuntimeException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.AuthorizableRemoteProxySupport;

/**
 * Interceptor for accessing a Burlap service.
 * Supports authentication via username and password.
 * The service URL must be an HTTP URL exposing a Burlap service.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Burlap, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>
 *
 * <p>Note: Burlap services accessed with this proxy factory do not have to be
 * exported via BurlapServiceExporter, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class BurlapClientInterceptor extends AuthorizableRemoteProxySupport
    implements MethodInterceptor, InitializingBean {

	private Object burlapProxy;

	public void afterPropertiesSet() throws MalformedURLException {
		BurlapProxyFactory proxyFactory = new BurlapProxyFactory();
		proxyFactory.setUser(getUsername());
		proxyFactory.setPassword(getPassword());
		this.burlapProxy = proxyFactory.create(getServiceInterface(), getServiceUrl());
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.getMethod().invoke(this.burlapProxy, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof HessianRuntimeException) {
				BurlapRuntimeException bre = (BurlapRuntimeException) ex.getTargetException();
				Throwable rootCause = (bre.getRootCause() != null) ? bre.getRootCause() : bre;
				throw new RemoteAccessException("Cannot access Burlap service", rootCause);
			}
			else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw new RemoteAccessException("Cannot access Hessian service", utex.getUndeclaredThrowable());
			}
			throw ex.getTargetException();
		}
	}

}
