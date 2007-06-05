package org.springframework.jdbc.core.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author trisberg
 */
public class AbstractJdbcInsert {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Lower-level class used to execute SQL */
	private JdbcTemplate jdbcTemplate = new JdbcTemplate();

	/** List of columns objects to be used in insert execution */
	private List<String> declaredColumns = new ArrayList<String>();

	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled = false;

	private String insertString;

	private TableMetaDataContext tableMetaDataContext = new TableMetaDataContext();


	protected AbstractJdbcInsert(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	protected AbstractJdbcInsert(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public String getTableName() {
		return tableMetaDataContext.getTableName();
	}

	public void setTableName(String tableName) {
		tableMetaDataContext.setTableName(tableName);
	}

	public String getSchemaName() {
		return tableMetaDataContext.getSchemaName();
	}

	public void setSchemaName(String schemaName) {
		tableMetaDataContext.setSchemaName(schemaName);
	}

	public String getCatalogName() {
		return tableMetaDataContext.getCatalogName();
	}

	public void setCatalogName(String catalogName) {
		tableMetaDataContext.setCatalogName(catalogName);
	}

	public void setColumnNames(List<String> columnNames) {
		declaredColumns.clear();
		declaredColumns.addAll(columnNames);
	}

	public List getColumnNames() {
		return Collections.unmodifiableList(declaredColumns);
	}


	public String getInsertString() {
		return insertString;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * Compile this query.
	 * Ignores subsequent attempts to compile.
	 * @throws org.springframework.dao.InvalidDataAccessApiUsageException if the object hasn't
	 * been correctly initialized, for example if no DataSource has been provided
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (getTableName() == null) {
				throw new InvalidDataAccessApiUsageException("Table name is required");
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
				logger.debug("SqlInsert for table [" + getTableName() + "] compiled");
			}
		}
	}

	/**
	 * Overridden method to configure the CallableStatementCreatorFactory
	 * based on our declared parameters.
	 * @see org.springframework.jdbc.object.RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {

		tableMetaDataContext.processMetaData(getJdbcTemplate().getDataSource(), getColumnNames());

		insertString = tableMetaDataContext.createInsertString();

		if (logger.isDebugEnabled()) {
			logger.debug("Compiled JdbcInsert. Insert string is [" + getInsertString() + "]");
		}

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

	public int doExecute(Map args) {
		checkCompiled();
		List values = matchInParameterValuesWithInsertColumns(args);
		return executeInsert(values);
	}

	public int doExecute(SqlParameterSource parameterSource) {
		checkCompiled();
		List values = null;
		if (parameterSource instanceof MapSqlParameterSource) {
			values = matchInParameterValuesWithInsertColumns(((MapSqlParameterSource) parameterSource).getValues());
		}
		else {
			values = matchInParameterValuesWithInsertColumns(parameterSource);
		}
		return executeInsert(values);
	}

	private int executeInsert(List values) {
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for call " + getInsertString() + " with: " + values);
		}
		logger.warn("====>" + getInsertString());
		int updateCount = jdbcTemplate.update(getInsertString(), values.toArray());
		return updateCount;
	}

	protected List<Object> matchInParameterValuesWithInsertColumns(SqlParameterSource parameterSource) {
		return tableMetaDataContext.matchInParameterValuesWithInsertColumns(parameterSource);
	}

	protected List<Object> matchInParameterValuesWithInsertColumns(Map<String, Object> args) {
		return tableMetaDataContext.matchInParameterValuesWithInsertColumns(args);
	}



}
