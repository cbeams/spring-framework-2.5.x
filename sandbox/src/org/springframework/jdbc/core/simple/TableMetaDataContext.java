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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.metadata.TableMetaDataProvider;
import org.springframework.jdbc.core.simple.metadata.TableMetaDataProviderFactory;
import org.springframework.jdbc.core.simple.metadata.TableParameterMetaData;
import org.springframework.jdbc.core.SqlTypeValue;

import javax.sql.DataSource;
import java.util.*;

/**
 * Class to manage context metadata used for the configuration and execution of operations on the table.
 *
 * @author Thomas Risberg
 * @since 2.1
 */
public class TableMetaDataContext {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** name of procedure to call **/
	private String tableName;

	/** name of catalog for call **/
	private String catalogName;

	/** name of schema for call **/
	private String schemaName;

	/** List of columns objects to be used in this context */
	private List<String> tableColumns = new ArrayList<String>();

	/** should we access insert parameter meta data info or not */
	private boolean accessTableParameterMetaData = true;

	/** the provider of call meta data */
	private TableMetaDataProvider metaDataProvider;


	/**
	 * Get the name of the table for this context
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Set the name of the table for this context
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Get the name of the catalog for this context
	 */
	public String getCatalogName() {
		return catalogName;
	}

	/**
	 * Set the name of the catalog for this context
	 */
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	/**
	 * Get the name of the schema for this context
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * Set the name of the schema for this context
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * Are we accessing table meta data
	 */
	public boolean isAccessTableParameterMetaData() {
		return accessTableParameterMetaData;
	}

	/**
	 * Specify whether we should access table column meta data
	 */
	public void setAccessTableParameterMetaData(boolean accessTableParameterMetaData) {
		this.accessTableParameterMetaData = accessTableParameterMetaData;
	}

	/**
	 * Get a List of the table column names
	 */
	public List<String> getTableColumns() {
		return tableColumns;
	}

	/**
	 * Is a column name String array for retreiving generated keys supported
	 * {@link java.sql.Connection#createStruct(String, Object[])}
	 */
	public boolean isGeneratedKeysColumnNameArraySupported() {
		return metaDataProvider.isGeneratedKeysColumnNameArraySupported();
	}

	/**
	 * Process the current meta data with the provided configuration options
	 * @param dataSource the DataSource being used
	 * @param declaredColumns any coluns that are declared
	 * @param generatedKeyNames name of generated keys
	 */
	public void processMetaData(DataSource dataSource, List<String> declaredColumns, String[] generatedKeyNames) {

		metaDataProvider =
				TableMetaDataProviderFactory.createMetaDataProvider(dataSource, this);

		tableColumns = reconcileColumnsToUse(declaredColumns, generatedKeyNames);

	}

	/**
	 * Compare columns created from metadata with declared columns and return a reconciled list
	 * @param declaredColumns declared column names
	 * @param generatedKeyNames names of generated key columns
	 */
	private List<String> reconcileColumnsToUse(List<String> declaredColumns, String[] generatedKeyNames) {
		if (declaredColumns.size() > 0) {
			return new ArrayList<String>(declaredColumns);
		}
		HashSet keys = new HashSet(generatedKeyNames.length);
		for (String key : generatedKeyNames) {
			keys.add(key.toUpperCase());
		}
		List<String> columns = new ArrayList<String>();
		for (TableParameterMetaData meta : metaDataProvider.getTableParameterMetaData()) {
			if (!keys.contains(meta.getParameterName().toUpperCase())) {
				columns.add(meta.getParameterName());
			}
		}
		return columns;
	}

	/**
	 * Match the provided column names and values with the list of columns used
	 *
	 * @param parameterSource the parameter names and values
	 */
	//TODO provide a SqlParameterValue when sql type is specified
	public List<Object> matchInParameterValuesWithInsertColumns(SqlParameterSource parameterSource) {
		List<Object> values = new ArrayList<Object>();
		for (String column : tableColumns) {
			if (parameterSource.hasValue(column.toLowerCase())) {
				values.add(parameterSource.getValue(column.toLowerCase()));
			}
			else {
				String propertyName = SimpleJdbcUtils.convertUnderscoreNameToPropertyName(column);
				if (parameterSource.hasValue(propertyName)) {
					values.add(parameterSource.getValue(propertyName));
				}
				else {
					values.add(null);
				}
			}
		}
		return values;
	}

	/**
	 * Match the provided column names and values with the list of columns used
	 *
	 * @param inParameters the parameter names and values
	 */
	//TODO provide a SqlParameterValue when sql type is specified
	public List<Object> matchInParameterValuesWithInsertColumns(Map<String, Object> inParameters) {
		List<Object> values = new ArrayList<Object>();
		Map<String, Object> source = new HashMap<String, Object>();
		for (String key : inParameters.keySet()) {
			source.put(key.toLowerCase(), inParameters.get(key));
		}
		for (String column : tableColumns) {
			values.add(source.get(column.toLowerCase()));
		}
		return values;
	}


	/**
	 * Build the insert string based on configuration and metadata information
	 * @return the insert string to be used
	 */
	public String createInsertString(String[] generatedKeyNames) {
		HashSet<String> keys = new HashSet<String>(generatedKeyNames.length);
		for (String key : generatedKeyNames) {
			keys.add(key.toUpperCase());
		}
		StringBuilder insertStatement = new StringBuilder();
		insertStatement.append("INSERT INTO ");
		if (this.getSchemaName() != null) {
			insertStatement.append(this.getSchemaName());
			insertStatement.append(".");
		}
		insertStatement.append(this.getTableName());
		insertStatement.append(" (");
		int columnCount = 0;
		for (String columnName : this.getTableColumns()) {
			if (!keys.contains(columnName.toUpperCase())) {
				columnCount++;
				if (columnCount > 1) {
					insertStatement.append(", ");
				}
				insertStatement.append(columnName);
			}
		}
		insertStatement.append(") VALUES(");
		if (columnCount < 1) {
			throw new InvalidDataAccessApiUsageException("Unable to locate columns for table " + this.getTableName());
		}
		for (int i = 0; i < columnCount; i++) {
			if (i > 0) {
				insertStatement.append(", ");
			}
			insertStatement.append("?");
		}
		insertStatement.append(")");
		return insertStatement.toString();
	}

	/**
	 * Build the array of {@link java.sql.Types} based on configuration and metadata information
	 * @return the array of types to be used
	 */
	public int[] createInsertTypes() {

		int[] types = new int[this.getTableColumns().size()];

		List<TableParameterMetaData> parameters = this.metaDataProvider.getTableParameterMetaData();
		Map<String, TableParameterMetaData> parameterMap = new HashMap<String, TableParameterMetaData>(parameters.size());
		for (TableParameterMetaData tpmd : parameters) {
			parameterMap.put(tpmd.getParameterName().toUpperCase(), tpmd);
		}

		int typeIndx = 0;
		for (String column : this.getTableColumns()) {
			if (column == null) {
				types[typeIndx] = SqlTypeValue.TYPE_UNKNOWN;
			}
			else {
				TableParameterMetaData tpmd = parameterMap.get(column.toUpperCase());
				if (tpmd != null) {
					types[typeIndx] = tpmd.getSqlType();
				}
				else {
					types[typeIndx] = SqlTypeValue.TYPE_UNKNOWN;
				}
			}
			typeIndx++;
		}

		return types;
	}

}
