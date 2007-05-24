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
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;

/**
 * @author trisberg
 */
public abstract class AbstractJdbcCall {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Lower-level class used to execute SQL */
	protected JdbcTemplate jdbcTemplate = new JdbcTemplate();

	/** List of SqlParameter objects */
	private final List<SqlParameter> callParameters = new ArrayList<SqlParameter>();

	/** List of SqlReturn* objects */
	private final List<SqlParameter> declaredReturnParameters = new ArrayList<SqlParameter>();

	/** List of SqlParameter objects */
	private final Map<String, SqlParameter> declaredParameters = new HashMap<String, SqlParameter>();

	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled = false;

	protected String procName;
	protected String procedureNameToUse;
	protected String catalogName;
	protected String catalogNameToUse;
	protected String schemaNameToUse;
	protected String schemaName;
	protected String userName;
	protected String returnNameToUse;
	protected boolean returnDeclared = false;
	protected boolean defaultSchemaToUserName = false;
	protected boolean useCatalogNameAsPackageName = false;
	private boolean function;
	protected boolean supportsCatalogsInProcedureCalls = true;
	protected boolean supportsSchemasInProcedureCalls = true;
	private boolean accessMetaData = true;
	private boolean accessProcedureColumnMetaData = true;
	protected boolean storesUpperCaseIdentifiers = false;
	protected boolean storesLowerCaseIdentifiers = false;
	protected boolean caseSensitiveParameters = false;
	protected List<String> outParameterNames = new ArrayList<String>();
	protected String callString;
	protected String removableProcedureColumnNamePrefix = "@";

	/**
	 * Object enabling us to create CallableStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	protected CallableStatementCreatorFactory callableStatementFactory;

	public boolean isFunction() {
		return function;
	}

	protected void setFunction(boolean function) {
		this.function = function;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void addDeclaredParameter(SqlParameter parameter) {
		if (parameter.isResultsParameter()) {
			declaredReturnParameters.add(parameter);
		}
		else {
			String parameterNameToMatch = caseSensitiveParameters ? parameter.getName() : parameter.getName().toLowerCase();
			if (parameterNameToMatch.startsWith(removableProcedureColumnNamePrefix) && parameterNameToMatch.length() > 1) {
				parameterNameToMatch = parameterNameToMatch.substring(1);
			}
			declaredParameters.put(parameterNameToMatch, parameter);
			if (parameter instanceof SqlOutParameter) {
				outParameterNames.add(parameterNameToMatch);
				returnDeclared = true;
			}
		}
		if (logger.isDebugEnabled()) {
			if (parameter.isResultsParameter()) {
				logger.debug("Added declared return parameter for [" + procName + "]: " + parameter.getName());
			}
			else if (parameter instanceof SqlOutParameter) {
				logger.debug("Added declared out parameter for [" + procName + "]: " + parameter.getName());
			}
			else {
				logger.debug("Added declared parameter for [" + procName + "]: " + parameter.getName());
			}
		}
	}
	
	public List<SqlParameter> getCallParameters() {
		return callParameters;
	}

	public String getCallString() {
		return callString;
	}

	public AbstractJdbcCall setAccessProcedureColumnMetaData(boolean accessProcedureColumnMetaData) {
		this.accessProcedureColumnMetaData = accessProcedureColumnMetaData;
		return this;
	}

	/**
	 * Compile this query.
	 * Ignores subsequent attempts to compile.
	 * @throws org.springframework.dao.InvalidDataAccessApiUsageException if the object hasn't
	 * been correctly initialized, for example if no DataSource has been provided
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (procName == null) {
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
				logger.debug("SqlCall for " + (isFunction() ? "function" : "procedure") + " [" + procName + "] compiled");
			}
		}
	}

	/**
	 * Overridden method to configure the CallableStatementCreatorFactory
	 * based on our declared parameters.
	 * @see org.springframework.jdbc.object.RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {
		if (accessMetaData) {
			processMetaData();
		}
		else {
			processDeclaredData();
		}

		List<SqlParameter> parameters = getCallParameters();
		int parameterCount = 0;
		if (isFunction()) {
			this.callString = "{ ? = call " +
					(useCatalogNameAsPackageName && catalogNameToUse != null && catalogNameToUse.length() > 0 ? catalogNameToUse + "." : "") +
					procedureNameToUse + "(";
			parameterCount = -1;
		}
		else {
			this.callString = "{ call " +
					(useCatalogNameAsPackageName && catalogNameToUse != null && catalogNameToUse.length() > 0 ? catalogNameToUse + "." : "") +
					procedureNameToUse + "(";
		}
		for (SqlParameter parameter : parameters) {
			if (!(parameter.isResultsParameter())) {
				if (parameterCount > 0) {
					this.callString += ", ";
				}
				if (parameterCount >= 0) {
					this.callString += "?";
				}
				parameterCount++;
			}
		}
		this.callString += ") }";
		if (logger.isDebugEnabled()) {
			logger.debug("Compiled stored procedure. Call string is [" + getCallString() + "]");
		}

		this.callableStatementFactory = new CallableStatementCreatorFactory(getCallString(), getCallParameters());
		//this.callableStatementFactory.setResultSetType(getResultSetType());
		//this.callableStatementFactory.setUpdatableResults(isUpdatableResults());
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
	 * Is this operation "compiled"? Compilation, as in JDO,
	 * means that the operation is fully configured, and ready to use.
	 * The exact meaning of compilation will vary between subclasses.
	 * @return whether this operation is compiled, and ready to use.
	 */
	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * Check whether this operation has been compiled already;
	 * lazily compile it if not already compiled.
	 * <p>Automatically called by <code>validateParameters</code>.
	 */
	protected void checkCompiled() {
		if (!isCompiled()) {
			logger.debug("SQL call not compiled before execution - invoking compile");
			compile();
		}
	}

	private void processDeclaredData() {
		procedureNameToUse = procName;
		catalogNameToUse = catalogName;
		schemaNameToUse = schemaName;
	}

	private void processMetaData() {
		final List<SqlParameter> parameters = new ArrayList<SqlParameter>();
		parameters.addAll(declaredReturnParameters);
		try {
			JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {

				public Object processMetaData(DatabaseMetaData databaseMetaData)
						throws SQLException, MetaDataAccessException {
					userName = databaseMetaData.getUserName();
					String databaseProductName = databaseMetaData.getDatabaseProductName();
					if ("Oracle".equals(databaseProductName)) {
						defaultSchemaToUserName = true;
						useCatalogNameAsPackageName = true;
					}
					supportsCatalogsInProcedureCalls = databaseMetaData.supportsCatalogsInProcedureCalls();
					supportsSchemasInProcedureCalls = databaseMetaData.supportsSchemasInProcedureCalls();
					storesUpperCaseIdentifiers = databaseMetaData.storesUpperCaseIdentifiers();
					storesLowerCaseIdentifiers = databaseMetaData.storesLowerCaseIdentifiers();
					if (storesUpperCaseIdentifiers) {
						procedureNameToUse = procName.toUpperCase();
						catalogNameToUse = catalogName != null ? catalogName.toUpperCase() : null;
						schemaNameToUse = schemaName != null ? schemaName.toUpperCase() : null;
						returnNameToUse = "RETURN";
					}
					else if (storesLowerCaseIdentifiers) {
						procedureNameToUse = procName.toLowerCase();
						catalogNameToUse = catalogName != null ? catalogName.toLowerCase() : null;
						schemaNameToUse = schemaName != null ? schemaName.toLowerCase() : null;
						returnNameToUse = "return";
					}
					else {
						procedureNameToUse = procName;
						catalogNameToUse = catalogName;
						schemaNameToUse = schemaName;
						returnNameToUse = "return";
					}
					// Oracle hack to distinguish between package and non-package functions/procedures with same name
					if (catalogNameToUse == null && useCatalogNameAsPackageName) {
						catalogNameToUse = "";
					}
					if (accessProcedureColumnMetaData) {
						String metaDataCatalogName = supportsCatalogsInProcedureCalls || useCatalogNameAsPackageName ?
								catalogNameToUse : null;
						String metaDataSchemaName = supportsSchemasInProcedureCalls ?
								(schemaNameToUse != null ? schemaNameToUse :
										(defaultSchemaToUserName ? userName : null)) : null;
						if (logger.isDebugEnabled()) {
							logger.debug("Retrieving metadata for " + metaDataCatalogName + "/" +
									metaDataSchemaName + "/" + procedureNameToUse);
						}
						ResultSet procs = null;
						try {
							procs = databaseMetaData.getProcedureColumns(
									metaDataCatalogName,
									metaDataSchemaName,
									procedureNameToUse,
									null);
							while (procs.next()) {
								if (logger.isDebugEnabled()) {
									logger.debug("Retrieved metadata: " + procs.getString("PROCEDURE_CAT") +
										" " + procs.getString("PROCEDURE_SCHEM") +
										" " + procs.getString("COLUMN_NAME") +
										" " + procs.getString("COLUMN_TYPE") +
										" " + procs.getInt("DATA_TYPE") +
										" " + procs.getString("TYPE_NAME") +
										" " + procs.getString("NULLABLE")
									);
								}
								String colName = procs.getString("COLUMN_NAME");
								int colType = procs.getInt("COLUMN_TYPE");
								if (logger.isDebugEnabled()) {
									logger.debug("Checking metadata parameter for: " + (colName == null ? returnNameToUse : colName));
								}
								String colNameToCheck;
								String colNameToUse;
								if (colName == null || colName.length() < 1) {
									colNameToCheck = colNameToUse = returnNameToUse;
								}
								else {
									colNameToUse = colName;
									colNameToCheck = caseSensitiveParameters ? colName : colName.toLowerCase();
								}
								if (colNameToUse.startsWith(removableProcedureColumnNamePrefix) && colNameToUse.length() > 1)
									colNameToUse = colNameToUse.substring(1);
								if (colNameToCheck.startsWith(removableProcedureColumnNamePrefix) && colNameToCheck.length() > 1)
									colNameToCheck = colNameToCheck.substring(1);
								if (!((colType == DatabaseMetaData.procedureColumnReturn && returnDeclared) ||
										declaredParameters.containsKey(colNameToCheck))) {
									int dataType = procs.getInt("DATA_TYPE");
									String typeName = procs.getString("TYPE_NAME");
									if (colType == DatabaseMetaData.procedureColumnReturn) {
										if (!isFunction() && "return_value".equals(colNameToCheck)) {
											if (logger.isDebugEnabled()) {
												logger.debug("Bypassing metadata return parameter for: " + colNameToUse);
											}
										}
										else {
											parameters.add(new SqlOutParameter(returnNameToUse, dataType));
											outParameterNames.add(returnNameToUse);
											setFunction(true);
											if (logger.isDebugEnabled()) {
												logger.debug("Added metadata return parameter for: " + returnNameToUse);
											}
										}
									}
									else {
										if (colType == DatabaseMetaData.procedureColumnOut || colType == DatabaseMetaData.procedureColumnInOut) {
											if("Oracle".equals(databaseProductName) && dataType == Types.OTHER && "REF CURSOR".equals(typeName)) {
												parameters.add(new SqlOutParameter(colNameToUse, -10, new ColumnMapRowMapper()));
											}
											else {
												parameters.add(new SqlOutParameter(colNameToUse, dataType));
											}
											outParameterNames.add(colNameToUse);
											if (logger.isDebugEnabled()) {
												logger.debug("Added metadata out parameter for: " + colNameToUse);
											}
										}
										else {
											parameters.add(new SqlParameter(colNameToUse, dataType));
											if (logger.isDebugEnabled()) {
												logger.debug("Added metadata in parameter for: " + colNameToUse);
											}
										}
									}
								}
								else {
									SqlParameter parameter;
									if (colType == 5) {
										parameter = declaredParameters.get(returnNameToUse);
									}
									else {
										parameter = declaredParameters.get(colNameToCheck);
									}
									if (parameter != null) {
										parameters.add(parameter);
										if (logger.isDebugEnabled()) {
											logger.debug("Using declared parameter for: " +
													(colNameToUse == null ? returnNameToUse : colNameToUse));
										}
									}
								}
							}
						}
						catch (SQLException se) {
							logger.warn("Error while retreiving metadata for procedure columns: " + se.getMessage());
						}
						finally {
							try {
								if (procs != null)
									procs.close();
							}
							catch (SQLException ignore) {}
						}
						if (parameters.size() == 0) {
							logger.warn("Problem retreiving metadata for procedure columns -->" +
									" Catalog: " + metaDataCatalogName +
									" Schema: " + metaDataSchemaName +
									" Object: " + procedureNameToUse);
							parameters.addAll(declaredReturnParameters);
							parameters.addAll(declaredParameters.values());
						}
					}
					else {
						parameters.addAll(declaredReturnParameters);
						parameters.addAll(declaredParameters.values());
					}
					return null;
				}
			});
		} catch (MetaDataAccessException e) {
			e.printStackTrace();
		}
		this.callParameters.addAll(parameters);
	}

	protected Map<String, Object> matchInParameterValuesWithCallParameters(Map<String, Object> inParameters) {
		if (!accessMetaData) {
			return inParameters;
		}
		Map<String, String> callParameterNames = new HashMap<String, String>(this.callParameters.size());
		for (SqlParameter parameter : callParameters) {
//			String parameterName =   parameter.getName() : parameter.getName().toLowerCase();
			String parameterName =  parameter.getName();
			String parameterNameToMatch = parameterName;
			if (parameterNameToMatch.startsWith(removableProcedureColumnNamePrefix) && parameterNameToMatch.length() > 1)
				parameterNameToMatch = parameterNameToMatch.substring(1);
			callParameterNames.put(parameterNameToMatch.toLowerCase(), parameterName);
		}
		Map<String, Object> matchedParameters = new HashMap<String, Object>(inParameters.size());
		for (String parameterName : inParameters.keySet()) {
			String parameterNameToMatch = parameterName;
			if (parameterNameToMatch.startsWith(removableProcedureColumnNamePrefix) && parameterNameToMatch.length() > 1)
				parameterNameToMatch = parameterNameToMatch.substring(1);
			String callParameterName = callParameterNames.get(parameterNameToMatch.toLowerCase());
			if (callParameterName == null) {
				logger.warn("Unable to locate the corresponding parameter for \"" + parameterName + "\" specified in the provided parameter values: " + inParameters);
			}
			else {
				matchedParameters.put(callParameterName, inParameters.get(parameterName));
			}

		}
		if (logger.isDebugEnabled()) {
			logger.debug("Matching " + inParameters + " with " + callParameterNames);
		}
		return matchedParameters;
	}
}
