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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience superclass for configuration used in creating proxies,
 * to ensure that all proxy creators have consistent properties.
 *
 * <p>Note that it is no longer possible to configure subclasses to expose
 * the MethodInvocation. Interceptors should normally manage their own
 * ThreadLocals if they need to make resources available to advised objects.
 * If it's absolutely necessary to expose the MethodInvocation, use an
 * interceptor to do so.
 *
 * @author Rod Johnson
 */
public class ProxyConfig implements Serializable {
	
	/*
	 * Note that some of the instance variables in this class and AdvisedSupport
	 * are protected, rather than private, as is usually preferred in Spring
	 * (following "Expert One-on-One J2EE Design and Development", Chapter 4).
	 * This allows direct field access in the AopProxy implementations, which
	 * produces a 10-20% reduction in AOP performance overhead compared with
	 * method access. - RJ, December 10, 2003.
	 */
	
	/**
	 * Transient to optimize serialization:
	 * AdvisedSupport resets it.
	 */
	protected transient Log logger = LogFactory.getLog(getClass());

	private boolean proxyTargetClass;
	
	private boolean optimize;
	
	private boolean opaque;

	/**
	 * Should proxies obtained from this configuration expose
	 * the AOP proxy for the AopContext class to retrieve for targets?
	 * The default is false, as enabling this property may impair performance.
	 */
	protected boolean exposeProxy;

	/**
	 * Is this config frozen: that is, should it be impossible
	 * to change advice. Default is not frozen.
	 */
	private boolean frozen;
	
	/** Factory used to create AopProxy instances */
	private transient AopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();

	
	/**
	 * Set whether to proxy the target class directly as well as any interfaces.
	 * We can set this to true to force CGLIB proxying. Default is false.
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * Return whether to proxy the target class directly as well as any interfaces.
	 */
	public boolean getProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * Set whether proxies should perform agressive optimizations.
	 * The exact meaning of "agressive optimizations" will differ
	 * between proxies, but there is usually some tradeoff. 
	 * <p>For example, optimization will usually mean that advice changes won't
	 * take effect after a proxy has been created. For this reason, optimization
	 * is disabled by default. An optimize value of true may be ignored
	 * if other settings preclude optimization: for example, if exposeProxy
	 * is set to true and that's not compatible with the optimization.
	 * <p>For example, CGLIB-enhanced proxies may optimize out.
	 * overriding methods with no advice chain. This can produce 2.5x
	 * performance improvement for methods with no advice.
	 * <p><b>Warning:</b> Setting this to true can produce large performance
	 * gains when using CGLIB (also set proxyTargetClass to true), so it's
	 * a good setting for performance-critical proxies. However, enabling this
	 * will mean that advice cannot be changed after a proxy has been obtained
	 * from this factory.
	 * @param optimize whether to enable agressive optimizations. 
	 * Default is false.
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * Return whether proxies should perform agressive optimizations.
	 */
	public boolean getOptimize() {
		return this.optimize;
	}

	/**
	 * Set whether the proxy should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the AopContext class. This is useful
	 * if an advised object needs to call another advised method on itself.
	 * (If it uses <code>this</code>, the invocation will not be advised).
	 * <p>Default is false, for optimal performance.
	 */
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}
	
	/**
	 * Return whether the AOP proxy will expose the AOP proxy for
	 * each invocation.
	 */
	public boolean getExposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * Set whether this config should be frozen.
	 * <p>When a config is frozen, no advice changes can be made. This is
	 * useful for optimization, and useful when we don't want callers to
	 * be able to manipulate configuration after casting to Advised.
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * Return whether the config is frozen, and no advice changes can be made.
	 */
	public boolean isFrozen() {
		return frozen;
	}

	/**
	 * Customize the AopProxyFactory, allowing different strategies
	 * to be dropped in without changing the core framework.
	 * Default is DefaultAopProxyFactory, using dynamic proxies or CGLIB.
	 * <p>For example, an AopProxyFactory could return an AopProxy using
	 * dynamic proxies, CGLIB or code generation strategy.
	 */
	public void setAopProxyFactory(AopProxyFactory apf) {
		this.aopProxyFactory = apf;
	}

	/**
	 * Return the AopProxyFactory that this ProxyConfig uses.
	 */
	public AopProxyFactory getAopProxyFactory() {
		return this.aopProxyFactory;
	}

	/**
	 * @return whether proxies created by this configuration
	 * should be prevented from being cast to Advised
	 */
	public boolean getOpaque() {
		return opaque;
	}
	
	/**
	 * @param opaque Set whether proxies created by this configuration
	 * should be prevented from being cast to Advised to
	 * query proxy status. Default is false, meaning that
	 * any AOP proxy can be cast to Advised.
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * Copy configuration from the other config object.
	 * @param other object to copy configuration from
	 */
	public void copyFrom(ProxyConfig other) {
		this.proxyTargetClass = other.proxyTargetClass;
		this.optimize = other.getOptimize();
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.opaque = other.opaque;
		this.aopProxyFactory = other.aopProxyFactory;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("proxyTargetClass=" + this.proxyTargetClass + "; ");
		sb.append("optimize=" + this.optimize + "; ");
		sb.append("exposeProxy=" + this.exposeProxy + "; ");
		sb.append("opaque=" + this.opaque + "; ");
		sb.append("frozen=" + this.frozen + "; ");
		sb.append("aopProxyFactory=" + this.aopProxyFactory + "; ");
		return sb.toString();
	}
}
