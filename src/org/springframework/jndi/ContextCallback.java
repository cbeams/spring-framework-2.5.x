/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Callback interface to be implemented by classes that
 * need to perform an operation (such as a lookup) in a 
 * JNDI context. This callback approach is valuable in
 * simplifying error handling, which is performed
 * by the JndiTemplate class. This is a similar approach to
 * that used by the JdbcTemplate class.
 * @see org.springframework.jndi.JndiTemplate
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @author Rod Johnson
 */
public interface ContextCallback {
    
	/**
	 * Do something with the given JNDI context.
	 * Implementations don't need to worry about error handling
	 * or cleanup, as the JndiTemplate class will handle this.
	 * @param ctx  the current JNDI context
	 * @throws NamingException  Implementations don't need
	 * to catch naming exceptions
	 * @return  a result object, or null
	 */
  Object doInContext(Context ctx) throws NamingException;

}

