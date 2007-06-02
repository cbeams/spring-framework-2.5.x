package org.springframework.jdbc.core.simple.metadata;

import org.springframework.jdbc.core.simple.CallMetaDataContext;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author trisberg
 */
public class Db2CallMetaDataProvider extends AbstractCallMetaDataProvider {

	public Db2CallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}

	public void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException {
		try {
			setSupportsCatalogsInProcedureCalls(databaseMetaData.supportsCatalogsInProcedureCalls());
		}
		catch (SQLException se) {
			logger.debug("Error retrieving 'DatabaseMetaData.supportsCatalogsInProcedureCalls' - " + se.getMessage());
		}
		try {
			setSupportsSchemasInProcedureCalls(databaseMetaData.supportsSchemasInProcedureCalls());
		}
		catch (SQLException se) {
			logger.debug("Error retrieving 'DatabaseMetaData.supportsSchemasInProcedureCalls' - " + se.getMessage());
		}
		try {
			setStoresUpperCaseIdentifiers(databaseMetaData.storesUpperCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.debug("Error retrieving 'DatabaseMetaData.storesUpperCaseIdentifiers' - " + se.getMessage());
		}
		try {
			setStoresLowerCaseIdentifiers(databaseMetaData.storesLowerCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.debug("Error retrieving 'DatabaseMetaData.storesLowerCaseIdentifiers' - " + se.getMessage());
		}
	}

}
