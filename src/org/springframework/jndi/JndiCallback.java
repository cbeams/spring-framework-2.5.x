/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Callback interface to be implemented by classes that need to perform an
 * operation (such as a lookup) in a JNDI context. This callback approach
 * is valuable in simplifying error handling, which is performed by the
 * JndiTemplate class. This is a similar to JdbcTemplate's approach.
 *
 * <p>Note that there is hardly any need to implement this callback
 * interface, as JndiTemplate provides all usual JNDI operations via
 * convenience methods.
 *
 * @see JndiTemplate
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @author Rod Johnson
 */
public interface JndiCallback {
    
	/**
	 * Do something with the given JNDI context.
	 * Implementations don't need to worry about error handling
	 * or cleanup, as the JndiTemplate class will handle this.
	 * @param ctx the current JNDI context
	 * @throws NamingException if thrown by JNDI methods
	 * @return a result object, or null
	 */
  Object doInContext(Context ctx) throws NamingException;

}

