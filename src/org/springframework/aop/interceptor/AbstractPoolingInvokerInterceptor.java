/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

import java.lang.reflect.InvocationTargetException;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.MethodInvocationImpl;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.SimpleIntroductionAdvice;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;

/**
 * Invoker interceptor that maintains a pool of instances, acquiring and
 * releasing an object from the pool for each method invocation.
 *
 * <p>This implementation is independent of pooling technology.
 *
 * <p>Subclasses must implement the acquireTarget() and releaseTarget() methods
 * to work with their chosen pool. The createTarget() method in this class can
 * be used to create objects to put in the pool.
 *
 * <p>This class implements DisposableBean to force subclasses to implement
 * a destroy() method to close down their pool.
 *
 * @author Rod Johnson
 * @version $Id: AbstractPoolingInvokerInterceptor.java,v 1.3 2003-11-24 11:29:16 johnsonr Exp $
 */
public abstract class AbstractPoolingInvokerInterceptor extends PrototypeInvokerInterceptor implements PoolingConfig, DisposableBean {
	
	/** The size of the pool */
	private int maxSize;
	
	private int invocations;

	/**
	 * Return the size of the pool
	 * @return the size of the pool
	 */
	public int getMaxSize() {
		return this.maxSize;
	}

	/**
	 * Set the size of the pool
	 * @param poolSize the size for the pool
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * Create a new target object that can be added to the pool
	 * @return a new target
	 */
	protected Object createTarget() {
		return super.getTarget();
	}
	
	public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);
		try {
			createPool(beanFactory);
		}
		catch (BeansException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Could not create instance pool", ex);
		}
	}
	
	
	/**
	 * Create the pool.
	 * @param beanFactory owning BeanFactory, in case we need collaborators from it
	 * (normally our own properties are sufficient)
	 * @throws Exception to avoid placing constraints on pooling APIs
	 */
	protected abstract void createPool(BeanFactory beanFactory) throws Exception;
	
	/**
	 * Acquire an object from the pool.
	 * @return an object from the pool
	 * @throws Exception we may need to deal with checked exceptions from pool
	 * APIs, so we're forgiving with our exception signature,
	 * although we don't like APIs that throw Exception
	 */
	protected abstract Object acquireTarget() throws Exception;
	
	/**
	 * Return the given object to the pool.
	 * @param target object that must have been acquired from the pool
	 * via a call to acquireTarget()
	 * @throws Exception to allow pooling APIs to throw exception
	 */
	protected abstract void releaseTarget(Object target) throws Exception; 
	
	/**
	 * @see org.springframework.aop.interceptor.PoolingConfig#getInvocations()
	 */
	public int getInvocations() {
		return this.invocations;
	}
	
	
	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object target = acquireTarget();
		++invocations;
		
		// Set the target on the invocation
		if (invocation instanceof MethodInvocationImpl) {
			((MethodInvocationImpl) invocation).setTarget(target);
		}
	
		// Use reflection to invoke the method
		try {
			return invocation.getMethod().invoke(target, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception. 
			// We must rethrow it. The client won't see the interceptor
			throw ex.getTargetException();
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Couldn't access method " + invocation.getMethod() + ", ", ex);
		}
		finally {
			releaseTarget(target);
		}
	}
	
	
	public SimpleIntroductionAdvice getPoolingConfigMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new SimpleIntroductionAdvice(dii, PoolingConfig.class);
	}

}
