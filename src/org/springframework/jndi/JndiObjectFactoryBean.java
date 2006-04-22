/*
 * Copyright 2002-2006 the original author or authors.
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

	private Object defaultObject;

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
	 * Set whether to look up the JNDI object on startup. Default is "true".
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
	 * Default is "true".
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
	 * Specify a default object to fall back to if the JNDI lookup fails.
	 * Default is none.
	 * <p>This can be an arbitrary bean reference or literal value.
	 * It is typically used for literal values in scenarios where the JNDI environment
	 * might define specific config settings but those are not required to be present.
	 * <p>Note: This is only supported for lookup on startup.
	 * @see #setLookupOnStartup
	 */
	public void setDefaultObject(Object defaultObject) {
		this.defaultObject = defaultObject;
	}


	/**
	 * Look up the JNDI object and store it.
	 */
	public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
		super.afterPropertiesSet();

		if (this.proxyInterface != null) {
			if (this.defaultObject != null) {
				throw new IllegalArgumentException("'defaultObject' is not supported in combination with proxyInterface");
			}
			// We need a proxy and a JndiObjectTargetSource.
			this.jndiObject = JndiObjectProxyFactory.createJndiObjectProxy(this);
		}

		else {
			if (!this.lookupOnStartup || !this.cache) {
				throw new IllegalArgumentException(
				    "Cannot deactivate 'lookupOnStartup' or 'cache' without specifying a 'proxyInterface'");
			}
			if (this.defaultObject != null && getExpectedType() != null &&
					!getExpectedType().isInstance(this.defaultObject)) {
				throw new IllegalArgumentException("Default object [" + this.defaultObject +
						"] of type [" + this.defaultObject.getClass().getName() +
						"] is not of expected type [" + getExpectedType().getName() + "]");
			}
			// Locate specified JNDI object.
			this.jndiObject = lookupWithFallback();
		}
	}

	/**
	 * Lookup variant that that returns the specified "defaultObject"
	 * (if any) in case of lookup failure.
	 * @return the located object, or the "defaultObject" as fallback
	 * @throws NamingException in case of lookup failure without fallback
	 * @see #setDefaultObject
	 */
	protected Object lookupWithFallback() throws NamingException {
		try {
			return lookup();
		}
		catch (TypeMismatchNamingException ex) {
			// Always let TypeMismatchNamingException through -
			// we don't want to fall back to the defaultObject in this case.
			throw ex;
		}
		catch (NamingException ex) {
			if (this.defaultObject != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("JNDI lookup failed - returning specified default object instead", ex);
				}
				else if (logger.isInfoEnabled()) {
					logger.debug("JNDI lookup failed - returning specified default object instead: " + ex);
				}
				return this.defaultObject;
			}
			throw ex;
		}
	}


	/**
	 * Return the singleton JNDI object.
	 */
	public Object getObject() {
		return this.jndiObject;
	}

	public Class getObjectType() {
		if (this.proxyInterface != null) {
			return this.proxyInterface;
		}
		else if (this.jndiObject != null) {
			return this.jndiObject.getClass();
		}
		else {
			return getExpectedType();
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
			targetSource.setExpectedType(jof.getExpectedType());
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
