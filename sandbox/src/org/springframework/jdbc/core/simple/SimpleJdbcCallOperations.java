package org.springframework.jdbc.core.simple;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Map;

/**
 * @author trisberg
 */
public interface SimpleJdbcCallOperations {

	SimpleJdbcCall withProcedureName(String procedureName);

	SimpleJdbcCall withSchemaName(String schemaName);

	SimpleJdbcCall withCatalogName(String catalogName);

	SimpleJdbcCall withFunctionName(String functionName);

	SimpleJdbcCall declareParameter(SqlParameter sqlParameter);

	SimpleJdbcCall declareParameters(SqlParameter... sqlParameters);

	SimpleJdbcCall declareInParameterNames(String... inParameterNames);

	SimpleJdbcCall declareOutParameterNames(String... inParameterNames);

	SimpleJdbcCall declareReturnTypeHandler(String parameterName, String sqlTypeName, SqlReturnType sqlReturnType);

	SimpleJdbcCall declareReturnRowMapper(String parameterName, RowMapper rowMapper);

	SimpleJdbcCall declareReturnResultSetExtractor(String parameterName, ResultSetExtractor resultSetExtractor);

	<T> T executeFunction(Class<T> returnType, Map args);

	<T> T executeFunction(Class<T> returnType, SqlParameterSource args);

	<T> T executeObject(Class<T> returnType, Map args);

	<T> T executeObject(Class<T> returnType, SqlParameterSource args);

	Map<String, Object> execute(Map args);

	Map<String, Object> execute(SqlParameterSource args);
}
