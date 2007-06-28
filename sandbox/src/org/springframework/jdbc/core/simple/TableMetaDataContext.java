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
import java.sql.Types;

/**
 * Class to hold context data for one of the MetaData strategy implementations of MetaDataProvider.
 *
 * @author trisberg
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

	/** List of columns objects to be used in insert execution */
	private List<String> insertColumns = new ArrayList<String>();

	/** should we access insert parameter meta data info or not */
	private boolean accessInsertParameterMetaData = true;

	/** the provider of call meta data */
	private TableMetaDataProvider metaDataProvider;


	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public boolean isAccessCallParameterMetaData() {
		return accessInsertParameterMetaData;
	}

	public void setAccessCallParameterMetaData(boolean accessInsertParameterMetaData) {
		this.accessInsertParameterMetaData = accessInsertParameterMetaData;
	}

	public List<String> getInsertColumns() {
		return insertColumns;
	}

	public boolean isGeneratedKeysColumnNameArraySupported() {
		return metaDataProvider.isGeneratedKeysColumnNameArraySupported();
	}

	public void processMetaData(DataSource dataSource, List<String> declaredColumns, String[] generatedKeyNames) {

		metaDataProvider =
				TableMetaDataProviderFactory.createMetaDataProvider(dataSource, this);

		insertColumns = reconcileColumnsToUse(declaredColumns, generatedKeyNames);

	}

	private List<String> reconcileColumnsToUse(List<String> declaredColumns, String[] generatedKeyNames) {
		if (declaredColumns.size() > 0) {
			return new ArrayList<String>(declaredColumns);
		}
		HashSet keys = new HashSet(generatedKeyNames.length);
		for (String key : generatedKeyNames) {
			keys.add(key.toUpperCase());
		}
		List<String> columns = new ArrayList<String>();
		for (TableParameterMetaData meta : metaDataProvider.getInsertParameterMetaData()) {
			if (!keys.contains(meta.getParameterName().toUpperCase())) {
				columns.add(meta.getParameterName());
			}
		}
		return columns;
	}

	public List<Object> matchInParameterValuesWithInsertColumns(SqlParameterSource parameterSource) {
		List<Object> values = new ArrayList<Object>();
		for (String column : insertColumns) {
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

	public List<Object> matchInParameterValuesWithInsertColumns(Map<String, Object> inParameters) {
		List<Object> values = new ArrayList<Object>();
		Map<String, Object> source = new HashMap<String, Object>();
		for (String key : inParameters.keySet()) {
			source.put(key.toLowerCase(), inParameters.get(key));
		}
		for (String column : insertColumns) {
			values.add(source.get(column.toLowerCase()));
		}
		return values;
	}


	public String createInsertString(String[] generatedKeyNames) {
		HashSet keys = new HashSet(generatedKeyNames.length);
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
		for (String columnName : this.getInsertColumns()) {
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

	public int[] createInsertTypes() {

		int[] types = new int[this.getInsertColumns().size()];

		List<TableParameterMetaData> parameters = this.metaDataProvider.getInsertParameterMetaData();
		Map<String, TableParameterMetaData> parameterMap = new HashMap<String, TableParameterMetaData>(parameters.size());
		for (TableParameterMetaData tpmd : parameters) {
			parameterMap.put(tpmd.getParameterName().toUpperCase(), tpmd);
		}

		int typeIndx = 0;
		for (String column : this.getInsertColumns()) {
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
