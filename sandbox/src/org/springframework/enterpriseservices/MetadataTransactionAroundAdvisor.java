/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.enterpriseservices;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.metadata.Attributes;
import org.springframework.transaction.interceptor.AttributesTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Transaction pointcut that looks for metadata.
 * Falls back to class transaction attribute if none is
 * found on a method.
 * @author Rod Johnson
 * @version $Id: MetadataTransactionAroundAdvisor.java,v 1.1 2003-11-22 09:05:39 johnsonr Exp $
 */
public class MetadataTransactionAroundAdvisor extends TransactionInterceptor implements InterceptionAroundAdvisor, Pointcut, MetadataDriven {
	
	/** Go below this if you want custom pointcuts or interceptors first */
	public final static int ORDER_VALUE = 10;
	
	private Attributes attributes; 
	
	private MethodMatcher methodMatcher;
	
	public MetadataTransactionAroundAdvisor() {
	}

	
	/**
	 * Is there a transaction attribute in this array of attributes?
	 * @param atts
	 * @return
	 */
	private boolean hasTransactionAttribute(List atts) {
		//logger.debug("Atts length=" + ((atts == null || atts.length == 0) ? 0 : atts.length) );
		for (int i = 0; i < atts.size(); i++) {
			//logger.debug("Found attribute " + atts.get(i));
			if (atts.get(i) instanceof TransactionAttribute) {
				logger.info("FOUND transaction attribute");
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.springframework.aop.framework.MethodPointcut#getInterceptor()
	 */
	public Interceptor getInterceptor() {
		return this;
	}

	/**
	 * Lower values are higher priority
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() {
		return ORDER_VALUE;
	}

	/**
	 * @see org.springframework.aop.InterceptionAdvice#getPointcut()
	 */
	public Pointcut getPointcut() {
		return this;
	}

	/**
	 * @see org.springframework.aop.Pointcut#getClassFilter()
	 */
	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	/**
	 * @see org.springframework.aop.Pointcut#getMethodMatcher()
	 */
	public MethodMatcher getMethodMatcher() {
		return methodMatcher;
	
	}


	/**
	 * @see org.springframework.enterpriseservices.MetadataDriven#setAttribute(org.springframework.metadata.Attributes)
	 */
	public void setAttributes(Attributes atts) {
		this.attributes = atts;
		setTransactionAttributeSource(new AttributesTransactionAttributeSource(attributes));
		this.methodMatcher = new StaticMethodMatcher() {
			public boolean matches(Method method, Class targetClass) {
				return hasTransactionAttribute(attributes.getAttributes(method)) ||
					hasTransactionAttribute(attributes.getAttributes(targetClass));
			}
		};
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		// TODO doesn't seem to work with attributes
		super.afterPropertiesSet();
		if (this.attributes == null)
			throw new AopConfigException("Must set attributes property on MetadataTransactionAdice");
	}


	/**
	 * Shared between all advised instances
	 * @see org.springframework.aop.Advice#isPerInstance()
	 */
	public boolean isPerInstance() {
		return false;
	}

}
