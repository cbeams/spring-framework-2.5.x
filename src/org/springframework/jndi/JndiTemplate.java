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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class that simplifies JNDI operations. It provides methods to lookup
 * and bind objects, and allows implementations of the JndiCallback interface
 * to perform any operation they like with a JNDI naming context provided.
 *
 * <p>This is the central class in this package. It is analogous to the
 * JdbcTemplate class. This class performs all JNDI context handling.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see JndiCallback
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @version $Id: JndiTemplate.java,v 1.6 2004-03-18 02:46:17 trisberg Exp $
 */
public class JndiTemplate {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private Properties environment;

	/**
	 * Create a new JndiTemplate instance.
	 */
	public JndiTemplate() {
	}

	/**
	 * Create a new JndiTemplate instance, using the given environment.
	 */
	public JndiTemplate(Properties environment) {
		this.environment = environment;
	}

	/**
	 * Set the environment for the JNDI InitialContext.
	 */
	public void setEnvironment(Properties environment) {
		this.environment = environment;
	}

	/**
	 * Return the environment for the JNDI InitialContext.
	 */
	public Properties getEnvironment() {
		return environment;
	}

	/**
	 * Execute the given JNDI context callback implementation.
	 * @param contextCallback JndiCallback implementation
	 * @return a result object returned by the callback, or null
	 * @throws NamingException thrown by the callback implementation
	 * @see #createInitialContext
	 */
	public Object execute(JndiCallback contextCallback) throws NamingException {
		Context ctx = createInitialContext();
		try {
			return contextCallback.doInContext(ctx);
		}
		finally {
			try {
				ctx.close();
			}
			catch (NamingException ex) {
				logger.warn("Could not close JNDI InitialContext", ex);
			}
		}
	}

	/**
	 * Create a new JNDI initial context. Invoked by execute.
	 * The default implementation use this template's environment settings.
	 * Can be subclassed for custom contexts, e.g. for testing.
	 * @return the initial Context instance
	 * @throws NamingException in case of initialization errors
	 */
	protected Context createInitialContext() throws NamingException {
		return new InitialContext(getEnvironment());
	}

	/**
	 * Look up the object with the given name in the current JNDI context.
	 * @param name the JNDI name of the object
	 * @return object found (cannot be null; if a not so well-behaved
	 * JNDI implementations returns null, a NamingException gets thrown)
	 * @throws NamingException if there is no object with the given
	 * name bound to JNDI
	 */
	public Object lookup(final String name) throws NamingException {
		return execute(new JndiCallback() {
			public Object doInContext(Context ctx) throws NamingException {
				logger.debug("Looking up JNDI object with name '" + name + "'");
				Object located = ctx.lookup(name);
				if (located == null) {
					throw new NamingException("JNDI object with '" + name +
																		"' not found: JNDI implementation returned null");
				}
				return located;
			}
		});
	}

	/**
	 * Bind the given object to the current JNDI context, using the given name.
	 * @param name the JNDI name of the object
	 * @param object the object to bind
	 * @throws NamingException thrown by JNDI, mostly name already bound
	 */
	public void bind(final String name, final Object object) throws NamingException {
		execute(new JndiCallback() {
			public Object doInContext(Context ctx) throws NamingException {
				logger.info("Binding JNDI object with name '" + name + "'");
				ctx.bind(name, object);
				return null;
			}
		});
	}
	
	/**
	 * Remove the binding for the given name from the current JNDI context.
	 * @param name the JNDI name of the object
	 * @throws NamingException thrown by JNDI, mostly name not found
	 */
	public void unbind(final String name) throws NamingException {
		execute(new JndiCallback() {
			public Object doInContext(Context ctx) throws NamingException {
				logger.info("Unbinding JNDI object with name '" + name + "'");
				ctx.unbind(name);
				return null;
			}
		});
	}
	
}
