/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

/**
 * Convenience superclass for configuration to do with creating proxies.
 * @author Rod Johnson
 * @version $Id: ProxyConfig.java,v 1.2 2003-12-01 18:28:24 johnsonr Exp $
 */
public class ProxyConfig {

	private boolean proxyTargetClass;
	
	private boolean enableCglibSubclassOptimizations;


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
	
	public void copyFrom(ProxyConfig other) {
		this.enableCglibSubclassOptimizations = other.getEnableCglibSubclassOptimizations();
		this.proxyTargetClass = other.proxyTargetClass;
	}

}
