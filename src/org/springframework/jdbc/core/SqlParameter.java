/*
 * Copyright 2002-2005 the original author or authors.
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

import java.util.LinkedList;
import java.util.List;

/**
 * Object to represent a SQL parameter definition.
 *
 * <p>Parameters may be anonymous, in which case name is null.
 * However, all parameters must define a SQL type constant from
 * <code>java.sql.Types</code>.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @see java.sql.Types
 */
public class SqlParameter {

	private String name;
	
	/** SQL type constant from java.sql.Types */
	private int sqlType;

	/** used for types that are user-named like: STRUCT, DISTINCT, JAVA_OBJECT, and named array types */
	private String typeName;


	/**
	 * Create a new anonymous SqlParameter, supplying SQL type.
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 */
	public SqlParameter(int sqlType) {
		this(null, sqlType, (String) null);
	}

	/**
	 * Create a new anonymous SqlParameter, supplying SQL type.
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 */
	public SqlParameter(int sqlType, String typeName) {
		this(null, sqlType, typeName);
	}

	/**
	 * Create a new SqlParameter, supplying name and SQL type.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 */
	public SqlParameter(String name, int sqlType) {
		this(name, sqlType, (String) null);
	}
	
	/**
	 * Create a new SqlParameter, supplying name and SQL type.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 */
	public SqlParameter(String name, int sqlType, String typeName) {
		this.name = name;
		this.sqlType = sqlType;
		this.typeName = typeName;
	}

	/**
	 * Return the name of the parameter.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the SQL type of the parameter.
	 */
	public int getSqlType() {
		return sqlType;
	}

	/**
	 * Return the type name of the parameter, if any.
	 */
	public String getTypeName() {
		return typeName;
	}


	/**
	 * Convert a list of JDBC types, as defined in <code>java.sql.Types</code>,
	 * to a List of SqlParameter objects as used in this package.
	 */
	public static List sqlTypesToAnonymousParameterList(int[] types) {
		List result = new LinkedList();
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				result.add(new SqlParameter(types[i]));
			}
		}
		return result;
	}

}
