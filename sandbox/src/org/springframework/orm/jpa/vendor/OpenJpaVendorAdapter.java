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

import java.util.Map;
import java.util.Properties;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;

/**
 * OpenJPA-specific JpaVendorAdapter implementation.
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class OpenJpaVendorAdapter extends AbstractJpaVendorAdapter {

	private final OpenJpaDialect jpaDialect = new OpenJpaDialect();
	

	/* (non-Javadoc)
	 * @see org.springframework.orm.jpa.JpaVendorAdapter#getJpaDialect()
	 */
	public JpaDialect getJpaDialect() {
		return jpaDialect;
	}

	/* (non-Javadoc)
	 * @see org.springframework.orm.jpa.JpaVendorAdapter#getPersistenceProviderClass()
	 */
	public Class getPersistenceProviderClass() {
		return PersistenceProviderImpl.class;
	}

	/* (non-Javadoc)
	 * @see org.springframework.orm.jpa.JpaVendorAdapter#getEntityManagerInterface()
	 */
	public Class getEntityManagerInterface() {
		return OpenJPAEntityManager.class;
	}

	/* (non-Javadoc)
	 * @see org.springframework.orm.jpa.JpaVendorAdapter#getJpaPropertyMap()
	 */
	// TODO: find the openjpa/kodo properties to plug in.
	public Map getJpaPropertyMap() {
		Properties jpaProperties = new Properties();

		// for db properties seems we have to specify dialects (though they come with special properties).
		/*
		if (getDatabasePlatform() != null) {
			jpaProperties.setProperty(XXX, getDatabasePlatform());
		}
		else if (getDatabase() != null) {
			Class databaseDialectClass = determineDatabaseDialectClass(getDatabase());
			if (databaseDialectClass != null) {
				jpaProperties.setProperty(XXX, databaseDialectClass.getName());
			}
		}
		*/

		if (isGenerateDdl()) {
			jpaProperties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
		}
		// we can configure this as logging but then it might rewrite some other openjpa logging property. 
		
		// taken from the docs - (under the name "Standard OpenJPA Log Configuration + All SQL Statements").
		if (isShowSql()) {
			jpaProperties.setProperty("openjpa.Log", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");
		}
		

		return jpaProperties;
	}

	/*
	protected Class determineDatabaseDialectClass(Database database) {
		switch (database) {
			case DB2: return DB2Dialect.class;
			case HSQL: return HSQLDialect.class;
			case INFORMIX: return InformixDialect.class;
			case MYSQL: return MySQLDialect.class;
			case ORACLE: return Oracle9Dialect.class;
			case POSTGRESQL: return PostgreSQLDialect.class;
			case SQL_SERVER: return SQLServerDialect.class;
			case SYBASE: return SybaseDialect.class;
			default: return null;
		}
	}
	*/

}
