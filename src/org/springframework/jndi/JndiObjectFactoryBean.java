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

package org.springframework.jndi;

import javax.naming.NamingException;

import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean that looks up a JNDI object. Behaves like the object when
 * used as bean reference, e.g. for JdbcTemplate's dataSource property.
 *
 * <p>The typical usage will be to register this as singleton factory
 * (e.g. for a certain JNDI DataSource) in an application context,
 * and give bean references to application services that need it.
 *
 * <p><b>Assumptions:</b> The resource obtained from JNDI is available
 * at context startup time and can be cached. If this is not the case,
 * consider using a ProxyFactoryBean with JndiObjectTargetSource,
 * which fetches objects from JNDI on demand.
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
 * @see JndiObjectTargetSource
 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
 */
public class JndiObjectFactoryBean extends JndiObjectLocator implements FactoryBean {

	private Object jndiObject;

	/**
	 * Look up the JNDI object and store it.
	 */
	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();
		this.jndiObject = lookup();
	}

	/**
	 * Return the singleton JNDI object.
	 */
	public Object getObject() {
		return this.jndiObject;
	}

	public Class getObjectType() {
		return (this.jndiObject != null) ? this.jndiObject.getClass() : null;
	}

	public boolean isSingleton() {
		return true;
	}

}
