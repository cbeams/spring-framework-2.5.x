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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Convenient superclass for JNDI-based service locators. Subclasses are
 * JavaBeans, exposing a jndiName property. This may or may not include
 * the "java:comp/env/" prefix expected by J2EE applications when accessing
 * a locally mapped (ENC - Environmental Naming Context) resource. If it
 * doesn't, the "java:comp/env/" prefix will be prepended if the "resourceRef"
 * property is true (the default is <strong>false</strong>) and no other scheme
 * like "java:" is given.
 *
 * <p>Subclasses must implement the located() method to cache the results
 * of the JNDI lookup. They don't need to worry about error handling.</p>
 * 
 * <p><b>Assumptions:</b> The resource obtained from JNDI can be cached.
 * 
 * <p>Subclasses will often be used as singletons in a bean container. This
 * sometimes presents a problem if that bean container pre-instantiates singletons,
 * since this class does the JNDI lookup in its init method, but the resource being
 * pointed to may not exist at that time, even though it may exist at the time of
 * first usage. The solution is to tell the bean container not to pre-instantiate
 * this class (i.e. lazily initialize it instead).<p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setJndiName
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 * @see #setResourceRef
 */
public abstract class AbstractJndiLocator extends JndiLocatorSupport implements InitializingBean {

	private String jndiName;

	/**
	 * Set the JNDI name to look up. If it doesn't begin with "java:comp/env/"
	 * this prefix is added if resourceRef is set to true.
	 * @param jndiName JNDI name to look up
	 * @see #setResourceRef
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	/**
	 * Return the JNDI name to look up.
	 */
	public String getJndiName() {
		return jndiName;
	}

	/**
	 * Check the jndiName property and initiate a lookup.
	 * <p>The JNDI object will thus be fetched eagerly on initialization.
	 * For refreshing the JNDI object, subclasses can invoke <code>lookup</code>
	 * at any later time.
	 * @see #lookup
	 */
	public void afterPropertiesSet() throws NamingException {
		if (!StringUtils.hasLength(getJndiName())) {
			throw new IllegalArgumentException("jndiName is required");
		}
		lookup();
	}

	/**
	 * Perform the actual JNDI lookup via the JndiTemplate.
	 * Invokes the <code>located</code> method after successful lookup.
	 * @throws NamingException if the JNDI lookup failed
	 * @see #located
	 */
	protected void lookup() throws NamingException {
		Object jndiObject = lookup(getJndiName());
		located(jndiObject);
	}

	/**
	 * Subclasses must implement this to cache the object this class has obtained from JNDI.
	 * @param jndiObject object successfully retrieved from JNDI
	 */
	protected abstract void located(Object jndiObject);

}
