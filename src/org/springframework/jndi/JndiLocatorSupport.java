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

import java.util.Properties;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenient superclass for classes that can locate any number of JNDI objects.
 * Subclasses are JavaBeans, exposing jndiTemplate and jndiEnvironment properties.
 *
 * <p>JNDI names may or may not include the "java:comp/env/" prefix expected
 * by J2EE applications when accessing a locally mapped (ENC - Environmental
 * Naming Context) resource. If it doesn't, the "java:comp/env/" prefix will
 * be prepended if the "resourceRef" property is true (the default is
 * <strong>false</strong>) and no other scheme like "java:" is given.
 *
 * @author Juergen Hoeller
 * @since 20.07.2004
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 * @see #setResourceRef
 */
public abstract class JndiLocatorSupport {

	/** JNDI prefix used in a J2EE container */
	public static String CONTAINER_PREFIX = "java:comp/env/";


	protected final Log logger = LogFactory.getLog(getClass());

	private JndiTemplate jndiTemplate = new JndiTemplate();

	private boolean resourceRef = false;


	/**
	 * Set the JNDI template to use for the JNDI lookup.
	 * You can also specify JNDI environment settings via setJndiEnvironment.
	 * @see #setJndiEnvironment
	 */
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		this.jndiTemplate = jndiTemplate;
	}

	/**
	 * Return the JNDI template to use for the JNDI lookup.
	 */
	public JndiTemplate getJndiTemplate() {
		return jndiTemplate;
	}

	/**
	 * Set the JNDI environment to use for the JNDI lookup.
	 * Creates a JndiTemplate with the given environment settings.
	 * @see #setJndiTemplate
	 */
	public void setJndiEnvironment(Properties jndiEnvironment) {
		this.jndiTemplate = new JndiTemplate(jndiEnvironment);
	}

	/**
	 * Return the JNDI enviromment to use for the JNDI lookup.
	 */
	public Properties getJndiEnvironment() {
		return jndiTemplate.getEnvironment();
	}

	/**
	 * Set if the lookup occurs in a J2EE container, i.e. if the prefix
	 * "java:comp/env/" needs to be added if the JNDI name doesn't already
	 * contain it. Default is false.
	 * <p>Note: Will only get applied if no other scheme like "java:" is given.
	 */
	public void setResourceRef(boolean resourceRef) {
		this.resourceRef = resourceRef;
	}

	/**
	 * Return if the lookup occurs in a J2EE container.
	 */
	public boolean isResourceRef() {
		return resourceRef;
	}


	/**
	 * Perform an actual JNDI lookup for the given name via the JndiTemplate.
   * If the name doesn't begin with "java:comp/env/", this prefix is added
	 * if resourceRef is set to true.
	 * @param jndiName the JNDI name to look up
	 * @throws NamingException if the JNDI lookup failed
	 * @see #setResourceRef
	 */
	protected Object lookup(String jndiName) throws NamingException {
		String jndiNameToUse = convertJndiName(jndiName);
		Object jndiObject = getJndiTemplate().lookup(jndiNameToUse);
		if (logger.isInfoEnabled()) {
			logger.info("Located object with JNDI name '" + jndiNameToUse + "': value=[" + jndiObject + "]");
		}
		return jndiObject;
	}

	/**
	 * Convert the given JNDI name to the actual JNDI name to use.
	 * Default implementation applies the "java:comp/env/" prefix if
	 * resourceRef is true and no other scheme like "java:" is given.
	 * @param jndiName the original JNDI name
	 * @return the JNDI name to use
	 * @see #CONTAINER_PREFIX
	 * @see #setResourceRef
	 */
	protected String convertJndiName(String jndiName) {
		// prepend container prefix if not already specified and no other scheme given
		if (isResourceRef() && !jndiName.startsWith(CONTAINER_PREFIX) && jndiName.indexOf(':') == -1) {
			jndiName = CONTAINER_PREFIX + jndiName;
		}
		return jndiName;
	}

}
