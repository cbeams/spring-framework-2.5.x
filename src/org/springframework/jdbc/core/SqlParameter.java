/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.util.List;
import java.util.LinkedList;

/**
 * Object to represent a SQL parameter definition.
 * Parameters may be anonymous, in which case name is null.
 * However all parameters must define a SQL type constant
 * from java.sql.Types.
 * @author Rod Johnson
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
		this(null, type, null);
	}

	public SqlParameter(int type, String typeName) {
		this(null, type, typeName);
	}

	public SqlParameter(String name, int type) {
		this(name, type, null);
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
