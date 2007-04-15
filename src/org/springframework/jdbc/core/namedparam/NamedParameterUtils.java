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

package org.springframework.jdbc.core.namedparam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

/**
 * Helper methods for named parameter parsing.
 * Only intended for internal use within Spring's JDBC framework.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class NamedParameterUtils {

	/**
	 * Set of characters that qualify as parameter separators,
	 * indicating that a parameter name in a SQL String has ended.
	 */
	private static final char[] PARAMETER_SEPARATORS =
			new char[] {'"', '\'', ':', '&', ',', ';', '(', ')', '|', '=', '+', '-', '*', '%', '/', '\\', '<', '>', '^'};


	/**
	 * Parse the SQL statement and locate any placeholders or named parameters.
	 * Named parameters are substituted for a JDBC placeholder.
	 * @param sql the SQL statement
	 */
	public static String parseSqlStatementIntoString(String sql) {
		return parseSqlStatement(sql).getNewSql();
	}

	/**
	 * Parse the SQL statement and locate any placeholders or named parameters.
	 * Named parameters are substituted for a JDBC placeholder.
	 * @param sql the SQL statement
	 * @return the parsed statement, represented as ParsedSql instance
	 */
	static ParsedSql parseSqlStatement(String sql) {
		Assert.notNull(sql, "SQL must not be null");

		List parameters = new ArrayList();
		Map namedParameters = new HashMap();
		ParsedSql parsedSql = new ParsedSql(sql);

		char[] statement = sql.toCharArray();
		StringBuffer newSql = new StringBuffer();
		boolean withinQuotes = false;
		char currentQuote = '-';
		int namedParameterCount = 0;
		int unnamedParameterCount = 0;
		int totalParameterCount = 0;

		int i = 0;
		while (i < statement.length) {
			char c = statement[i];
			if (withinQuotes) {
				if (c == currentQuote) {
					withinQuotes = false;
					currentQuote = '-';
				}
				newSql.append(c);
			}
			else {
				if (c == '"' || c == '\'') {
					withinQuotes = true;
					currentQuote = c;
					newSql.append(c);
				}
				else {
					if (c == ':' || c == '&') {
						int j = i + 1;
						while (j < statement.length && !isParameterSeparator(statement[j])) {
							j++;
						}
						if (j - i > 1) {
							String parameter = sql.substring(i + 1, j);
							if (!namedParameters.containsKey(parameter)) {
								namedParameters.put(parameter, parameter);
								namedParameterCount++;
							}
							newSql.append("?");
							parameters.add(parameter);
							totalParameterCount++;
						} else {
							newSql.append(c);
						}
						i = j - 1;
					}
					else {
						newSql.append(c);
						if (c == '?') {
							unnamedParameterCount++;
							totalParameterCount++;
						}
					}
				}
			}
			i++;
		}
		parsedSql.setNewSql(newSql.toString());
		parsedSql.setParameterNames((String[]) parameters.toArray(new String[parameters.size()]));
		parsedSql.setNamedParameterCount(namedParameterCount);
		parsedSql.setUnnamedParameterCount(unnamedParameterCount);
		parsedSql.setTotalParameterCount(totalParameterCount);
		return parsedSql;
	}

	/**
	 * Parse the SQL statement and locate any placeholders or named parameters.
	 * Named parameters are substituted for a JDBC placeholder and any select list
	 * is expanded to the required number of placeholders.
	 * <p>The parameter values passed in are used to determine the number of
	 * placeholder to be used for a select list. Select lists should be limited
	 * to 100 or fewer elements. A larger number of elements is not guaramteed to
	 * be supported by the database and is strictly vendor-dependent.
	 * @param sql the SQL statement
	 * @param paramSource the source for named parameters
	 * @return the SQL statement with substituted parameters
	 */
	public static String substituteNamedParameters(String sql, SqlParameterSource paramSource) {
		Assert.notNull(sql, "SQL must not be null");

		char[] statement = sql.toCharArray();
		StringBuffer newSql = new StringBuffer();
		boolean withinQuotes = false;
		char currentQuote = '-';

		int i = 0;
		while (i < statement.length) {
			char c = statement[i];
			if (withinQuotes) {
				if (c == currentQuote) {
					withinQuotes = false;
					currentQuote = '-';
				}
				newSql.append(c);
			}
			else {
				if (c == '"' || c == '\'') {
					withinQuotes = true;
					currentQuote = c;
					newSql.append(c);
				}
				else {
					if (c == ':' || c == '&') {
						int j = i + 1;
						while (j < statement.length && !isParameterSeparator(statement[j])) {
							j++;
						}
						if (j - i > 1) {
							String paramName = sql.substring(i + 1, j);
							if (paramSource != null && paramSource.hasValue(paramName)) {
								Object value = paramSource.getValue(paramName);
								if (value instanceof Collection) {
									Collection entries = (Collection) value;
									for (int k = 0; k < entries.size(); k++) {
										if (k > 0) {
											newSql.append(", ");
										}
										newSql.append("?");
									}
								}
								else {
									newSql.append("?");
								}
							}
							else {
								newSql.append("?");
							}
						}
						else {
							newSql.append(c);
						}
						i = j - 1;
					}
					else {
						newSql.append(c);
					}
				}
			}
			i++;
		}
		return newSql.toString();
	}


	/**
	 * Convert a Map of named parameter values to a corresponding array.
	 * @param sql the SQL statement
	 * @param paramMap the Map of parameters
	 * @return the array of values
	 */
	public static Object[] buildValueArray(String sql, Map paramMap) {
		return buildValueArray(sql, paramMap, null);
	}

	/**
	 * Convert a Map of named parameter values to a corresponding array.
	 * @param sql the SQL statement
	 * @param paramMap the Map of parameters
	 * @param declaredParams the List of declared SqlParameter objects
	 * (may be <code>null</code>). If specified, the parameter metadata will
	 * be built into the value array in the form of SqlParameterValue objects.
	 * @return the array of values
	 */
	public static Object[] buildValueArray(String sql, Map paramMap, List declaredParams) {
		ParsedSql parsedSql = parseSqlStatement(sql);
		return buildValueArray(parsedSql, new MapSqlParameterSource(paramMap), declaredParams);
	}

	/**
	 * Convert a Map of named parameter values to a corresponding array.
	 * @param parsedSql the parsed SQL statement
	 * @param paramSource the source for named parameters
	 * @return the array of values
	 */
	static Object[] buildValueArray(ParsedSql parsedSql, SqlParameterSource paramSource) {
		return buildValueArray(parsedSql, paramSource, null);
	}

	/**
	 * Convert a Map of named parameter values to a corresponding array.
	 * @param parsedSql the parsed SQL statement
	 * @param paramSource the source for named parameters
	 * @param declaredParams the List of declared SqlParameter objects
	 * (may be <code>null</code>). If specified, the parameter metadata will
	 * be built into the value array in the form of SqlParameterValue objects.
	 * @return the array of values
	 */
	static Object[] buildValueArray(ParsedSql parsedSql, SqlParameterSource paramSource, List declaredParams) {
		Object[] paramArray = new Object[parsedSql.getTotalParameterCount()];
		if (parsedSql.getNamedParameterCount() > 0 && parsedSql.getUnnamedParameterCount() > 0) {
			throw new InvalidDataAccessApiUsageException(
					"You can't mix named and traditional ? placeholders. You have " +
					parsedSql.getNamedParameterCount() + " named parameter(s) and " +
					parsedSql.getUnnamedParameterCount() + " traditonal placeholder(s) in [" +
					parsedSql.getSql() + "]");
		}
		String[] paramNames = parsedSql.getParameterNames();
		for (int i = 0; i < paramNames.length; i++) {
			String paramName = paramNames[i];
			try {
				Object value = paramSource.getValue(paramName);
				SqlParameter param = findParameter(declaredParams, paramName, i);
				paramArray[i] = (param != null ? new SqlParameterValue(param, value) : value);
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(
						"No value supplied for the SQL parameter '" + paramName + "': " + ex.getMessage());
			}
		}
		return paramArray;
	}

	/**
	 * Find a matching parameter in the given list of declared parameters.
	 * @param declaredParams the declared SqlParameter objects
	 * @param paramName the name of the desired parameter
	 * @param paramIndex the index of the desired parameter
	 * @return the declared SqlParameter, or <code>null</code> if none found
	 */
	private static SqlParameter findParameter(List declaredParams, String paramName, int paramIndex) {
		if (declaredParams != null) {
			// First pass: Look for named parameter match.
			for (Iterator it = declaredParams.iterator(); it.hasNext();) {
				SqlParameter declaredParam = (SqlParameter) it.next();
				if (paramName.equals(declaredParam.getName())) {
					return declaredParam;
				}
			}
			// Second pass: Look for parameter index match.
			if (paramIndex < declaredParams.size()) {
				SqlParameter declaredParam = (SqlParameter) declaredParams.get(paramIndex);
				// Only accept unnamed parameters for index matches.
				if (declaredParam.getName() == null) {
					return declaredParam;
				}
			}
		}
		return null;
	}


	/**
	 * Convert a Map of parameter types to a corresponding int array.
	 * This is necessary in order to reuse existing methods on JdbcTemplate.
	 * Any named parameter types are placed in the correct position in the
	 * Object array based on the parsed SQL statement info.
	 * @param parsedSql the parsed SQL statement
	 * @param paramSource the source for named parameters
	 */
	static int[] buildSqlTypeArray(ParsedSql parsedSql, SqlParameterSource paramSource) {
		int[] sqlTypes = new int[parsedSql.getTotalParameterCount()];
		String[] paramNames = parsedSql.getParameterNames();
		for (int i = 0; i < paramNames.length; i++) {
			sqlTypes[i] = paramSource.getSqlType(paramNames[i]);
		}
		return sqlTypes;
	}

	/**
	 * Determine whether a parameter name ends at the current position,
	 * that is, whether the given character qualifies as a separator.
	 */
	private static boolean isParameterSeparator(char c) {
		if (Character.isWhitespace(c)) {
			return true;
		}
		for (int i = 0; i < PARAMETER_SEPARATORS.length; i++) {
			if (c == PARAMETER_SEPARATORS[i]) {
				return true;
			}
		}
		return false;
	}

}
