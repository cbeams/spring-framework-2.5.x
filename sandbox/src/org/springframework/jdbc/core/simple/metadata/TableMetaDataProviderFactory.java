package org.springframework.jdbc.core.simple.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.TableMetaDataContext;
import org.springframework.jdbc.core.simple.SimpleJdbcUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author trisberg
 */
public class TableMetaDataProviderFactory {

	/** Logger */
	private static final Log logger = LogFactory.getLog(TableMetaDataProviderFactory.class);

	static public TableMetaDataProvider createMetaDataProvider(DataSource dataSource,
															 final TableMetaDataContext context) {
		try {
			return (TableMetaDataProvider) JdbcUtils.extractDatabaseMetaData(
					dataSource, new DatabaseMetaDataCallback() {

				public Object processMetaData(DatabaseMetaData databaseMetaData)
						throws SQLException, MetaDataAccessException {
					String databaseProductName =
							SimpleJdbcUtils.commonDatabaseName(databaseMetaData.getDatabaseProductName());
					boolean accessTableColumnMetaData = context.isAccessCallParameterMetaData();
					TableMetaDataProvider provider = new GenericTableMetaDataProvider(databaseMetaData);
					if (logger.isDebugEnabled()) {
						logger.debug("Using " + provider.getClass().getName());
					}
					provider.initializeWithMetaData(databaseMetaData);
					if (accessTableColumnMetaData) {
						provider.initializeWithTableColumnMetaData(databaseMetaData, context.getCatalogName(), context.getSchemaName(), context.getTableName());
					}
					return provider;
				}
			});
		} catch (MetaDataAccessException e) {
			throw new DataAccessResourceFailureException("Error retreiving database metadata", e);
		}

	}

}
