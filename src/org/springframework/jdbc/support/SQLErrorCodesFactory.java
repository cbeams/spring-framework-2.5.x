/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jdbc.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Factory for creating SQLErrorCodes based on the
 * databaseProductName taken from the DatabaseMetaData.
 *
 * <p>Returns SQLErrorCodes populated with vendor codes
 * defined in a configuration file named "sql-error-codes.xml".
 * Reads the default file in this package if not overridden by a file
 * in the root of the classpath (e.g. in the WEB-INF/classes directory).
 *
 * @author Thomas Risberg
 * @author Rod Johnson
 * @version $Id: SQLErrorCodesFactory.java,v 1.9 2004-04-01 02:07:11 trisberg Exp $
 * @see java.sql.DatabaseMetaData#getDatabaseProductName
 */
public class SQLErrorCodesFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Name of custom SQL error codes file, loading from the root
	 * of the class path (e.g. in the WEB-INF/classes directory).
	 */
	public static final String SQL_ERROR_CODE_OVERRIDE_PATH = "sql-error-codes.xml";

	/**
	 * Name of default SQL error code files, loading from the class path.
	 */
	public static final String SQL_ERROR_CODE_DEFAULT_PATH = "org/springframework/jdbc/support/sql-error-codes.xml";

	/**
	 * Keep track of this instance so we can return it to classes that request it.
	 */
	private static final SQLErrorCodesFactory instance;

	static {
		instance = new SQLErrorCodesFactory();
	}

	/**
	 * Return singleton instance.
	 */
	public static SQLErrorCodesFactory getInstance() {
		return instance;
	}


	/**
	* Create a Map to hold error codes for all databases defined in the config file.
	*/
	private Map rdbmsErrorCodes;

	/**
	* Create a Map to hold database product name retreived from database metadata.
	*/
	private Map dataSourceProductName = new HashMap(10);

	/**
	 * Not public to enforce Singleton design pattern.
	 * Would be private except to allow testing via overriding the
	 * loadInputStream() method.
	 * <b>Do not subclass in application code.</b>
	 */
	protected SQLErrorCodesFactory() {
		try {
			String path = SQL_ERROR_CODE_OVERRIDE_PATH;
			Resource resource = loadResource(path);
			if (resource == null || !resource.exists()) {
				path = SQL_ERROR_CODE_DEFAULT_PATH;
				resource = loadResource(path);
				if (resource == null || !resource.exists()) {
					throw new BeanDefinitionStoreException("Unable to locate file [" + SQL_ERROR_CODE_DEFAULT_PATH  + "]");
				}
			}
			ListableBeanFactory bf = new XmlBeanFactory(resource);
			String[] rdbmsNames = bf.getBeanDefinitionNames(SQLErrorCodes.class);
			this.rdbmsErrorCodes = new HashMap(rdbmsNames.length);

			for (int i = 0; i < rdbmsNames.length; i++) {
				SQLErrorCodes ec = (SQLErrorCodes) bf.getBean(rdbmsNames[i]);
				if (ec.getBadSqlGrammarCodes() == null) {
					ec.setBadSqlGrammarCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getBadSqlGrammarCodes());
				}
				if (ec.getDataIntegrityViolationCodes() == null) {
					ec.setDataIntegrityViolationCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getDataIntegrityViolationCodes());
				}
				if (ec.getDatabaseProductName() == null) {
					this.rdbmsErrorCodes.put(rdbmsNames[i], ec);
				}
				else {
					this.rdbmsErrorCodes.put(ec.getDatabaseProductName(), ec);
				}
			}
			logger.info("SQLErrorCodes loaded: " + this.rdbmsErrorCodes.keySet());
		}
		catch (BeanDefinitionStoreException be) {
			logger.warn("Error loading error codes from config file. Message: " + be.getMessage());
			this.rdbmsErrorCodes = new HashMap(0);
		}
	}
	
	/**
	 * Protected for testability. Load the given resource from the class path.
	 * @param path resource path. SQL_ERROR_CODE_DEFAULT_PATH or
	 * SQL_ERROR_CODE_OVERRIDE_PATH.
	 * <b>Not to be overriden by application developers, who should obtain instances
	 * of this class from the static getInstance() method.</b>
	 * @return the input stream or null if the resource wasn't found
	 */
	protected Resource loadResource(String path) {
		return new ClassPathResource(path);
	}

	/**
	 * Return SQLErrorCodes for the given DataSource,
	 * evaluating databaseProductName from DatabaseMetaData,
	 * or an empty error codes instance if no SQLErrorCodes were found.
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName
	 */
	public SQLErrorCodes getErrorCodes(DataSource ds) {
		logger.info("Looking up default SQLErrorCodes for DataSource");
        // Lets avoid looking up database product info if we can.
        Integer dataSourceHash = new Integer(ds.hashCode());
        if (dataSourceProductName.containsKey(dataSourceHash)) {
            String dataSourceDbName = (String)dataSourceProductName.get(dataSourceHash);
            logger.info("Database product name found in cache {" + 
            		dataSourceHash + "}. Name is " + dataSourceDbName);
            return getErrorCodes(dataSourceDbName);
        }
        // We could not find it - got to look it up.
		Connection con = null;
		try {
			con = DataSourceUtils.getConnection(ds);
		}
		catch (DataAccessException ex) {
			// Log failure and leave connection null
			logger.warn("Cannot get connection from database to get metadata when trying to create exception translator", ex);	
		}
		
		if (con != null) {
			// should always be the case outside of test environments
			try {
				DatabaseMetaData dbmd = con.getMetaData();
				if (dbmd != null) {
					String dbName = dbmd.getDatabaseProductName();
					String driverVersion = dbmd.getDriverVersion();
					// special check for DB2
					if (dbName != null && dbName.startsWith("DB2/")) {
						dbName = "DB2";
					}
					if (dbName != null) {
						dataSourceProductName.put(new Integer(ds.hashCode()), dbName);
						logger.info("Database Product Name is " + dbName);
						logger.info("Driver Version is " + driverVersion);
						SQLErrorCodes sec = (SQLErrorCodes) this.rdbmsErrorCodes.get(dbName);
						if (sec != null) {
							return sec;
						}
						logger.info("Error Codes for " + dbName + " not found");
					}
				}
				else {
					logger.warn("Null meta data from connection when trying to create exception translator");
				}
				// could not find the database among the defined ones
			}
			catch (SQLException se) {
				// this is bad - we probably lost the connection
				logger.warn("Could not read database meta data for exception translator", se);
			}
			finally {
				DataSourceUtils.closeConnectionIfNecessary(con, ds);
			}
		}
		
		// fallback is to return an empty ErrorCodes instance
		return new SQLErrorCodes();
	}

	/**
	 * Return SQLErrorCodes instance for the given database. No need for a 
	 * database metadata lookup.
	 */
	public SQLErrorCodes getErrorCodes(String dbName) {
		SQLErrorCodes sec = (SQLErrorCodes) this.rdbmsErrorCodes.get(dbName);
		if (sec == null) {
			// could not find the database among the defined ones
			sec = new SQLErrorCodes();
		}
		return sec;
	}

}
