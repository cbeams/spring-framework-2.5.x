/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jndi.support;

import javax.naming.NamingException;

import org.springframework.jndi.JndiTemplate;

/**
 * Simple implementation of JndiTemplate interface that always returns
 * a given object. Very useful for testing. Effectively a mock object.
 * @author Rod Johnson
 * @see org.springframework.jdbc.datasource.DriverManagerDataSource
 * @version $Id: ExpectedLookupTemplate.java,v 1.3 2004-02-09 10:46:58 jhoeller Exp $
 */
public class ExpectedLookupTemplate extends JndiTemplate {

	private final String name;

	private final Object object;

	/**
	 * Construct a new JndiTemplate that will always return the
	 * given object, but honour only requests for the given name.
	 * @param name the name the client is expected to look up
	 * @param object the object that will be returned
	 */
	public ExpectedLookupTemplate(String name, Object object) {
		this.name = name;
		this.object = object;
	}

	/**
	 * If the name is the expected name specified in the constructor,
	 * return the object provided in the constructor. If the name is
	 * unexpected, a respective NamingException gets thrown.
	 */
	public Object lookup(String name) throws NamingException {
		if (!name.equals(this.name)) {
			throw new NamingException("Unexpected JNDI name '" + name + "' - expecting '" + this.name + "'");
		}
		return this.object;
	}

}
