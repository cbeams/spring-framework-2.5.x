/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.jdbc.support;

import java.sql.Types;

import junit.framework.TestCase;

/**
 * TODO this test case needs attention: I wrote it based on Isabelle's documentation
 * and it appears that JdbcUtils doesn't work exactly as documented.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: JdbcUtilsTests.java,v 1.2 2003-12-08 12:56:36 trisberg Exp $
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
