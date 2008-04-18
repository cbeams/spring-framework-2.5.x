/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.test.jdbc;

import java.util.List;

/**
 * JdbcTestUtils is a collection of JDBC related utility methods for
 * use in unit and integration testing scenarios.
 *
 * @author Thomas Risberg
 * @since 2.5.4
 */
public class JdbcTestUtils {

	/**
	 * Split an SQL script into separate statements delimited with the provided delimiter character. Each
	 * individual statement will be added to the provided <code>List</code>.
	 * @param script the SQL script
	 * @param delim charecter delimiting each statement - tpically a ';' character
	 * @param statements the List that will contain the individual statements
	 */
	public static void splitSqlScript(String script, char delim, List statements) {
		StringBuffer sb = new StringBuffer();
		boolean inLiteral = false;
		char[] content = script.toCharArray();

		for (int i = 0; i < script.length(); i++) {
			if (content[i] == '\'') {
				inLiteral = inLiteral ? false : true;
			}
			if (content[i] == delim && !inLiteral) {
				if (sb.length() > 0) {
					statements.add(sb.toString().trim());
					sb = new StringBuffer();
				}
			}
			else {
				sb.append(content[i]);
			}
		}
		if (sb.length() > 0) {
			statements.add(sb.toString().trim());
		}
	}

}
