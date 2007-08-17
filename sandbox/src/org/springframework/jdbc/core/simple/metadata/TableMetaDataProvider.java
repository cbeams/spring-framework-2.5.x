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

package org.springframework.jdbc.core.simple.metadata;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface specifying the API to be implemented by a class providing table metedata.  This is intended for internal use
 * by the Simple JDBC classes.
 *
 * @author trisberg
 * @since 2.1
 */
public interface TableMetaDataProvider {

	/**
	 * Initialize using the database metedata provided
	 * @param databaseMetaData
	 * @throws SQLException
	 */
	void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException;

	/**
	 * Initialize using provided database metadata, table and column information
	 * @param databaseMetaData
	 * @param catalogName
	 * @param schemaName
	 * @param tableName
	 * @throws SQLException
	 */
	void initializeWithTableColumnMetaData(DatabaseMetaData databaseMetaData, String catalogName, String schemaName, String tableName)
			throws SQLException;

	/**
	 * Get the table name formatted based on metadata information
	 *
	 * @param tableName
	 * @return table name formatted
	 */
	String tableNameToUse(String tableName);

	/**
	 * Get the catalog name formatted based on metadata information
	 *
	 * @param catalogName
	 * @return catalog name formatted
	 */
	String catalogNameToUse(String catalogName);

	/**
	 * Get the schema name formatted based on metadata information
	 *
	 * @param schemaName
	 * @return schema name formatted
	 */
	String schemaNameToUse(String schemaName);

	/**
	 * Get the catalog name to be used for metedata lookups
	 *
	 * @param catalogName
	 * @return catalog name to use
	 */
	String metaDataCatalogNameToUse(String catalogName) ;

	/**
	 * Get the schema name to be used for metedata lookups
	 *
	 * @param schemaName
	 * @return schema name to use
	 */
	String metaDataSchemaNameToUse(String schemaName) ;

	/**
	 * Get whether column metedata should be used
	 */
 	boolean isTableColumnMetaDataUsed();
	
	/**
	 * Get whether a column name array is supported for generated keys
	 */
 	boolean isGeneratedKeysColumnNameArraySupported();

	/**
	 * Specify whether column metedata should be used
	 */
	List<TableParameterMetaData> getInsertParameterMetaData();

}
