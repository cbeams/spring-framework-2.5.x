/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jndi;

import javax.naming.NamingException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean that looks up a JNDI object. Exposes the object found in JNDI
 * when used as bean reference, e.g. for JdbcTemplate's "dataSource" property
 * in case of a <code>javax.sql.DataSource</code>.
 *
 * <p>The typical usage will be to register this as singleton factory
 * (e.g. for a certain JNDI DataSource) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>The default behavior is to look up the JNDI object on startup and
 * cache it. This can be customized through the "lookupOnStartup" and
 * "cache" properties, using a JndiObjectTargetSource underneath.
 * Note that you need to specify a "proxyInterface" in such a scenario,
 * because the actual JNDI object type is not known in advance.
 *
 * <p>Of course, service implementations can lookup e.g. a DataSource from
 * JNDI themselves, but this class enables central configuration of the
 * JNDI name, and easy switching to non-JNDI replacements. The latter can
 * be used for test setups, standalone clients, etc.
 *
 * <p>Note that switching to e.g. DriverManagerDataSource is just a matter
 * of configuration: replace the definition of this FactoryBean with a
 * DriverManagerDataSource definition!
 *
 * @author Juergen Hoeller
 * @since 22.05.2003
 * @see #setProxyInterface
 * @see #setLookupOnStartup
 * @see #setCache
 * @see JndiObjectTargetSource
 * @see javax.sql.DataSource
 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
 * @see org.springframework.jdbc.datasource.DriverManagerDataSource
 */
public class JndiObjectFactoryBean extends JndiObjectLocator implements FactoryBean {

	private Class proxyInterface;

	private boolean lookupOnStartup = true;

	private boolean cache = true;

	private Object jndiObject;


	/**
	 * Specify the proxy interface to use for the JNDI object.
	 * Needs to be specified because the actual JNDI object type is not known
	 * in advance in case of a lazy lookup.
	 * <p>Typically used in conjunction with "lookupOnStartup"=false and/or "cache"=false.
	 * @see #setLookupOnStartup
	 * @see #setCache
	 */
	public void setProxyInterface(Class proxyInterface) {
		if (!proxyInterface.isInterface()) {
			throw new IllegalArgumentException("[" + proxyInterface.getName() + "] is not an interface");
		}
		this.proxyInterface = proxyInterface;
	}

	/**
	 * Set whether to look up the JNDI object on startup. Default is true.
	 * <p>Can be turned off to allow for late availability of the JNDI object.
	 * In this case, the JNDI object will be fetched on first access.
	 * <p>For a lazy lookup, a proxy interface needs to be specified.
	 * @see #setProxyInterface
	 * @see #setCache
	 */
	public void setLookupOnStartup(boolean lookupOnStartup) {
		this.lookupOnStartup = lookupOnStartup;
	}

	/**
	 * Set whether to cache the JNDI object once it has been located.
	 * Default is true.
	 * <p>Can be turned off to allow for hot redeployment of JNDI objects.
	 * In this case, the JNDI object will be fetched for each invocation.
	 * <p>For hot redeployment, a proxy interface needs to be specified.
	 * @see #setProxyInterface
	 * @see #setLookupOnStartup
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	/**
	 * Look up the JNDI object and store it.
	 */
	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();

		if (this.proxyInterface != null) {
			// We need a proxy and a JndiObjectTargetSource.
			this.jndiObject = JndiObjectProxyFactory.createJndiObjectProxy(this);
		}
		else {
			if (!this.lookupOnStartup || !this.cache) {
				throw new IllegalArgumentException(
				    "Cannot deactivate 'lookupOnStartup' or 'cache' without specifying a 'proxyInterface'");
			}
			this.jndiObject = lookup();
		}
	}


	/**
	 * Return the singleton JNDI object.
	 */
	public Object getObject() {
		return this.jndiObject;
	}

	public Class getObjectType() {
		if (this.jndiObject != null) {
			return this.jndiObject.getClass();
		}
		else if (this.proxyInterface != null) {
			return this.proxyInterface;
		}
		else {
			return null;
		}
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Inner class to just introduce an AOP dependency
	 * when actually creating a proxy.
	 */
	private static class JndiObjectProxyFactory {

		private static Object createJndiObjectProxy(JndiObjectFactoryBean jof) throws NamingException {
			// Create a JndiObjectTargetSource that mirrors the JndiObjectFactoryBean's configuration.
			JndiObjectTargetSource targetSource = new JndiObjectTargetSource();
			targetSource.setJndiTemplate(jof.getJndiTemplate());
			targetSource.setJndiName(jof.getJndiName());
			targetSource.setResourceRef(jof.isResourceRef());
			targetSource.setLookupOnStartup(jof.lookupOnStartup);
			targetSource.setCache(jof.cache);
			targetSource.afterPropertiesSet();

			// Create a proxy with JndiObjectFactoryBean's proxy interface and the JndiObjectTargetSource.
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.addInterface(jof.proxyInterface);
			proxyFactory.setTargetSource(targetSource);
			return proxyFactory.getProxy();
		}
	}

}
