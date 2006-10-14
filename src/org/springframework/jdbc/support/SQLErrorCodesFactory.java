/*
 * Copyright 2002-2006 the original author or authors.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Factory for creating {@link SQLErrorCodes} based on the
 * "databaseProductName" taken from the {@link java.sql.DatabaseMetaData}.
 *
 * <p>Returns <code>SQLErrorCodes</code> populated with vendor codes
 * defined in a configuration file named "sql-error-codes.xml".
 * Reads the default file in this package if not overridden by a file in
 * the root of the class path (for example in the "/WEB-INF/classes" directory).
 *
 * @author Thomas Risberg
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.sql.DatabaseMetaData#getDatabaseProductName
 */
public class SQLErrorCodesFactory {

	/**
	 * The name of custom SQL error codes file, loading from the root
	 * of the class path (e.g. in the "WEB-INF/classes" directory).
	 */
	public static final String SQL_ERROR_CODE_OVERRIDE_PATH = "sql-error-codes.xml";

	/**
	 * The name of default SQL error code files, loading from the class path.
	 */
	public static final String SQL_ERROR_CODE_DEFAULT_PATH = "org/springframework/jdbc/support/sql-error-codes.xml";


	private static final Log logger = LogFactory.getLog(SQLErrorCodesFactory.class);

	/**
	 * Keep track of a single instance so we can return it to classes that request it.
	 */
	private static final SQLErrorCodesFactory instance = new SQLErrorCodesFactory();


	/**
	 * Return the singleton instance.
	 */
	public static SQLErrorCodesFactory getInstance() {
		return instance;
	}


	/**
	 * Map to hold error codes for all databases defined in the config file.
	 * Key is the database product name, value is the SQLErrorCodes instance.
	 */
	private final Map errorCodesMap;

	/**
	 * Map to hold database product name retrieved from database metadata.
	 * Key is the DataSource, value is the database product name.
	 */
	private final Map dataSourceProductNames = new HashMap(10);


	/**
	 * Create a new instance of the {@link SQLErrorCodesFactory} class.
	 * <p>Not public to enforce Singleton design pattern. Would be private
	 * except to allow testing via overriding the
	 * {@link #loadResource(String)} method.
	 * <p><b>Do not subclass in application code.</b>
	 * @see #loadResource(String)
	 */
	protected SQLErrorCodesFactory() {
		Map errorCodes = null;

		try {
			DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
			XmlBeanDefinitionReader bdr = new XmlBeanDefinitionReader(lbf);

			// Load default SQL error codes.
			Resource resource = loadResource(SQL_ERROR_CODE_DEFAULT_PATH);
			if (resource != null && resource.exists()) {
				bdr.loadBeanDefinitions(resource);
			}
			else {
				logger.warn("Default sql-error-codes.xml not found (should be included in spring.jar)");
			}

			// Load custom SQL error codes, overriding defaults.
			resource = loadResource(SQL_ERROR_CODE_OVERRIDE_PATH);
			if (resource != null && resource.exists()) {
				bdr.loadBeanDefinitions(resource);
				logger.info("Found custom sql-error-codes.xml file at the root of the classpath");
			}

			// Check all beans of type SQLErrorCodes.
			Map errorCodeBeans = lbf.getBeansOfType(SQLErrorCodes.class, true, false);
			if (logger.isInfoEnabled()) {
				logger.info("SQLErrorCodes loaded: " + errorCodeBeans.keySet());
			}
			errorCodes = new HashMap(errorCodeBeans.size());

			for (Iterator it = errorCodeBeans.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String beanName = (String) entry.getKey();
				SQLErrorCodes ec = (SQLErrorCodes) entry.getValue();

				// If explicit database product names specified, expose error codes for those names.
				String[] names = ec.getDatabaseProductNames();
				if (names != null) {
					for (int i = 0; i < names.length; i++) {
						errorCodes.put(names[i], ec);
					}
				}
				else {
					errorCodes.put(beanName, ec);
				}
			}
		}
		catch (BeansException ex) {
			logger.warn("Error loading SQL error codes from config file", ex);
			errorCodes = Collections.EMPTY_MAP;
		}

		this.errorCodesMap = errorCodes;
	}
	
	/**
	 * Load the given resource from the class path.
	 * <p><b>Not to be overridden by application developers, who should obtain
	 * instances of this class from the static {@link #getInstance()} method.</b>
	 * <p>Protected for testability.
	 * @param path resource path; either a custom path or one of either
	 * {@link #SQL_ERROR_CODE_DEFAULT_PATH} or
	 * {@link #SQL_ERROR_CODE_OVERRIDE_PATH}.
	 * @return the resource, or <code>null</code> if the resource wasn't found
	 * @see #getInstance
	 */
	protected Resource loadResource(String path) {
		return new ClassPathResource(path);
	}


	/**
	 * Return the {@link SQLErrorCodes} instance for the given database.
	 * <p>No need for a database metadata lookup.
	 * @param dbName the database name (must not be <code>null</code> )
	 * @return the <code>SQLErrorCodes</code> instance for the given database
	 * @throws IllegalArgumentException if the supplied database name is <code>null</code>
	 */
	public SQLErrorCodes getErrorCodes(String dbName) {
		Assert.notNull(dbName, "Database product name must not be null");

		SQLErrorCodes sec = (SQLErrorCodes) this.errorCodesMap.get(dbName);
		if (sec != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL error codes for '" + dbName + "' found");
			}
			return sec;
		}

		// Could not find the database among the defined ones.
		if (logger.isDebugEnabled()) {
			logger.debug("SQL error codes for '" + dbName + "' not found");
		}
		return new SQLErrorCodes();
	}

	/**
	 * Return {@link SQLErrorCodes} for the given {@link DataSource},
	 * evaluating "databaseProductName" from the
	 * {@link java.sql.DatabaseMetaData}, or an empty error codes
	 * instance if no <code>SQLErrorCodes</code> were found.
	 * @param dataSource the <code>DataSource</code> identifying the database
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName
	 */
	public SQLErrorCodes getErrorCodes(DataSource dataSource) {
		Assert.notNull(dataSource, "DataSource must not be null");
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up default SQLErrorCodes for DataSource [" + dataSource + "]");
		}

		synchronized (this.dataSourceProductNames) {
		// Let's avoid looking up database product info if we can.
			String dataSourceDbName = (String) this.dataSourceProductNames.get(dataSource);
			if (dataSourceDbName != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Database product name found in cache for DataSource [" + dataSource +
							"]: name is '" + dataSourceDbName + "'");
				}
				return getErrorCodes(dataSourceDbName);
			}

			// We could not find it - got to look it up.
			try {
				String dbName = (String)
						JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName");

				if (dbName != null) {
					// Special check for DB2 -- !!! DEPRECATED AS OF Spring 1.1 !!!
					// !!! This will be removed in a future version !!!
					// We have added wildcard support so you should add a
					// <property name="databaseProductName"><value>DB2*</value></property>
					// entry to your custom sql-error-cdes.xml instead.
					if (dbName.startsWith("DB2")) {
						dbName = "DB2";
					}

					// Special check for wild card match:W we can match on a database name like
					// 'DB2*' meaning the database name starts with 'DB2', or '*DB2' for ends
					// with 'DB2', or even '*DB2*' for contains 'DB2'.
					Iterator dbNameIter = this.errorCodesMap.keySet().iterator();
					while (dbNameIter.hasNext()) {
						String checkDbName = (String) dbNameIter.next();
						if (checkDbName != null && (checkDbName.startsWith("*") || checkDbName.endsWith("*"))) {
							if (checkDbName.startsWith("*") && checkDbName.endsWith("*")) {
								if (dbName.indexOf(checkDbName.substring(1, checkDbName.length() - 1)) >= 0) {
									dbName = checkDbName;
								}
							}
							else if (checkDbName.startsWith("*")) {
								if (dbName.endsWith(checkDbName.substring(1, checkDbName.length()))) {
									dbName = checkDbName;
								}
							}
							else if (checkDbName.endsWith("*")) {
								if (dbName.startsWith(checkDbName.substring(0, checkDbName.length() - 1))) {
									dbName = checkDbName;
								}
							}
						}
					}

					this.dataSourceProductNames.put(dataSource, dbName);
					if (logger.isDebugEnabled()) {
						logger.debug("Database product name cached for DataSource [" + dataSource +
								"]: name is '" + dbName + "'");
					}
					return getErrorCodes(dbName);
				}
			}
			catch (MetaDataAccessException ex) {
				logger.warn("Error while extracting database product name - falling back to empty error codes", ex);
			}
		}

		// Fallback is to return an empty ErrorCodes instance.
		return new SQLErrorCodes();
	}

}
