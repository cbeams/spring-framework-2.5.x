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

package org.springframework.jdbc.core;

import java.util.LinkedList;
import java.util.List;

/**
 * Object to represent a SQL parameter definition.
 * Parameters may be anonymous, in which case name is null.
 * However all parameters must define a SQL type constant
 * from java.sql.Types.
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public class SqlParameter {

	private String name;
	
	/** SQL type constant from java.sql.Types */
	private int type;

	/** used for types that are user-named like: STRUCT, DISTINCT, JAVA_OBJECT, and named array types. */
	private String typeName;
			
	/**
	 * Add a new anonymous parameter
	 */
	public SqlParameter(int type) {
		this(null, type, (String)null);
	}

	public SqlParameter(int type, String typeName) {
		this(null, type, typeName);
	}

	public SqlParameter(String name, int type) {
		this(name, type, (String)null);
	}
	
	public SqlParameter(String name, int type, String typeName) {
		this.name = name;
		this.type = type;
		this.typeName = typeName;
	}

	public String getName() {
		return name;
	}
	
	public int getSqlType() {
		return type;
	}

	public String getTypeName() {
		return typeName;
	}

	/**
	 * Convert a list of JDBC types, as defined in the java.sql.Types class,
	 * to a List of SqlParameter objects as used in this package
	 */
	public static List sqlTypesToAnonymousParameterList(int[] types) {
		List l = new LinkedList();
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				l.add(new SqlParameter(types[i]));
			}
		}
		return l;
	}

}
