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

package org.springframework.orm.jpa.vendor;

import javax.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.JpaVendorAdapter;

/**
 * Abstract JpaVendorAdapter implementation that defines common properties,
 * to be translated into vendor-specific JPA properties by subclasses.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractJpaVendorAdapter implements JpaVendorAdapter {

	private Database database = Database.DEFAULT;

	private boolean showSql;

	private boolean generateDdl;
	
	private Class<?> databasePlatformClass;


	/**
	 * Specify the target database to operate on, as a value
	 * of the <code>Database</code> enum:
	 * DB2, HSQL, INFORMIX, MYSQL, ORACLE, POSTGRESQL, SQL_SERVER, SYBASE
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}
	
	/**
	 * Set the database platform class. Use this mechanism as a fallback
	 * if the values of the enum passed to the database property are
	 * insufficient
	 * @param databasePlatformClass the databasePlatformClass to set
	 */
	public void setDatabasePlatformClass(Class<?> databasePlatformClass) {
		this.databasePlatformClass = databasePlatformClass;
	}
	
	/**
	 * @return the databasePlatformClass
	 */
	protected Class<?> getDatabasePlatformClass() {
		return this.databasePlatformClass;
	}

	/**
	 * Return the target database to operate on.
	 */
	protected Database getDatabase() {
		return database;
	}

	/**
	 * Set whether to show SQL in the log (or in the console).
	 */
	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	/**
	 * Return whether to show SQL in the log (or in the console).
	 */
	protected boolean isShowSql() {
		return showSql;
	}

	/**
	 * Set whether to generate DDL after the EntityManagerFactory
	 * has been initialized.
	 */
	public void setGenerateDdl(boolean generateDdl) {
		this.generateDdl = generateDdl;
	}

	/**
	 * Return whether to generate DDL after the EntityManagerFactory
	 * has been initialized.
	 */
	protected boolean isGenerateDdl() {
		return generateDdl;
	}


	/**
	 * Post-process the EntityManagerFactory after it has been initialized.
	 * @param emf the EntityManagerFactory to process
	 */
	public void postProcessEntityManagerFactory(EntityManagerFactory emf) {
	}

}
