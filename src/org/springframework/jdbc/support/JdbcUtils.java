/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.support;

import java.sql.Types;

/**
 * Utility methods for SQL statements.
 * @author Isabelle Muszynski
 * @author Thomas Risberg
 * @version $Id: JdbcUtils.java,v 1.2 2003-12-08 12:56:11 trisberg Exp $
 */
public class JdbcUtils {

	/**
	 * Count the occurrences of the character <code>placeholder</code> in an SQL string <code>str</code>.
	 * <p>
	 * The character <code>placeholder</code> is not counted if it appears within a literal as determined
	 * by the <code>delim</code> that is passed in.
	 * <p>
	 * Examples : if the delimiter is the single quote, and the character to count the 
	 * occurrences of is the question mark, then
	 * <p>
	 * <code>The big ? 'bad wolf?'</code> gives a count of one
	 * <code>The big ?? bad wolf</code> gives a count of two
	 * <code>The big  'ba''ad?' ? wolf</code> gives a count of one
	 * <p>
	 * The grammar of the string passed in should obey the rules
	 * of the JDBC spec which is close to no rules at all.  One placeholder
	 * per parameter and it should be valid SQL for the target database.
	 * <p>
	 * @param str string to search in. Returns 0 if this is null
	 * @param placeholder the character to search for and count.
	 * @param delim the delimiter for character literals.
	 */
	public static int countParameterPlaceholders(String str, char placeholder, char delim) {
		int count = 0;
		boolean insideLiteral = false;
		
		for (int i = 0; str != null && i < str.length(); i++) {
			if (str.charAt(i) == placeholder) {
				if (!insideLiteral)
					count++;
			}
			else {
				if (str.charAt(i) == delim) {
					insideLiteral = insideLiteral ^ true;
				}
			}
		}

		return count;
	}

	/**
	 * Check that a SQL type is numeric
	 * @param sqlType the SQL type to be checked
	 * @return <code>true</code> if the type is numeric,
	 * <code>false</code> otherwise
	 */
	public static boolean isNumeric(int sqlType) {
		return Types.BIT == sqlType
			|| Types.BIGINT == sqlType
			|| Types.DECIMAL == sqlType
			|| Types.DOUBLE == sqlType
			|| Types.FLOAT == sqlType
			|| Types.INTEGER == sqlType
			|| Types.NUMERIC == sqlType
			|| Types.REAL == sqlType
			|| Types.SMALLINT == sqlType
			|| Types.TINYINT == sqlType;
	}

	/**
	 * Translate a SQL type into one of a few values.
	 * All integer types are translated to Integer.
	 * All real types are translated to Double.
	 * All string types are translated to String.
	 * All other types are left untouched.
	 * @param sqlType the type to be translated into a simpler type
	 * @return the new SQL type
	 */
	public static int translateType(int sqlType) {

		int retType = sqlType;
		if (Types.BIT == sqlType || Types.TINYINT == sqlType || Types.SMALLINT == sqlType || Types.INTEGER == sqlType)
			retType = Types.INTEGER;
		else if (Types.CHAR == sqlType || Types.VARCHAR == sqlType)
			retType = Types.VARCHAR;
		else if (
			Types.DECIMAL == sqlType
				|| Types.DOUBLE == sqlType
				|| Types.FLOAT == sqlType
				|| Types.NUMERIC == sqlType
				|| Types.REAL == sqlType)
			retType = Types.NUMERIC;

		return retType;
	}
}
