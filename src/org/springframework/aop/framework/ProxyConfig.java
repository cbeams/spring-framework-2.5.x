/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience superclass for configuration to do with creating proxies.
 * @author Rod Johnson
 * @version $Id: ProxyConfig.java,v 1.3 2003-12-02 09:36:46 johnsonr Exp $
 */
public class ProxyConfig {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean proxyTargetClass;
	
	private boolean enableCglibSubclassOptimizations;
	
	/**
	 * Should proxies obtained from this configuration expose
	 * Invocation for the AopContext class to retrieve for targets?
	 * The default is false, as enabling this property may
	 * impair performance.
	 */
	private boolean exposeInvocation;

	private boolean exposeProxy;

	
	public ProxyConfig() {
	}

	public void copyFrom(ProxyConfig other) {
		this.enableCglibSubclassOptimizations = other.getEnableCglibSubclassOptimizations();
		this.proxyTargetClass = other.proxyTargetClass;
		this.exposeInvocation = other.exposeInvocation;
		this.exposeProxy = other.exposeProxy;
	}

	public boolean getProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * Set whether to proxy the target class directly as well as any interfaces.
	 * We can set this to true to force CGLIB proxying. Default is false
	 * @param proxyTargetClass whether to proxy the target class directly as well as any interfaces
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}
	
	/**
	 * @return return whether CGLIB-enhanced proxies will optimize out
	 * overriding methods with no advice chain. Can produce 2.5x performance
	 * improvement for methods with no advice.
	 */
	public boolean getEnableCglibSubclassOptimizations() {
		return this.enableCglibSubclassOptimizations;
	}

	/**
	 * Set whether CGLIB-enhanced proxies will optimize out
	 * overriding methods with no advice chain. Can produce 2.5x performance
	 * improvement for methods with no advice. Default is false.
	 * <br><b>Warning:</b> Setting this to true can produce large performance
	 * gains when using CGLIB (also set proxyTargetClass to true), so it's
	 * a good setting for performance-critical proxies. However, enabling this
	 * will mean that advice cannot be changed after a proxy has been obtained
	 * from this factory.
	 * @param enableCglibSubclassOptimizations The enableCglibSubclassOptimizations to set.
	 */
	public void setEnableCglibSubclassOptimizations(boolean enableCglibSubclassOptimizations) {
		this.enableCglibSubclassOptimizations = enableCglibSubclassOptimizations;
	}


	/**
	 * Set whether the AopContext class will be usable by target objects.
	 * @param exposeInvocation The exposeInvocation to set
	 */
	public final void setExposeInvocation(boolean exposeInvocation) {
		this.exposeInvocation = exposeInvocation;
	}

	/**
	 * Return whether the AopContext class will be usable by target objects.
	 */
	public final boolean getExposeInvocation() {
		return exposeInvocation;
	}

	public final boolean getExposeProxy() {
		return this.exposeProxy;
	}

	public final void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("exposeProxy=" + exposeProxy + "; ");
		sb.append("exposeInvocation=" + exposeInvocation + "; ");
		sb.append("enableCglibSubclassOptimizations=" + enableCglibSubclassOptimizations + "; ");
		return sb.toString();
	}

}
