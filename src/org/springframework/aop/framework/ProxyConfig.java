/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

/**
 * Convenience superclass for configuration to do with creating proxies.
 * @author Rod Johnson
 * @version $Id: ProxyConfig.java,v 1.1 2003-12-01 15:40:46 johnsonr Exp $
 */
public class ProxyConfig {

	private boolean proxyTargetClass;

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

}
