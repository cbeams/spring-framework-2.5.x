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

package org.springframework.mock.jndi;

import javax.naming.NamingException;

import org.springframework.jndi.JndiTemplate;

/**
 * Simple implementation of JndiTemplate interface that always returns
 * a given object. Very useful for testing. Effectively a mock object.
 * @author Rod Johnson
 * @see org.springframework.jdbc.datasource.DriverManagerDataSource
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
