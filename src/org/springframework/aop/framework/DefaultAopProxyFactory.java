/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

/**
 * Simple implementation of AopProxyFactory
 * @author Rod Johnson
 * @version $Id: DefaultAopProxyFactory.java,v 1.1 2004-03-12 02:51:19 johnsonr Exp $
 */
public class DefaultAopProxyFactory implements AopProxyFactory {

	/**
	 * @see org.springframework.aop.framework.AopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
	 */
	public AopProxy createAopProxy(AdvisedSupport advisedSupport) throws AopConfigException {
		boolean useCglib = advisedSupport.getOptimize() || advisedSupport.getProxyTargetClass() || advisedSupport.getProxiedInterfaces().length == 0;
		if (useCglib) {
			return CglibProxyFactory.createCglibProxy(advisedSupport);
		}
		else {
			// Depends on whether we have expose proxy or frozen or static ts
			return new JdkDynamicAopProxy(advisedSupport);
		}
	}
	
	/**
	 * Inner class to just introduce a CGLIB dependency
	 * when actually creating a CGLIB proxy.
	 */
	private static class CglibProxyFactory {

		private static AopProxy createCglibProxy(AdvisedSupport advisedSupport) {
			return new Cglib2AopProxy(advisedSupport);
		}
	}

}
