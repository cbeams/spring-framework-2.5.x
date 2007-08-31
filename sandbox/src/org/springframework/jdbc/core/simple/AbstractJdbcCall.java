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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.*;

/**
 * Abstract class to provide base functionality for easy stored procedure calls based on configuration options and
 * database metadata.
 * This class provides the base SPI for {@link SimpleJdbcCall}.
 *
 * @author Thomas Risberg
 * @since 2.1
 */
public abstract class AbstractJdbcCall {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Lower-level class used to execute SQL */
	private JdbcTemplate jdbcTemplate = new JdbcTemplate();

	/** List of SqlParameter objects */
	private final List<SqlParameter> declaredParameters = new ArrayList<SqlParameter>();

	/** List of RefCursor/ResultSet RowMapper objects */
	private final Map<String, ParameterizedRowMapper> declaredRowMappers = new LinkedHashMap<String, ParameterizedRowMapper>();

	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled = false;

	/** the generated string used for call statement */
	private String callString;

	/** context used to retrieve and manage database metadata */
	private CallMetaDataContext callMetaDataContext = new CallMetaDataContext();

	/**
	 * Object enabling us to create CallableStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private CallableStatementCreatorFactory callableStatementFactory;

	/**
	 * Constructor to be used when initializing using a {@link DataSource}
	 *
	 * @param dataSource the DataSource to be used
	 */
	protected AbstractJdbcCall(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Constructor to be used when initializing using a {@link JdbcTemplate}
	 *
	 * @param jdbcTemplate the JdbcTemplate to use
	 */
	protected AbstractJdbcCall(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * Get the name of the stored procedure
	 */
	public String getProcedureName() {
		return callMetaDataContext.getProcedureName();
	}


	/**
	 * Set the name of the stored procedure
	 * @param procedureName name of stored procedure
	 */
	public void setProcedureName(String procedureName) {
		callMetaDataContext.setProcedureName(procedureName);
	}

	/**
	 * Set the names of in parameters to be used
	 * @param inParameterNames
	 */
	public void setInParameterNames(HashSet inParameterNames) {
		callMetaDataContext.setLimitedInParameterNames(inParameterNames);
	}

	/**
	 * Get the catalog name used
	 */
	public String getCatalogName() {
		return callMetaDataContext.getCatalogName();
	}

	/**
	 * Set the catalog name to use
	 */
	public void setCatalogName(String catalogName) {
		callMetaDataContext.setCatalogName(catalogName);
	}

	/**
	 * Get the schema name used 
	 */
	public String getSchemaName() {
		return callMetaDataContext.getSchemaName();
	}

	/**
	 * Set the schema name to use
	 */
	public void setSchemaName(String schemaName) {
		callMetaDataContext.setSchemaName(schemaName);
	}

	/**
	 * Is this call a function call
	 */
	public boolean isFunction() {
		return callMetaDataContext.isFunction();
	}

	/**
	 * Specify whether this call is a function call
	 */
	public void setFunction(boolean b) {
		this.callMetaDataContext.setFunction(b);
	}

	/**
	 * Does the call require a return value
	 */
	public boolean isReturnValueRequired() {
		return callMetaDataContext.isReturnValueRequired();
	}

	/**
	 * Specify whether the call requires a rerurn value
	 */
	public void setReturnValueRequired(boolean b) {
		this.callMetaDataContext.setReturnValueRequired(b);
	}

	/**
	 * get the configured {@link JdbcTemplate}
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * Get the {@link CallableStatementCreatorFactory} being used
	 */
	protected CallableStatementCreatorFactory getCallableStatementFactory() {
		return callableStatementFactory;
	}

	/**
	 * Add a declared parameter to teh list of parameters for the call
	 * @param parameter the {@link SqlParameter} to add
	 */
	public void addDeclaredParameter(SqlParameter parameter) {
		this.declaredParameters.add(parameter);
		if (logger.isDebugEnabled()) {
			logger.debug("Added declared parameter for [" + getProcedureName() + "]: " + parameter.getName());
		}
	}

	/**
	 * Add a {@link RowMapper} for the specified parameter or column
	 * @param parameterName name of parameter or column
	 * @param rowMapper the RowMapper implementation to use
	 */
	public void addDeclaredRowMapper(String parameterName, ParameterizedRowMapper rowMapper) {
		this.declaredRowMappers.put(parameterName, rowMapper);
		if (logger.isDebugEnabled()) {
			logger.debug("Added row mapper for [" + getProcedureName() + "]: " + parameterName);
		}
	}

	/**
	 * Get the call string that should be used based on parameters and meta data
	 */
	public String getCallString() {
		return callString;
	}

	/**
	 * Specify whether the parameter metadata for the call should be used.  The default is true.
	 */
	public void setAccessCallParameterMetaData(boolean accessCallParameterMetaData) {
		this.callMetaDataContext.setAccessCallParameterMetaData(accessCallParameterMetaData);
	}


	//-------------------------------------------------------------------------
	// Methods handling compilation issues
	//-------------------------------------------------------------------------

	/**
	 * Compile this JdbcCall using provided parameters and meta data plus other settings.  This
	 * finalizes the configuration for this object and subsequent attempts to compile are ignored.
	 * This will be implicitly called the first time an un-compiled call is executed.
	 * @throws org.springframework.dao.InvalidDataAccessApiUsageException if the object hasn't
	 * been correctly initialized, for example if no DataSource has been provided
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (getProcedureName() == null) {
				throw new InvalidDataAccessApiUsageException("Procedure or Function name is required");
			}

			try {
				this.jdbcTemplate.afterPropertiesSet();
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(ex.getMessage());
			}

			compileInternal();
			this.compiled = true;

			if (logger.isDebugEnabled()) {
				logger.debug("SqlCall for " + (isFunction() ? "function" : "procedure") + " [" + getProcedureName() + "] compiled");
			}
		}
	}

	/**
	 * Method to perform the actual compilation.  Subclasses can override this template method to perform
	 * their own compilation.  Invoked after this base class's compilation is complete.
	 */
	protected void compileInternal() {

		callMetaDataContext.initializeMetaData(getJdbcTemplate().getDataSource());

		// iterate over the declared RowMappers and register the corresponding SqlParameter
		for (Map.Entry<String, ParameterizedRowMapper> entry : declaredRowMappers.entrySet()) {
			SqlParameter resultSetParameter =
					callMetaDataContext.createReturnResultSetParameter(entry.getKey(), entry.getValue());
			declaredParameters.add(resultSetParameter);
		}

		callMetaDataContext.processParameters(declaredParameters);

		callString = callMetaDataContext.createCallString();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Compiled stored procedure. Call string is [" + getCallString() + "]");
		}

		this.callableStatementFactory =
				new CallableStatementCreatorFactory(getCallString(), callMetaDataContext.getCallParameters());

		this.callableStatementFactory.setNativeJdbcExtractor(getJdbcTemplate().getNativeJdbcExtractor());

		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

	/**
	 * Is this operation "compiled"?
	 * @return whether this operation is compiled, and ready to use.
	 */
	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * Check whether this operation has been compiled already;
	 * lazily compile it if not already compiled.
	 * <p>Automatically called by <code>doExecute</code>.
	 */
	protected void checkCompiled() {
		if (!isCompiled()) {
			logger.debug("JdbcCall call not compiled before execution - invoking compile");
			compile();
		}
	}


	//-------------------------------------------------------------------------
	// Methods handling execution
	//-------------------------------------------------------------------------

	/**
	 * Method that provides execution of the call using the passed in {@link SqlParameterSource}
	 *
	 * @param parameterSource parameter names and values to be used in call
	 * @return Map of out parameters
	 */
	protected Map<String, Object> doExecute(SqlParameterSource parameterSource) {
		checkCompiled();
		Map params = null;
		if (parameterSource instanceof MapSqlParameterSource) {
			Map<String, Object> sourceValues = ((MapSqlParameterSource)parameterSource).getValues();
			params = matchInParameterValuesWithCallParameters(sourceValues);
		}
		else {
			params = matchInParameterValuesWithCallParameters(parameterSource);
		}
		return executeCallInternal(params);
	}

	/**
	 * Method that provides execution of the call using the passed in Map of parameters
	 *
	 * @param args Map of parameter name and values
	 * @return Map of out parameters
	 */
	protected Map<String, Object> doExecute(Map<String, Object> args) {
		checkCompiled();
		Map params = matchInParameterValuesWithCallParameters(args);
		return executeCallInternal(params);
	}

	/**
	 * Mathod to perform the actual call processing
	 */
	private Map<String, Object> executeCallInternal(Map params) {
		CallableStatementCreator csc = getCallableStatementFactory().newCallableStatementCreator(params);
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for call " + getCallString() + " with: " + params);
			int i = 1;
			for (SqlParameter p : getCallParameters()) {
				logger.debug(i++ + ": " +  p.getName() + " SQL Type "+ p.getSqlType() + " Type Name " + p.getTypeName() + " " + p.getClass().getName());
			}
		}
		Map<String, Object> result = getJdbcTemplate().call(csc, getCallParameters());
		return result;
	}

	/**
	 * Get the name of a single out parameter or return value. Used for functions or procedures with one out parameter
	 */
	protected String getScalarOutParameterName() {
		return callMetaDataContext.getScalarOutParameterName();
	}

	/**
	 * Match the provided in parameter values with regitered parameters and parameters defined via metedata
	 * processing.
	 *
	 * @param parameterSource the parameter vakues provided as a {@link SqlParameterSource}
	 * @return Map with parameter names and values
	 */
	protected Map<String, Object> matchInParameterValuesWithCallParameters(SqlParameterSource parameterSource) {
		return callMetaDataContext.matchInParameterValuesWithCallParameters(parameterSource);
	}

	/**
	 * Match the provided in parameter values with regitered parameters and parameters defined via metedata
	 * processing.
	 *
	 * @param args the parameter values provided in a Map
	 * @return Map with parameter names and values
	 */
	protected Map<String, Object> matchInParameterValuesWithCallParameters(Map<String, Object> args) {
		return callMetaDataContext.matchInParameterValuesWithCallParameters(args);
	}

	/**
	 * Get a List of all the call parameters to be used for call. This includes any parameters added
	 * based on meta data processing.
	 */
	protected List<SqlParameter> getCallParameters() {
		return callMetaDataContext.getCallParameters();
	}
}
