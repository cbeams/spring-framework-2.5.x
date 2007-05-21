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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;

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

	/** List of SqlParameter objects */
	private final List<String> declaredParameterNames = new ArrayList<String>();

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
	protected boolean defaultSchemaToUserName = false;
	protected boolean useCatalogNameAsPackageName = false;
	private boolean function;
	protected boolean supportsCatalogsInProcedureCalls = true;
	protected boolean supportsSchemasInProcedureCalls = true;
	private boolean accessMetaData = true;
	protected boolean storesUpperCaseIdentifiers = false;
	protected boolean storesLowerCaseIdentifiers = false;
	protected List<String> outParameterNames = new ArrayList<String>();
	protected String callString;

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
		callParameters.add(parameter);
		declaredParameterNames.add(parameter.getName());
	}
	public List<SqlParameter> getCallParameters() {
		return callParameters;
	}

	public String getCallString() {
		return callString;
	}

	public AbstractJdbcCall setAccessMetaData(boolean accessMetaData) {
		this.accessMetaData = accessMetaData;
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
					(useCatalogNameAsPackageName && catalogNameToUse != null ? catalogNameToUse + "." : "") +
					procedureNameToUse + "(";
			parameterCount = -1;
		}
		else {
			this.callString = "{ call " +
					(useCatalogNameAsPackageName && catalogNameToUse != null ? catalogNameToUse + "." : "") +
					procedureNameToUse + "(";
		}
		for (SqlParameter parameter : parameters) {
			if (!(parameter instanceof SqlReturnResultSet)) {
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
		for (int i = 0; i < callParameters.size(); i++) {
			SqlParameter param = callParameters.get(i);
			if (param instanceof SqlOutParameter) {
				outParameterNames.add(param.getName());
			}
		}
	}

	private void processMetaData() {
		final List<SqlParameter> parameters = new ArrayList<SqlParameter>();
		try {
			JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {

				public Object processMetaData(DatabaseMetaData databaseMetaData)
						throws SQLException, MetaDataAccessException {
					userName = databaseMetaData.getUserName();
					if ("Oracle".equals(databaseMetaData.getDatabaseProductName())) {
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
					}
					else if (storesLowerCaseIdentifiers) {
						procedureNameToUse = procName.toLowerCase();
						catalogNameToUse = catalogName != null ? catalogName.toLowerCase() : null;
						schemaNameToUse = schemaName != null ? schemaName.toLowerCase() : null;
					}
					else {
						procedureNameToUse = procName;
						catalogNameToUse = catalogName;
						schemaNameToUse = schemaName;
					}
					String metaDataCatalogName = supportsCatalogsInProcedureCalls || useCatalogNameAsPackageName ?
							catalogNameToUse : null;
					String metaDataSchemaName = supportsSchemasInProcedureCalls ?
							(schemaNameToUse != null ? schemaNameToUse :
									(defaultSchemaToUserName ? userName : null)) : null;
					if (logger.isDebugEnabled()) {
						logger.debug("Retreiving metadata for " + metaDataCatalogName + "/" +
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
							String colName = procs.getString("COLUMN_NAME");
							if (!declaredParameterNames.contains(colName)) {
								int dataType = procs.getInt("DATA_TYPE");
								int colType = procs.getInt("COLUMN_TYPE");
								if (colType == 5) {
									parameters.add(new SqlOutParameter("return", dataType));
									outParameterNames.add("return");
									setFunction(true);
								}
								else {
									if (colType == 4) {
										parameters.add(new SqlOutParameter(colName, dataType));
										outParameterNames.add(colName);
									}
									else {
										parameters.add(new SqlParameter(colName, dataType));
									}
								}
							}
							if (logger.isDebugEnabled()) {
								logger.debug(">>" + procs.getString("PROCEDURE_CAT") +
									" " + procs.getString("PROCEDURE_SCHEM") +
									" " + procs.getString("COLUMN_NAME") +
									" " + procs.getString("COLUMN_TYPE") +
									" " + procs.getString("DATA_TYPE") +
									" " + procs.getString("TYPE_NAME")
								);
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
					return null;
				}
			});
		} catch (MetaDataAccessException e) {
			e.printStackTrace();
		}
		this.callParameters.addAll(parameters);
	}
}
