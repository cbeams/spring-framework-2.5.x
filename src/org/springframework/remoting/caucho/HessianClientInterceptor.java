package org.springframework.remoting.caucho;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.AuthorizableRemoteProxySupport;

/**
 * Interceptor for accessing a Hessian service.
 * Supports authentication via username and password.
 * The service URL must be an HTTP URL exposing a Hessian service.
 *
 * <p>Hessian is a slim, binary RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>
 *
 * <p>Note: Hessian services accessed with this proxy factory do not have to be
 * exported via HessianServiceExporter, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class HessianClientInterceptor extends AuthorizableRemoteProxySupport
    implements MethodInterceptor, InitializingBean {

	private Object hessianProxy;

	public void afterPropertiesSet() throws MalformedURLException {
		HessianProxyFactory proxyFactory = new HessianProxyFactory();
		proxyFactory.setUser(getUsername());
		proxyFactory.setPassword(getPassword());
		this.hessianProxy = proxyFactory.create(getServiceInterface(), getServiceUrl());
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.getMethod().invoke(this.hessianProxy, invocation.getArguments());
		}
		catch (UndeclaredThrowableException ex) {
			throw new RemoteAccessException("Cannot access Hessian service", ex.getUndeclaredThrowable());
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof HessianRuntimeException) {
				HessianRuntimeException hre = (HessianRuntimeException) ex.getTargetException();
				Throwable rootCause = (hre.getRootCause() != null) ? hre.getRootCause() : hre;
				throw new RemoteAccessException("Cannot access Hessian service", rootCause);
			}
			else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw new RemoteAccessException("Cannot access Hessian service", utex.getUndeclaredThrowable());
			}
			throw ex.getTargetException();
		}
	}

}
