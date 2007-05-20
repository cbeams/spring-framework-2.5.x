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
	protected final List<SqlParameter> declaredParameters = new ArrayList<SqlParameter>();

	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled = false;

	protected String procName;
	protected String metaDataProcedureName;
	protected String catalogName;
	protected String schemaName;
	protected String userName;
	protected boolean defaultSchemaToUserName = false;
	private boolean function;
	protected boolean supportsCatalogsInProcedureCalls = true;
	protected boolean supportsSchemasInProcedureCalls = true;
	private boolean autoDetectParameters = true;
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

	public List<SqlParameter> getDeclaredParameters() {
		return declaredParameters;
	}

	public String getCallString() {
		return callString;
	}

	public AbstractJdbcCall setAutoDetectParameters(boolean autoDetect) {
		this.autoDetectParameters = autoDetect;
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
		if (autoDetectParameters) {
			processMetaData();
		}
		else {
			processRegisteredData();
		}

		List<SqlParameter> parameters = getDeclaredParameters();
		int parameterCount = 0;
		if (isFunction()) {
			this.callString = "{? = call " + metaDataProcedureName + "(";
			parameterCount = -1;
		}
		else {
			this.callString = "{call " + metaDataProcedureName + "(";
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
		this.callString += ")}";
		if (logger.isDebugEnabled()) {
			logger.debug("Compiled stored procedure. Call string is [" + getCallString() + "]");
		}

		this.callableStatementFactory = new CallableStatementCreatorFactory(getCallString(), getDeclaredParameters());
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

	private void processRegisteredData() {
		metaDataProcedureName = procName;
		for (int i = 0; i < declaredParameters.size(); i++) {
			SqlParameter param = declaredParameters.get(i);
			if (param instanceof SqlOutParameter) {
				outParameterNames.add(param.getName());
			}
		}
	}

	private void processMetaData() {
		final List parameters = new ArrayList();
		try {
			JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {

				public Object processMetaData(DatabaseMetaData databaseMetaData) throws SQLException, MetaDataAccessException {
					userName = databaseMetaData.getUserName();
					System.out.println("->" + userName);
					if ("Oracle".equals(databaseMetaData.getDatabaseProductName())) {
						defaultSchemaToUserName = true;
					}
					supportsCatalogsInProcedureCalls = databaseMetaData.supportsCatalogsInProcedureCalls();
					supportsSchemasInProcedureCalls = databaseMetaData.supportsSchemasInProcedureCalls();
					System.out.println("->" + databaseMetaData.allProceduresAreCallable());
					System.out.println("->" + databaseMetaData.getCatalogTerm());
					System.out.println("->" + databaseMetaData.getSchemaTerm());
					System.out.println("->" + databaseMetaData.getCatalogSeparator());
					storesUpperCaseIdentifiers = databaseMetaData.storesUpperCaseIdentifiers();
					storesLowerCaseIdentifiers = databaseMetaData.storesLowerCaseIdentifiers();
					if (storesUpperCaseIdentifiers) {
						metaDataProcedureName = procName.toUpperCase();
					}
					else if (storesLowerCaseIdentifiers) {
						metaDataProcedureName = procName.toLowerCase();
					}
					else {
						metaDataProcedureName = procName;
					}
					ResultSet procs = databaseMetaData.getProcedureColumns(
							supportsCatalogsInProcedureCalls ? catalogName : null,
							supportsSchemasInProcedureCalls ? (schemaName != null ? schemaName : (defaultSchemaToUserName ? userName : null)) : null,
							metaDataProcedureName,
							null);
					while (procs.next()) {
						String colName = procs.getString("COLUMN_NAME");
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
						System.out.println(">>" + procs.getString("PROCEDURE_CAT") +
								" " + procs.getString("PROCEDURE_SCHEM") +
								" " + procs.getString("COLUMN_NAME") +
								" " + procs.getString("COLUMN_TYPE") +
								" " + procs.getString("DATA_TYPE") +
								" " + procs.getString("TYPE_NAME")
						);
					}
					return null;
				}
			});
		} catch (MetaDataAccessException e) {
			e.printStackTrace();
		}
		this.declaredParameters.addAll(parameters);
	}
}
