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

import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.jdbc.core.simple.metadata.OracleCallMetaDataProvider;
import org.springframework.jdbc.core.simple.CallMetaDataContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;

/**
 * @author trisberg
 */
public class CallMetaDataProviderFactory {

	/** Logger */
	private static final Log logger = LogFactory.getLog(CallMetaDataProviderFactory.class);

	public static final List<String> supportedDatabaseProductsForProcedures = Arrays.asList(
			"Apache Derby",
			"MySQL",
			"Microsoft SQL Server",
			"Oracle"
		);
	public static final List<String> supportedDatabaseProductsForFunctions = Arrays.asList(
			"MySQL",
			"Microsoft SQL Server",
			"Oracle"
		);

	static public CallMetaDataProvider createMetaDataProcessor(DataSource dataSource,
															 final CallMetaDataContext context) {
		try {
			return (CallMetaDataProvider)JdbcUtils.extractDatabaseMetaData(
					dataSource, new DatabaseMetaDataCallback() {

				public Object processMetaData(DatabaseMetaData databaseMetaData)
						throws SQLException, MetaDataAccessException {
					String databaseProductName = databaseMetaData.getDatabaseProductName();
					boolean accessProcedureColumnMetaData = context.isAccessProcedureColumnMetaData();
					if (context.isFunction()) {
						if (!supportedDatabaseProductsForFunctions.contains(databaseProductName)) {
							logger.warn(databaseProductName + " is not one of the databases fully supported for function calls -- supported are: " +
									supportedDatabaseProductsForFunctions);
							if (accessProcedureColumnMetaData) {
								logger.warn("Metadata processing disabled - you must specify all parameters explicitly");
								accessProcedureColumnMetaData = false;
							}
						}
					}
					else {
						if (!supportedDatabaseProductsForProcedures.contains(databaseProductName)) {
							logger.warn(databaseProductName + " is not one of the databases fully supported for procedure calls -- supported are: " +
									supportedDatabaseProductsForProcedures);
							if (accessProcedureColumnMetaData) {
								logger.warn("Metadata processing disabled - you must specify all parameters explicitly");
								accessProcedureColumnMetaData = false;
							}
						}
					}

					CallMetaDataProvider provider;
					if ("Oracle".equals(databaseProductName)) {
						provider = new OracleCallMetaDataProvider(databaseMetaData);
					}
					else if ("Microsoft SQL Server".equals(databaseProductName)) {
						provider = new SqlServerCallMetaDataProvider((databaseMetaData));
					}
					else {
						provider = new GenericCallMetaDataProvider(databaseMetaData);
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Using " + provider.getClass().getName());
					}
					provider.initializeWithMetaData(databaseMetaData);
					if (accessProcedureColumnMetaData) {
						provider.initializeWithProcedureColumnMetaData(databaseMetaData, context);
					}
					return provider;
				}
			});
		} catch (MetaDataAccessException e) {
			throw new DataAccessResourceFailureException("Error retreiving database metadata", e);
		}

	}

}
