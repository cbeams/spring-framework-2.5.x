/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core.simple;

/**
 * Utility class used for the Simple JDBC functionality.
 *
 * @author trisberg
 */
public class SimpleJdbcUtils {

	/**
	 * Extract a common name for the database in use even if various drivers/platforms provide varying names.
	 * @param source the name as provided in database metedata
	 * @return the common name to be used
	 */
	public static String commonDatabaseName(String source) {
		String name = source;
		if (source != null && source.startsWith("DB2")) {
			name = "DB2";
		}
		else if ( "Sybase SQL Server".equals(source) || "Adaptive Server Enterprise".equals(source) || "sql server".equals(source) ) {
			name = "Sybase";
		}
		return name; 
	}

	/**
	 * Convert a column name with undercores to the corresponding property name using "camel case".  A name
	 * like "customer_number" would match a "customerNumber" property name.
	 * @param name the column name to be converted
	 * @return the name using "camel case"
	 */
	public static String convertUnderscoreNameToPropertyName(String name) {
		StringBuffer result = new StringBuffer();
		boolean nextIsUpper = false;
		if (name != null && name.length() > 0) {
			if (name.length() > 1 && name.substring(1,2).equals("_"))
				result.append(name.substring(0, 1).toUpperCase());
			else
				result.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				if (s.equals("_")) {
					nextIsUpper = true;
				}
				else {
					if (nextIsUpper) {
						result.append(s.toUpperCase());
						nextIsUpper = false;
					}
					else {
						result.append(s.toLowerCase());
					}
				}
			}
		}
		return result.toString();
	}
}
