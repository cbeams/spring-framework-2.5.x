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

package org.springframework.jdbc.support;

import java.sql.Types;

import junit.framework.TestCase;

/**
 * TODO this test case needs attention: I wrote it based on Isabelle's documentation
 * and it appears that JdbcUtils doesn't work exactly as documented.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class JdbcUtilsTests extends TestCase {

	/**
	 * Constructor for JdbcUtilsTests.
	 * @param arg0
	 */
	public JdbcUtilsTests(String arg0) {
		super(arg0);
	}

	/**
	 */
	public void testCountParameterPlaceholders() {
		assertTrue(JdbcUtils.countParameterPlaceholders(null, '?', '\'') == 0);

		assertTrue(JdbcUtils.countParameterPlaceholders("", '?', '\'') == 0);

		assertTrue(JdbcUtils.countParameterPlaceholders("?", '?', '\'') == 1);

		assertTrue(JdbcUtils.countParameterPlaceholders("The big ? 'bad wolf'", '?', '\'') == 1);
		
		assertTrue(JdbcUtils.countParameterPlaceholders("The big ?? bad wolf", '?', '\'') == 2);
		
		assertTrue(JdbcUtils.countParameterPlaceholders("The big 'ba''ad?' ? wolf", '?', '\'') == 1);

		assertTrue(JdbcUtils.countParameterPlaceholders(null, '?', "\"'") == 0);

		assertTrue(JdbcUtils.countParameterPlaceholders("", '?', "\"'") == 0);

		assertTrue(JdbcUtils.countParameterPlaceholders("?", '?', "\"'") == 1);

		assertTrue(JdbcUtils.countParameterPlaceholders("The \"big\" ? 'bad wolf'", '?', "\"'") == 1);
		
		assertTrue(JdbcUtils.countParameterPlaceholders("The big ?? bad wolf", '?', "\"'") == 2);
		
		assertTrue(JdbcUtils.countParameterPlaceholders("The \"big?\" 'ba''ad?' ? wolf", '?', "\"'") == 1);
}

	public void testIsNumeric() {
		assertTrue(JdbcUtils.isNumeric(Types.BIGINT));
		assertTrue(JdbcUtils.isNumeric(Types.NUMERIC));
		assertTrue(JdbcUtils.isNumeric(Types.INTEGER));
		assertTrue(JdbcUtils.isNumeric(Types.FLOAT));
		assertTrue(!JdbcUtils.isNumeric(Types.VARCHAR));
	}

	public void testTranslateType() {
		assertTrue(JdbcUtils.translateType(Types.VARCHAR) == Types.VARCHAR);
		assertTrue(JdbcUtils.translateType(Types.CHAR) == Types.VARCHAR);
	}

}
