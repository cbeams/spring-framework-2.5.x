/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

import java.lang.reflect.InvocationTargetException;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.MethodInvocationImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Invoker interceptor that maintains a pool of instances,
 * acquiring and releasing an object from the pool for each
 * method invocation. 
 * <br>This implementation is independent of pooling technology.
 * <br>Subclasses must implement the acquireTarget()
 * and releastTarget() methods to work with their chosen pool.
 * The createTarget() method in this class can be used
 * to create objects to put in the pool.
 * <br>This class implements DisposableBean to force subclasses
 * to implement a destroy() method to close down their pool.
 * @author Rod Johnson
 * @version $Id: AbstractPoolingInvokerInterceptor.java,v 1.1 2003-10-08 08:13:20 johnsonr Exp $
 */
public abstract class AbstractPoolingInvokerInterceptor extends PrototypeInvokerInterceptor implements DisposableBean {
	
	/** The size of the pool */
	private int poolSize;
	
	
	/**
	 * Return the size of the pool
	 * @return the size of the pool
	 */
	public int getPoolSize() {
		return this.poolSize;
	}

	/**
	 * Set the size of the pool
	 * @param poolSize the size for the pool
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}


	/**
	 * Create a new target object that can be added to the pool
	 * @return a new target
	 */
	protected Object createTarget() {
		return super.getTarget();
	}
	
	/**
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public final void setBeanFactory(BeanFactory beanFactory) throws Exception {
		super.setBeanFactory(beanFactory);
		createPool(beanFactory);
	}
	
	
	/**
	 * Create the pool
	 * @param beanFactory owning BeanFactory, in case we
	 * need collaborators from it (normally our own properties
	 * are sufficient)
	 * @throws Exception to avoid placing constraints on pooling APIs
	 */
	protected abstract void createPool(BeanFactory beanFactory) throws Exception;
	
	/**
	 * Acquire an object from the pool
	 * @return an object from the pool
	 * @throws Exception we may need to deal with checked exceptions from pool
	 * APIs, so we're forgiving with our exception signature,
	 * although we don't like APIs that throw Exception
	 */
	protected abstract Object acquireTarget() throws Exception;
	
	/**
	 * Return the given object to the pool
	 * @param target object that must have been acquired
	 * from the pool via a call to acquireTarget()
	 * @throws Exception to allow pooling APIs to throw exception
	 */
	protected abstract void releaseTarget(Object target) throws Exception; 
	
	
	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		Object target = acquireTarget();
		
		// Set the target on the invocation
		if (invocation instanceof MethodInvocationImpl) {
			((MethodInvocationImpl) invocation).setTarget(target);
		}
	
		// Use reflection to invoke the method
		try {
			
			Object rval = invocation.getMethod().invoke(target, invocation.getArguments());
			return rval;
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception. 
			// We must rethrow it. The client won't see the interceptor
			Throwable t = ex.getTargetException();
			throw t;
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Couldn't access method " + invocation.getMethod() + ", ", ex);
		}
		finally {
			releaseTarget(target);
		}
	}

}
