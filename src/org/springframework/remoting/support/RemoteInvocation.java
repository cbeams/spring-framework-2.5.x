package org.springframework.remoting.support;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInvocation;

/**
 * JavaBean that encapsulates a remote invocation.
 * Provides the core method invocation properties.
 * Currently just used for RMI invokers, but not restricted to RMI.
 *
 * <p>This is an SPI class, typically not used directly by applications.
 * Can be subclassed for additional invocation parameters.
 *
 * @author Juergen Hoeller
 * @since 25.02.2004
 * @see org.springframework.remoting.rmi.RmiInvocationHandler
 */
public class RemoteInvocation implements Serializable {

	private String methodName;

	private Class[] parameterTypes;

	private Object[] arguments;

	/**
	 * Create a new RemoteInvocation for use as JavaBean.
	 */
	public RemoteInvocation() {
	}

	/**
	 * Create a new RemoteInvocation for the given parameters.
	 * @param methodName the name of the method to invoke
	 * @param argumentTypes the argument types of the method
	 * @param arguments the arguments for the invocation
	 */
	public RemoteInvocation(String methodName, Class[] argumentTypes, Object[] arguments) {
		this.methodName = methodName;
		this.parameterTypes = argumentTypes;
		this.arguments = arguments;
	}

	/**
	 * Create a new RemoteInvocation for the given AOP method invocation.
	 * @param methodInvocation the AOP invocation to convert
	 */
	public RemoteInvocation(MethodInvocation methodInvocation) {
		this.methodName = methodInvocation.getMethod().getName();
		this.parameterTypes = methodInvocation.getMethod().getParameterTypes();
		this.arguments = methodInvocation.getArguments();
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public Object[] getArguments() {
		return arguments;
	}

}
