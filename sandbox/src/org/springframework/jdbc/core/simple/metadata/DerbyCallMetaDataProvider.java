package org.springframework.jdbc.core.simple.metadata;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author trisberg
 */
public class DerbyCallMetaDataProvider extends GenericCallMetaDataProvider {

	public DerbyCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}

	@Override
	public String metaDataSchemaNameToUse(String schemaName) {
		// Use current user schema if no schema specified
		return schemaName == null ? getUserName().toUpperCase() : super.metaDataSchemaNameToUse(schemaName);
	}
}
