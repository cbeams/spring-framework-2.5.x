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

package org.springframework.jdbc.core;

import java.util.Map;

/**
 * Interface defining common functionality need for implementations that will hold parameter values for named
 * parameters used by SqlCommand.  This interface provides for the specification of SQL type in addition to
 * parameter values. All parameter values and types are identified by specifying the name of the parameter.
 * This clas provides methods that will make adding several values easier.  The addValue method returns a
 * reference to the Map itself so you can string several method calls together within a single statement.
 *
 * <p>Intended to wrap various implementatations like a Map or
 * a JavaBean with a consistent interface.
 *
 * @author Thomas Risberg
 * @since 2.0
 * @see org.springframework.jdbc.core.NamedParameterJdbcOperations
 * @see org.springframework.jdbc.core.SqlNamedParameterWrapper
 * @see org.springframework.jdbc.core.SqlParameterBeanWrapper
 * //ToDo: @see org.springframework.jdbc.object.SqlCommand
 */
public interface SqlNamedParameterHolder {

	/**
	 * Set the Map of named values corresponding to the named parameters.
	 * @param valueMap Map containing parameter values keyed by parameter name
	 */
	public void setValues(Map valueMap);

	/**
	 * Get the Map of named values corresponding to the named parameters.
	 * @return Map containing parameter values keyed by parameter name
	 */
	public Map getValues();

	/**
	 * Set the Map of named types corresponding to the named parameters.
	 * @param typeMap Map containing parameter types keyed by parameter name
	 */
	public void setTypes(Map typeMap);

	/**
	 * Get the Map of named types corresponding to the named parameters.
	 * @return Map containing parameter types keyed by parameter name
	 */
	public Map getTypes();

}
