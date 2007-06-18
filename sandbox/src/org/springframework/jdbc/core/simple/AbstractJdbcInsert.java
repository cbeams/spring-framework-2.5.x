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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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

	private int[] insertTypes;

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

	public List<String> getColumnNames() {
		return Collections.unmodifiableList(declaredColumns);
	}


	public String getInsertString() {
		return insertString;
	}

	public int[] getInsertTypes() {
		return insertTypes;
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

		insertTypes = tableMetaDataContext.createInsertTypes();

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

	public int doExecute(Map<String, Object> args) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(args);
		return executeInsert(values);
	}

	public int doExecute(SqlParameterSource parameterSource) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(parameterSource);
		return executeInsert(values);
	}

	private int executeInsert(List<Object> values) {
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for call " + getInsertString() + " with: " + values);
		}
		int updateCount = jdbcTemplate.update(getInsertString(), values.toArray());
		return updateCount;
	}

	public Number doExecuteAndReturnKey(Map<String, Object> args) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(args);
		return executeInsertAndReturnKey(values);
	}

	public Number doExecuteAndReturnKey(SqlParameterSource parameterSource) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(parameterSource);
		return executeInsertAndReturnKey(values);
	}

	private Number executeInsertAndReturnKey(final List<Object> values) {
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for call " + getInsertString() + " with: " + values);
		}
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int updateCount = jdbcTemplate.update(
				new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
						PreparedStatement ps =
                        //Option1: connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                        //Option2: connection.prepareStatement(INSERT_SQL, new int[] {1});
                        //Option3: connection.prepareStatement(INSERT_SQL, new String[] {"id"});
                        con.prepareStatement(getInsertString(), Statement.RETURN_GENERATED_KEYS);
						int colIndex = 0;
						for (Object value : values) {
							colIndex++;
							StatementCreatorUtils.setParameterValue(ps, colIndex, SqlTypeValue.TYPE_UNKNOWN, value);
						}
						return ps;
					}
				},
				keyHolder);
		if (updateCount > 0) {
			return keyHolder.getKey();
		}
		else {
			return null;
		}
	}

	public int[] doExecuteBatch(Map<String, Object>[] batch) {
		checkCompiled();
		List[] batchValues = new ArrayList[batch.length];
		int i = 0;
		for (Map<String, Object> args : batch) {
			List<Object> values = matchInParameterValuesWithInsertColumns(args);
			batchValues[i++] = values;
		}
		return executeBatch(batchValues);
	}

	public int[] doExecuteBatch(SqlParameterSource[] batch) {
		checkCompiled();
		List[] batchValues = new ArrayList[batch.length];
		int i = 0;
		for (SqlParameterSource parameterSource : batch) {
			List<Object> values = matchInParameterValuesWithInsertColumns(parameterSource);
			batchValues[i++] = values;
		}
		return executeBatch(batchValues);
	}

	private int[] executeBatch(final List[] batchValues) {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing statement " + getInsertString() + " with batch of size: " + batchValues.length);
		}
		final int[] columnTypes = getInsertTypes();
		int[] updateCounts = jdbcTemplate.batchUpdate(
				getInsertString(),
				new BatchPreparedStatementSetter() {

					public void setValues(PreparedStatement ps, int i) throws SQLException {
						List values = batchValues[i];
						int colIndex = 0;
						for (Object value : values) {
							colIndex++;
							StatementCreatorUtils.setParameterValue(ps, colIndex, columnTypes[colIndex - 1], value);
						}

					}

					public int getBatchSize() {
						return batchValues.length;
					}
				});
		return updateCounts;
	}

	protected List<Object> matchInParameterValuesWithInsertColumns(SqlParameterSource parameterSource) {
		if (parameterSource instanceof MapSqlParameterSource) {
			return matchInParameterValuesWithInsertColumns(((MapSqlParameterSource) parameterSource).getValues());
		}
		else {
			return tableMetaDataContext.matchInParameterValuesWithInsertColumns(parameterSource);
		}
	}

	protected List<Object> matchInParameterValuesWithInsertColumns(Map<String, Object> args) {
		return tableMetaDataContext.matchInParameterValuesWithInsertColumns(args);
	}



}
