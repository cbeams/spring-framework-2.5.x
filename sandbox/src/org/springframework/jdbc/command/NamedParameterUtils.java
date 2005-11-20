package org.springframework.jdbc.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.support.ParsedSql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: trisberg
 * Date: Nov 20, 2005
 * Time: 9:25:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class NamedParameterUtils {
    private static final Log logger = LogFactory.getLog(NamedParameterUtils.class);
    private static final int MAX_SELECT_LIST_ENTRIES = 100;

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
                        while (j < statement.length &&
                                parameterNameIsContinued(statement, j)) {
                            j++;
                        }
                        if (j - i > 1) {
                            parameterCount++;
                        }
                        i = j - 1;
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
     * Parse the SQL statement and locate any placeholders or named parameters.  Named
     * parameters are substituted for a JDBC placeholder.
     * @param sql the SQL statement.
     */
    public static ParsedSql parseSqlStatement(String sql) {

        ArrayList namedParameters = new ArrayList();
        ParsedSql parsedSql = new ParsedSql(sql);

        byte[] statement = (sql == null) ? new byte[0] : sql.getBytes();
        StringBuffer newSql = new StringBuffer();
        boolean withinQuotes = false;
        char currentQuote = '-';
        int parameterCount = 0;
        int namedParameterCount = 0;

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
                            namedParameters.add(parameter);
                            newSql.append("?");
                            parameterCount++;
                            namedParameterCount++;
                        }
                        i = j - 1;
                    }
                    else {
                        newSql.append((char)statement[i]);
                        if (statement[i] == '?')
                            parameterCount++;
                    }
                }
            }
            i++;
        }
        parsedSql.setNamedParameterCount(namedParameterCount);
        parsedSql.setParameterCount(parameterCount);
        parsedSql.setNewSql(newSql.toString());
        parsedSql.setNamedParameters(namedParameters);
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
                        while (j < statement.length &&
                                parameterNameIsContinued(statement, j)) {
                            j++;
                        }
                        if (j - i > 1) {
                            String parameter = sql.substring(i+1, j);
                            if (argMap != null) {
                                Object o = argMap.get(parameter);
                                if (o instanceof List) {
                                    if (((List)o).size() > MAX_SELECT_LIST_ENTRIES) {
                                        logger.warn("The number of entries in a select list should not exceed " + MAX_SELECT_LIST_ENTRIES + ".");
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
     * Convert a Map of parameter values to a corresponding array.  This is necessary in
     * order to reuse existing methods on JdbcTemplate.
     * See below for additional info.
     * @param sql the SQL statement.
     * @param argMap the Map of parameters.
     */
    public static Object[] convertArgMapToArray(String sql, Map argMap) {
        ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
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
        Object[] args = new Object[parsedSql.getNamedParameterCount()];
        if (parsedSql.getNamedParameterCount() != parsedSql.getParameterCount()) {
            throw new InvalidDataAccessApiUsageException("You must supply named parameter placeholders for all " +
                    "parameters when using a Map for the parameter values.");
        }
        if (parsedSql.getNamedParameterCount() != parameters.size()) {
            if (parsedSql.getNamedParameterCount() > parameters.size()) {
            throw new InvalidDataAccessApiUsageException("Wrong number of parameters/values supplied.  You have " +
                    parsedSql.getNamedParameterCount() + " named parameter(s) and supplied " + parameters.size() +
                    " parameter value(s).");
            }
            else {
                logger.warn("You have additional entries in the parameter map supplied.  There are " +
                    parsedSql.getNamedParameterCount() + " named parameter(s) and " + parameters.size() +
                    " parameter value(s).");
                logger.warn("!!!! " + parameters);
            }
        }
        for (int i = 0; i < parsedSql.getNamedParameters().size(); i++) {
            if (!parameters.containsKey(parsedSql.getNamedParameters().get(i))) {
                throw new InvalidDataAccessApiUsageException("No entry supplied for the '" +
                        parsedSql.getNamedParameters().get(i) + "' parameter.");

            }
            args[i] = parameters.get(parsedSql.getNamedParameters().get(i));
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
        int[] types = new int[parsedSql.getNamedParameterCount()];
        if (parsedSql.getNamedParameterCount() != typeMap.size()) {
            if (parsedSql.getNamedParameterCount() < typeMap.size()) {
                logger.warn("You have additional entries in the type map supplied.  There are " +
                    parsedSql.getNamedParameterCount() + " named parameter(s) and " + typeMap.size() +
                    " type value(s).");
            }
        }
        for (int i = 0; i < parsedSql.getNamedParameters().size(); i++) {
            if (typeMap.containsKey(parsedSql.getNamedParameters().get(i))) {
                types[i] = ((Integer)typeMap.get(parsedSql.getNamedParameters().get(i))).intValue();
            }
            else {
                types[i] = SqlTypeValue.TYPE_UNKNOWN;
            }
        }
        return types;
    }
}
