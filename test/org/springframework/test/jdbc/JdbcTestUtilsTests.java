/*
 * Copyright 2002-2008 the original author or authors.
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

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests for {@link JdbcTestUtils}.
 *
 * @author Thomas Risberg
 * @since 2.5.4
 */
public class JdbcTestUtilsTests extends TestCase {

	public void testCountDelimiters() {
		assertEquals("count with ';' is wrong", 2, JdbcTestUtils.countSqlScriptDelimiters("select 1; select ';';", ';'));		
		assertEquals("count with '\\n' is wrong", 2, JdbcTestUtils.countSqlScriptDelimiters("select 1\n select '\\n\n'\n", '\n'));		
	}

	public void testSplitSqlScriptDelimitedWithSemicolon() {
		String statement1 = "insert into customer (id, name) \n" +
				"values (1, 'Rod ; Johnson'), (2, 'Adrian \n Collier')";
		String statement2 = "insert into orders(id, order_date, customer_id) \n" +
				"values (1, '2008-01-02', 2)";
		String statement3 = "insert into orders(id, order_date, customer_id) " +
				"values (1, '2008-01-02', 2)";
		char delim = ';';
		String script = statement1 + delim + statement2 + delim + statement3;
		List statements = new ArrayList();
		JdbcTestUtils.splitSqlScript(script, delim, statements);
		assertEquals("wrong number of statements", 3, statements.size());
		assertEquals("statement 1 not split correctly", statement1, statements.get(0));
		assertEquals("statement 2 not split correctly", statement2, statements.get(1));
		assertEquals("statement 3 not split correctly", statement3, statements.get(2));

	}

	public void testSplitSqlScriptDelimitedWithNewLine() {
		String statement1 = "insert into customer (id, name) " +
				"values (1, 'Rod ; Johnson'), (2, 'Adrian ; Collier')";
		String statement2 = "insert into orders(id, order_date, customer_id) " +
				"values (1, '2008-01-02', 2)";
		String statement3 = "insert into orders(id, order_date, customer_id) " +
				"values (1, '2008-01-02', 2)";
		char delim = '\n';
		String script = statement1 + delim + statement2 + delim + statement3;
		List statements = new ArrayList();
		JdbcTestUtils.splitSqlScript(script, delim, statements);
		assertEquals("wrong number of statements", 3, statements.size());
		assertEquals("statement 1 not split correctly", statement1, statements.get(0));
		assertEquals("statement 2 not split correctly", statement2, statements.get(1));
		assertEquals("statement 3 not split correctly", statement3, statements.get(2));

	}
}
