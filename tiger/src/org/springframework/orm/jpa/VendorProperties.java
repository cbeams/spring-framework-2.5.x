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

package org.springframework.orm.jpa;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.spi.Database;

/**
 * Customization strategy for PersistenceProviders.
 * Assumes knowledge of PersistenceProvider class. Adds typed properties for
 * common purposes such as showing SQL and DDL generation and execution. Also
 * offers an enumeration for database platform.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class VendorProperties {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean showSql;

	private boolean generateDdl;

	private Database database = Database.DEFAULT;

	/**
	 * Populated if there is a single class strategy used for a particular
	 * database platform.
	 */
	protected Class<?> databasePlatformClass;

	private Map<Database, Class<?>> databasesToPlatformClasses = new HashMap<Database, Class<?>>();

	// TODO could add db version (String?) also

	public void setDatabasePlatformClass(Class<?> databasePlatformClass) {
		this.databasePlatformClass = databasePlatformClass;
	}

	public Class<?> getDatabasePlatformClass() {
		return databasePlatformClass;
	}

	public void setDatabase(Database database) {
		this.database = database;
		this.databasePlatformClass = databasesToPlatformClasses.get(database);
		if (this.databasePlatformClass == null) {
			logger.warn("Cannot find database platform class for " + database);
		}
	}
	
	public Database getDatabase() {
		return database;
	}

	/**
	 * Subclasses can call this to register a new database platform class,
	 * assuming they use a single strategy class fotr the database.
	 * 
	 * @param db
	 *            database platform class
	 * @param platformClass
	 *            platform class to handle this database
	 */
	protected void registerDatabasePlatformClass(Database db, Class<?> platformClass) {
		this.databasesToPlatformClasses.put(db, platformClass);
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
		if (showSql) {
			logger.info("Setting SQL display ON");
		}
	}
	
	public boolean getShowSql() {
		return this.showSql;
	}

	public void setGenerateDdl(boolean generateDDL) {
		this.generateDdl = generateDDL;
		if (generateDDL) {
			logger.info("Setting DDL generation ON");
		}
	}
	
	public boolean getGenerateDdl() {
		return this.generateDdl;
	}

	protected String getCreateDdlName(String puName) {
		return puName+ "_create.ddl";
	}

	protected String getDropDdlName(String puName) {
		return puName + "_drop.ddl";
	}

	public abstract void applyBeforeProviderCreation(AbstractEntityManagerFactoryBean fb);
	
	public void applyAfterProviderCreation(AbstractEntityManagerFactoryBean bean) {
		//
	}
	
	public abstract JpaDialect getJpaDialect();

	/**
	 * Execute the given DDL or SQL script
	 * 
	 * @param ddlPath
	 *            Spring resource location of executable script
	 */
	protected void executeDdl(String ddlPath, AbstractEntityManagerFactoryBean fb) {
		List<String> statements = new LinkedList<String>();
		Resource res = fb.getResourceLoader().getResource(ddlPath);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(fb.getDataSource());
		LineNumberReader lnr = null;
		try {
			lnr = new LineNumberReader(new InputStreamReader(res.getInputStream()));
			String lastLine = lnr.readLine();
			while (lastLine != null) {
				statements.add(lastLine);
				lastLine = lnr.readLine();
			}

			for (String statement : statements) {
				logger.info("Executing DDL: " + statement);
				jdbcTemplate.update(statement);
			}
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Failed to read DDL script at resource location '" + ddlPath + "'", ex);
		}
		finally {
			try {
				if (lnr != null)
					lnr.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
	}

}
