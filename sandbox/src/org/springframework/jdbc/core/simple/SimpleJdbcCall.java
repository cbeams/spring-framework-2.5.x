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


	public SimpleJdbcCall withProcedureName(String procedureName) {
		this.procName = procedureName;
		setFunction(false);
		return this;
	}

	public SimpleJdbcCall withSchemaName(String schemaName) {
		this.schemaName = schemaName;
		return this;
	}

	public SimpleJdbcCall withCatalogName(String catalogName) {
		this.catalogName = catalogName;
		return this;
	}

	public SimpleJdbcCall withFunctionName(String functionName) {
		this.procName = functionName;
		setFunction(true);
		return this;
	}

	public SimpleJdbcCall declareParameter(SqlParameter sqlParameter) {
		if (sqlParameter != null)
			addDeclaredParameter(sqlParameter);
		return this;
	}

	public SimpleJdbcCall declareParameters(SqlParameter... sqlParameters) {
		for (SqlParameter sqlParameter : sqlParameters) {
			if (sqlParameter != null)
				addDeclaredParameter(sqlParameter);
		}
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

	public SimpleJdbcCall withoutProcedureColumnMetaDataAccess() {
		setAccessProcedureColumnMetaData(false);
		return this;
	}

	public <T> T executeFunction(Class<T> returnType, Map args) {
		return (T) execute(args).get(returnNameToUse);

	}

	public <T> T executeFunction(Class<T> returnType, SqlParameterSource args) {
		return (T) execute(args).get(returnNameToUse);

	}

	public <T> T executeObject(Class<T> returnType, Map args) {
		return (T) execute(args).get(outParameterNames.get(0));

	}

	public <T> T executeObject(Class<T> returnType, SqlParameterSource args) {
		return (T) execute(args).get(outParameterNames.get(0));

	}

	public Map<String, Object> execute(Map args) {
		checkCompiled();
		Map values = matchInParameterValuesWithCallParameters(args);
		CallableStatementCreator csc = this.callableStatementFactory.newCallableStatementCreator(values);
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for call " + getCallString() + " with: " + values);
			int i = 1;
			for (SqlParameter p : getCallParameters()) {
				logger.debug(i++ + ": " +  p.getName() + " SQL Type "+ p.getSqlType() + " Type Name " + p.getTypeName() + " " + p.getClass().getName());
			}
		}
		Map result = jdbcTemplate.call(csc, getCallParameters());
		return (Map<String, Object>)result;
	}

	public Map<String, Object> execute(SqlParameterSource args) {
		Map values = ((MapSqlParameterSource)args).getValues();
		return execute(values);
	}

}
