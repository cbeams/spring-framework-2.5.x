/*
 * Copyright 2002-2006 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlTypeValue;

/**
 * Helper methods for handling parameter parsing, in particular for named parameters.
 *
 * @author Thomas Risberg
 * @since 2.0
 */
public class NamedParameterUtils {

	private static final int MAX_SELECT_LIST_ENTRIES = 100;

	private static final Log logger = LogFactory.getLog(NamedParameterUtils.class);


	/**
	 * Count the occurrences of the character <code>placeholder</code> in an SQL string
	 * <code>sql</code>. The character <code>placeholder</code> is not counted if it
	 * appears within a literal -- surrounded by single or double quotes.  This method will
	 * count traditional placeholders in the form of a question mark ('?') as well as
	 * named parameters indicated with a leading ':' or '&'.
	 * @param sql string to search in. Returns 0 if this is null
	 */
	public static int countParameterPlaceholders(String sql) {
		byte[] statement = (sql == null) ? new byte[0] : sql.getBytes();
		boolean withinQuotes = false;
		Map namedParameters = new HashMap();
		char currentQuote = '-';
		int parameterCount = 0;
		int i = 0;
		while (i < statement.length) {
			if (withinQuotes) {
				if (statement[i] == currentQuote) {
					withinQuotes = false;
					currentQuote = '-';
				}
			}
			else {
				if (statement[i] == '"' || statement[i] == '\'') {
					withinQuotes = true;
					currentQuote = (char)statement[i];
				}
				else {
					if (statement[i] == ':' || statement[i] == '&') {
						int j = i + 1;
						StringBuffer parameter = new StringBuffer();
						while (j < statement.length &&
								parameterNameIsContinued(statement, j)) {
							parameter.append(statement[j]);
							j++;
						}
						if (j - i > 1) {
							if (!namedParameters.containsKey(parameter.toString())) {
								parameterCount++;
								namedParameters.put(parameter.toString(), parameter);
								i = j - 1;
							}
						}
					}
					else {
						if (statement[i] == '?')
							parameterCount++;
					}
				}
			}
			i++;
		}
		return parameterCount;
	}

	/**
	 * Parse the SQL statement and locate any placeholders or named parameters.
	 * Named parameters are substituted for a JDBC placeholder.
	 * @param sql the SQL statement
	 */
	public static ParsedSql parseSqlStatement(String sql) {
		List parameters = new ArrayList();
        Map namedParameters = new HashMap();
        ParsedSql parsedSql = new ParsedSql(sql);

		byte[] statement = (sql == null) ? new byte[0] : sql.getBytes();
		StringBuffer newSql = new StringBuffer();
		boolean withinQuotes = false;
		char currentQuote = '-';
		int parameterCount = 0;
		int namedParameterCount = 0;
		int unNamedParameterCount = 0;

		int i = 0;
		while (i < statement.length) {
			if (withinQuotes) {
				if (statement[i] == currentQuote) {
					withinQuotes = false;
					currentQuote = '-';
				}
				newSql.append((char)statement[i]);
			}
			else {
				if (statement[i] == '"' || statement[i] == '\'') {
					withinQuotes = true;
					currentQuote = (char)statement[i];
					newSql.append((char)statement[i]);
				}
				else {
					if (statement[i] == ':' || statement[i] == '&') {
						int j = i + 1;
						while (j < statement.length &&
								parameterNameIsContinued(statement, j)) {
							j++;
						}
						if (j - i > 1) {
							String parameter = sql.substring(i+1, j);
                            if (!namedParameters.containsKey(parameter)) {
                                namedParameters.put(parameter, parameter);
                                namedParameterCount++;
                            }
							newSql.append("?");
                            parameters.add(parameter);
							parameterCount++;
                        }
						i = j - 1;
					}
					else {
						newSql.append((char)statement[i]);
						if (statement[i] == '?') {
							unNamedParameterCount++;
							parameterCount++;
						}
					}
				}
			}
			i++;
		}
		parsedSql.setUnNamedParameterCount(unNamedParameterCount);
		parsedSql.setNamedParameterCount(namedParameterCount);
		parsedSql.setParameterCount(parameterCount);
		parsedSql.setNewSql(newSql.toString());
		parsedSql.setParameters(parameters);
		return parsedSql;
	}

	/**
	 * Parse the SQL statement and locate any placeholders or named parameters.  Named
	 * parameters are substituted for a JDBC placeholder and any select list is expanded
	 * to the required number of placeholders.  The parameter values passed in
	 * are used to determine the number of placeholder to be used for a select list.
	 * Select lists should be limited to 100 or fewer elements.
	 * A larger number of elements is not guaramteed to be
	 * supported by the database and is strictly vendor dependent.
	 * @param sql the SQL statement.
	 * @param argMap a Map containing the parameter values
	 */
	public static String substituteNamedParameters(String sql, Map argMap) {
		byte[] statement = (sql == null) ? new byte[0] : sql.getBytes();
		StringBuffer newSql = new StringBuffer();
		boolean withinQuotes = false;
		char currentQuote = '-';

		int i = 0;
		while (i < statement.length) {
			if (withinQuotes) {
				if (statement[i] == currentQuote) {
					withinQuotes = false;
					currentQuote = '-';
				}
				newSql.append((char)statement[i]);
			}
			else {
				if (statement[i] == '"' || statement[i] == '\'') {
					withinQuotes = true;
					currentQuote = (char)statement[i];
					newSql.append((char)statement[i]);
				}
				else {
					if (statement[i] == ':' || statement[i] == '&') {
						int j = i + 1;
						while (j < statement.length && parameterNameIsContinued(statement, j)) {
							j++;
						}
						if (j - i > 1) {
							String parameter = sql.substring(i+1, j);
							if (argMap != null) {
								Object o = argMap.get(parameter);
								if (o instanceof List) {
									if (((List) o).size() > MAX_SELECT_LIST_ENTRIES) {
										logger.warn("The number of entries in a select list should not exceed " + MAX_SELECT_LIST_ENTRIES);
									}
									for (int k = 0; k < ((List)o).size(); k++) {
										if (k > 0)
											newSql.append(", ");
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
						i = j - 1;
					}
					else {
						newSql.append((char)statement[i]);
					}
				}
			}
			i++;
		}
		return newSql.toString();
	}

	/**
	 * Determine whether a parameter name ends at the current position - delimited by any
	 * whitespace character.
	 * @param statement the SQL statement.
	 * @param j the position within the statement.
	 */
	private static boolean parameterNameIsContinued(byte[] statement, int j) {
		return (statement[j] != ' ' && statement[j] != ',' && statement[j] != ')' &&
				statement[j] != '"' && statement[j] != '\'' && statement[j] != '|' &&
				statement[j] != ';' && statement[j] != '\n' && statement[j] != '\r');
	}

	/**
	 * Convert a Map of parameter values to a corresponding array.
	 * <p>This is necessary in order to reuse existing methods on JdbcTemplate.
	 * See below for additional info.
	 * @param sql the SQL statement.
	 * @param argMap the Map of parameters.
	 */
	public static Object[] convertArgMapToArray(String sql, Map argMap) {
		ParsedSql parsedSql = parseSqlStatement(sql);
		return convertArgMapToArray(argMap, parsedSql);
	}

	/**
	 * Convert a Map of parameter values to a corresponding array.  This is necessary in
	 * order to reuse existing methods on JdbcTemplate.
	 * Any named parameters are placed in the correct position in the Object array based on the
	 * parsed SQL statement info.
	 * @param parameters the Map of parameters.
	 * @param parsedSql the parsed SQL statement.
	 */
	public static Object[] convertArgMapToArray(Map parameters, ParsedSql parsedSql) {
		Object[] args = new Object[parsedSql.getParameterCount()];
		if (parsedSql.getNamedParameterCount() > 0 && parsedSql.getUnNamedParameterCount() > 0) {
		    throw new InvalidDataAccessApiUsageException("You can't mix named and traditional ? placeholders. You have " +
				parsedSql.getNamedParameterCount() + " named parameter(s) and " + parsedSql.getUnNamedParameterCount() +
				" traditonal placeholder(s) in [" + parsedSql.getSql() + "]");
		}
		if (parsedSql.getNamedParameterCount() != parameters.size()) {
			if (parsedSql.getNamedParameterCount() > parameters.size()) {
    			throw new InvalidDataAccessApiUsageException("Wrong number of parameters/values supplied. You have " +
					parsedSql.getNamedParameterCount() + " named parameter(s) and supplied " + parameters.size() +
					" parameter value(s)");
			}
			else {
				logger.warn("You have additional entries in the parameter map supplied. There are " +
					parsedSql.getNamedParameterCount() + " named parameter(s) and " + parameters.size() +
					" parameter value(s)");
			}
		}
		for (int i = 0; i < parsedSql.getParameters().size(); i++) {
			if (!parameters.containsKey(parsedSql.getParameters().get(i))) {
				throw new InvalidDataAccessApiUsageException("No entry supplied for the '" +
						parsedSql.getParameters().get(i) + "' parameter");
			}
			args[i] = parameters.get(parsedSql.getParameters().get(i));
		}
		return args;
	}

	/**
	 * Convert a Map of parameter types to a corresponding int array.  This is necessary in
	 * order to reuse existing methods on JdbcTemplate.
	 * Any named parameter types are placed in the correct position in the Object array based on the
	 * parsed SQL statement info.
	 * @param typeMap the Map of parameter types.
	 * @param parsedSql the parsed SQL statement.
	 */
	public static int[] convertTypeMapToArray(Map typeMap, ParsedSql parsedSql) {
		int[] types = new int[parsedSql.getParameterCount()];
		if (parsedSql.getNamedParameterCount() != typeMap.size()) {
			if (parsedSql.getNamedParameterCount() < typeMap.size()) {
				logger.warn("You have additional entries in the type map supplied.  There are " +
					parsedSql.getNamedParameterCount() + " named parameter(s) and " + typeMap.size() +
					" type value(s)");
			}
		}
		for (int i = 0; i < parsedSql.getParameters().size(); i++) {
			if (typeMap.containsKey(parsedSql.getParameters().get(i))) {
				types[i] = ((Integer) typeMap.get(parsedSql.getParameters().get(i))).intValue();
			}
			else {
				types[i] = SqlTypeValue.TYPE_UNKNOWN;
			}
		}
        return types;
	}

}
