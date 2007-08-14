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
 * @author trisberg
 */
public interface TableMetaDataProvider {

	void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException;

	void initializeWithTableColumnMetaData(DatabaseMetaData databaseMetaData, String catalogName, String schemaName, String tableName) throws SQLException;

	String tableNameToUse(String tableName);

	String catalogNameToUse(String catalogName);

	String schemaNameToUse(String schemaName);

	String metaDataCatalogNameToUse(String catalogName) ;

	String metaDataSchemaNameToUse(String catalogName) ;

 	boolean isTableColumnMetaDataUsed();
	
 	boolean isGeneratedKeysColumnNameArraySupported();

	List<TableParameterMetaData> getInsertParameterMetaData();

}
