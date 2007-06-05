package org.springframework.jdbc.core.simple.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author trisberg
 */
public class GenericTableMetaDataProvider implements TableMetaDataProvider {

	/** Logger available to subclasses */
	protected static final Log logger = LogFactory.getLog(TableMetaDataProvider.class);

	private boolean tableColumnMetaDataUsed = false;

	private String userName;

	private boolean storesUpperCaseIdentifiers = true;

	private boolean storesLowerCaseIdentifiers = false;

	private List<TableParameterMetaData> insertParameterMetaData = new ArrayList<TableParameterMetaData>();

	protected GenericTableMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		userName = databaseMetaData.getUserName();
	}

	public void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException {

		try {
			setStoresUpperCaseIdentifiers(databaseMetaData.storesUpperCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.storesUpperCaseIdentifiers' - " + se.getMessage());
		}
		try {
			setStoresLowerCaseIdentifiers(databaseMetaData.storesLowerCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.storesLowerCaseIdentifiers' - " + se.getMessage());
		}

	}

	public void initializeWithTableColumnMetaData(DatabaseMetaData databaseMetaData, String catalogName, String schemaName, String tableName)
			throws SQLException {

		tableColumnMetaDataUsed = true;

		processTableColumns(databaseMetaData, catalogName, schemaName, tableName);

	}

	public String tableNameToUse(String tableName) {
		if (tableName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return tableName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return tableName.toLowerCase();
		else
			return tableName;
	}

	public String catalogNameToUse(String catalogName) {
		if (catalogName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return catalogName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return catalogName.toLowerCase();
		else
		return catalogName;
	}

	public String schemaNameToUse(String schemaName) {
		if (schemaName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return schemaName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return schemaName.toLowerCase();
		else
		return schemaName;
	}

	public String metaDataCatalogNameToUse(String catalogName) {
		return catalogNameToUse(catalogName);
	}

	public String metaDataSchemaNameToUse(String schemaName) {
		if (schemaName == null) {
			return userName;
		}
		return schemaNameToUse(schemaName);
	}

	private void processTableColumns(DatabaseMetaData databaseMetaData, String catalogName, String schemaName, String tableName) {
		ResultSet procs = null;
		String metaDataCatalogName = metaDataCatalogNameToUse(catalogName);
		String metaDataSchemaName = metaDataSchemaNameToUse(schemaName);
		String metaDataTableName = tableNameToUse(tableName);
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving metadata for " + metaDataCatalogName + "/" +
					metaDataSchemaName + "/" + metaDataTableName);
		}
		try {
			procs = databaseMetaData.getColumns(
					metaDataCatalogName,
					metaDataSchemaName,
					metaDataTableName,
					null);
			while (procs.next()) {
				TableParameterMetaData meta = new TableParameterMetaData(
						procs.getString("COLUMN_NAME"),
						procs.getInt("DATA_TYPE"),
						procs.getBoolean("NULLABLE")
				);
				insertParameterMetaData.add(meta);
				if (logger.isDebugEnabled()) {
					logger.debug("Retrieved metadata: "
						+ meta.getParameterName() +
						" " + meta.getSqlType() +
						" " + meta.isNullable()
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
			catch (SQLException se) {
				logger.warn("Problem closing resultset for procedure column metadata " + se.getMessage());
			}
		}

	}


	public boolean isStoresUpperCaseIdentifiers() {
		return storesUpperCaseIdentifiers;
	}

	public void setStoresUpperCaseIdentifiers(boolean storesUpperCaseIdentifiers) {
		this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
	}

	public boolean isStoresLowerCaseIdentifiers() {
		return storesLowerCaseIdentifiers;
	}

	public void setStoresLowerCaseIdentifiers(boolean storesLowerCaseIdentifiers) {
		this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
	}

	public List<TableParameterMetaData> getInsertParameterMetaData() {
		return insertParameterMetaData;
	}
}
