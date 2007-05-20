package org.springframework.jdbc.core.simple;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.*;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author trisberg
 */
public class SimpleJdbcCall extends AbstractJdbcCall implements SimpleJdbcCallOperations {

	public SimpleJdbcCall(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public SimpleJdbcCall(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public synchronized SimpleJdbcCall withProcedureName(String procedureName) {
		this.procName = procedureName;
		setFunction(false);
		return this;
	}

	public synchronized SimpleJdbcCall withSchemaName(String schemaName) {
		this.schemaName = schemaName;
		setFunction(false);
		return this;
	}

	public synchronized SimpleJdbcCall withCatalogName(String catalogName) {
		this.catalogName = catalogName;
		setFunction(false);
		return this;
	}

	public synchronized SimpleJdbcCall withFunctionName(String functionName) {
		this.procName = functionName;
		setFunction(true);
		return this;
	}

	public SimpleJdbcCall declareParameter(SqlParameter sqlParameter) {
		if (sqlParameter != null)
			declaredParameters.add(sqlParameter);
		setAutoDetectParameters(false);
		return this;
	}

	public SimpleJdbcCall declareParameters(SqlParameter... sqlParameters) {
		for (SqlParameter sqlParameter : sqlParameters) {
			if (sqlParameter != null)
				declaredParameters.add(sqlParameter);
		}
		setAutoDetectParameters(false);
		return this;
	}

	public SimpleJdbcCall declareInParameterNames(String... inParameterNames) {
		return this;
	}

	public SimpleJdbcCall declareOutParameterNames(String... inParameterNames) {
		return this;
	}

	public SimpleJdbcCall declareReturnTypeHandler(String parameterName, String sqlTypeName, SqlReturnType sqlReturnType) {
		return this;
	}

	public SimpleJdbcCall declareReturnRowMapper(String parameterName, RowMapper rowMapper) {
		return this;
	}

	public SimpleJdbcCall declareReturnResultSetExtractor(String parameterName, ResultSetExtractor resultSetExtractor) {
		return this;
	}

	public <T> T executeFunction(Class<T> returnType, Map args) {
		return (T) execute(args).get("return");

	}

	public <T> T executeFunction(Class<T> returnType, SqlParameterSource args) {
		return (T) execute(args).get("return");

	}

	public <T> T executeObject(Class<T> returnType, Map args) {
		return (T) execute(args).get(outParameterNames.get(0));

	}

	public <T> T executeObject(Class<T> returnType, SqlParameterSource args) {
		return (T) execute(args).get(outParameterNames.get(0));

	}

	public Map<String, Object> execute(Map args) {
		return null;
	}

	public Map<String, Object> execute(SqlParameterSource args) {
		checkCompiled();
		Map values = ((MapSqlParameterSource)args).getValues();
		System.out.println("-->" + values);
		CallableStatementCreator csc = this.callableStatementFactory.newCallableStatementCreator(values);
		Map result = jdbcTemplate.call(csc, declaredParameters);
		return (Map<String, Object>)result;
	}

}
