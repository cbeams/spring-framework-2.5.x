/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.framework;

/**
 * Simple implementation of AopProxyFactory,
 * either creating a CGLIB proxy or a JDK dynamic proxy.
 *
 * <p>Creates a CGLIB proxy if one the following is true:
 * <ul>
 * <li>the "optimize" flag is set
 * <li>the "proxyTargetClass" flag is set
 * <li>no interfaces have been specified
 * </ul>
 *
 * <p>In general, specify "proxyTargetClass" to enforce a CGLIB proxy,
 * respectively one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see Cglib2AopProxy
 * @see JdkDynamicAopProxy
 * @see AdvisedSupport#getOptimize
 * @see AdvisedSupport#getProxyTargetClass
 * @see AdvisedSupport#getProxiedInterfaces 
 */
public class DefaultAopProxyFactory implements AopProxyFactory {

	public AopProxy createAopProxy(AdvisedSupport advisedSupport) throws AopConfigException {
		boolean useCglib =
		    advisedSupport.getOptimize() ||
		    advisedSupport.getProxyTargetClass() ||
		    advisedSupport.getProxiedInterfaces().length == 0;

		if (useCglib) {
			return CglibProxyFactory.createCglibProxy(advisedSupport);
		}
		else {
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
