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
