/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;


/**
 * Implementation of Interceptor interface that 
 * invokes a local target object using reflection.
 * This is a simple JavaBean that caches a local object.
 * The AbstractReflectionInvokerInterceptor superclass
 * implements the invoke() method.
 * This should always be the last interceptor in the chain.
 * It does not invoke proceed() on the MethodInvocation. 
 * @author Rod Johnson
 * @version $Id: InvokerInterceptor.java,v 1.1 2003-11-16 12:54:59 johnsonr Exp $
 */
public class InvokerInterceptor extends AbstractReflectionInvokerInterceptor {

	/** Target cached and invoked using reflection */	
	private Object target;
	
	public InvokerInterceptor() {
	}
	
	public InvokerInterceptor(Object target) {
		this.target = target;
	}
	
	public void setTarget(Object target) {
		this.target = target;
	}
	
	public Object getTarget() {
		return this.target;
	}
	
	/**
	 * Two invoker interceptors are equal if they have the same target or if the targets
	 * are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof InvokerInterceptor))
			return false;
		InvokerInterceptor otherII = (InvokerInterceptor) other;
		return otherII.target == this.target || otherII.target.equals(this.target);
	}

}
