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

import javax.persistence.spi.PersistenceProvider;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.springframework.orm.jpa.JpaDialect;

/**
 * OpenJPA-specific JpaVendorAdapter implementation.
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class OpenJpaVendorAdapter extends AbstractJpaVendorAdapter {

	private final OpenJpaDialect jpaDialect = new OpenJpaDialect();
	
	private final PersistenceProvider persistenceProvider = new PersistenceProviderImpl();
	

	/* (non-Javadoc)
	 * @see org.springframework.orm.jpa.JpaVendorAdapter#getJpaDialect()
	 */
	public JpaDialect getJpaDialect() {
		return jpaDialect;
	}


	public PersistenceProvider getPersistenceProvider() {
		return this.persistenceProvider;
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
	public Map getJpaPropertyMap() {
		Properties jpaProperties = new Properties();

		// for db properties seems we have to specify dialects (though they come with special properties). */
		
		if (getDatabasePlatform() != null) {
			jpaProperties.setProperty("openjpa.jdbc.DBDictionary", getDatabasePlatform());
		}
		else if (getDatabase() != null) {
			String databaseDictonary = determineDatabaseDictionary(getDatabase());
			if (databaseDictonary != null) {
				jpaProperties.setProperty("openjpa.jdbc.DBDictionary", databaseDictonary);
			}
		}

		if (isGenerateDdl()) {
			jpaProperties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
		}
		
		// taken from the docs - (under the name "Standard OpenJPA Log Configuration + All SQL Statements").
		if (isShowSql()) {
			jpaProperties.setProperty("openjpa.Log", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");
		}
		

		return jpaProperties;
	}

	
	protected String determineDatabaseDictionary(Database database) {
		switch (database) {
			case DB2: return "db2";
			case HSQL: return "hsql(SimulateLocking=true)";
			case INFORMIX: return "informix";
			case MYSQL: return "mysql";
			case ORACLE: return "oracle";
			case POSTGRESQL: return "postgres";
			case SQL_SERVER: return "sqlserver";
			case SYBASE: return "sybase";
			default: return null;
		}
	}
	

}
