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

package org.springframework.jdbc.core.namedparam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.InvalidDataAccessApiUsageException;

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
	 * Count the occurrences of the character <code>placeholder</code> in an SQL string
	 * <code>sql</code>. The character <code>placeholder</code> is not counted if it
	 * appears within a literal -- surrounded by single or double quotes.  This method will
	 * count traditional placeholders in the form of a question mark ('?') as well as
	 * named parameters indicated with a leading ':' or '&'.
	 * @param sql string to search in. Returns 0 if this is <code>null</code> 
	 */
	public static int countParameterPlaceholders(String sql) {
		byte[] statement = getBytesNullSafe(sql);
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
					currentQuote = (char) statement[i];
				}
				else {
					if (statement[i] == ':' || statement[i] == '&') {
						int j = i + 1;
						StringBuffer parameter = new StringBuffer();
						while (j < statement.length && parameterNameContinues(statement, j)) {
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
						if (statement[i] == '?') {
							parameterCount++;
						}
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
	public static String parseSqlStatementIntoString(String sql) {
		return parseSqlStatement(sql).getNewSql();
	}

	/**
	 * Parse the SQL statement and locate any placeholders or named parameters.
	 * Named parameters are substituted for a JDBC placeholder.
	 * @param sql the SQL statement
	 */
	static ParsedSql parseSqlStatement(String sql) {
		List parameters = new ArrayList();
		Map namedParameters = new HashMap();
		ParsedSql parsedSql = new ParsedSql(sql);

		byte[] statement = getBytesNullSafe(sql);
		StringBuffer newSql = new StringBuffer();
		boolean withinQuotes = false;
		char currentQuote = '-';
		int namedParameterCount = 0;
		int unnamedParameterCount = 0;
		int totalParameterCount = 0;

		int i = 0;
		while (i < statement.length) {
			if (withinQuotes) {
				if (statement[i] == currentQuote) {
					withinQuotes = false;
					currentQuote = '-';
				}
				newSql.append((char) statement[i]);
			}
			else {
				if (statement[i] == '"' || statement[i] == '\'') {
					withinQuotes = true;
					currentQuote = (char) statement[i];
					newSql.append((char) statement[i]);
				}
				else {
					if (statement[i] == ':' || statement[i] == '&') {
						int j = i + 1;
						while (j < statement.length && parameterNameContinues(statement, j)) {
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
						}
						i = j - 1;
					}
					else {
						newSql.append((char) statement[i]);
						if (statement[i] == '?') {
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
     */
    public static String substituteNamedParameters(String sql, SqlParameterSource paramSource) {
        byte[] statement = getBytesNullSafe(sql);
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
                newSql.append((char) statement[i]);
            }
            else {
                if (statement[i] == '"' || statement[i] == '\'') {
                    withinQuotes = true;
                    currentQuote = (char) statement[i];
                    newSql.append((char) statement[i]);
                }
                else {
                    if (statement[i] == ':' || statement[i] == '&') {
                        int j = i + 1;
                        while (j < statement.length && parameterNameContinues(statement, j)) {
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
                        i = j - 1;
                    }
                    else {
                        newSql.append((char) statement[i]);
                    }
                }
            }
            i++;
        }
        return newSql.toString();
    }

	/**
	 * Convert a Map of named parameter values to a corresponding array.
	 * <p>This is necessary in order to reuse existing methods on JdbcTemplate.
	 * See below for additional info.
	 * @param sql the SQL statement
	 * @param paramMap the Map of parameters
	 */
	public static Object[] buildValueArray(String sql, Map paramMap) {
		ParsedSql parsedSql = parseSqlStatement(sql);
		return buildValueArray(parsedSql, new SimpleSqlParameterSource(paramMap));
	}

	/**
	 * Convert a Map of named parameter values to a corresponding array.
	 * This is necessary in order to reuse existing methods on JdbcTemplate.
	 * Any named parameters are placed in the correct position in the Object
	 * array based on the parsed SQL statement info.
	 * @param parsedSql the parsed SQL statement
	 * @param paramSource the source for named parameters
	 */
	static Object[] buildValueArray(ParsedSql parsedSql, SqlParameterSource paramSource) {
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
				paramArray[i] = paramSource.getValue(paramName);
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(
						"No value supplied for the SQL parameter '" + paramName + "': " + ex.getMessage());
			}
		}
		return paramArray;
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
    

    private static byte[] getBytesNullSafe(String sql) {
        return (sql == null) ? new byte[0] : sql.getBytes();
    }

	/**
	 * Determine whether a parameter name continues at the current position,
	 * that is, does not end delimited by any whitespace character yet.
	 * @param statement the SQL statement
	 * @param j the position within the statement
	 */
	private static boolean parameterNameContinues(byte[] statement, int j) {
		return (statement[j] != ' ' && statement[j] != ',' && statement[j] != ')' &&
				statement[j] != '"' && statement[j] != '\'' && statement[j] != '|' &&
				statement[j] != ';' && statement[j] != '\n' && statement[j] != '\r');
	}

}
