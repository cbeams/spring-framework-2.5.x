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

package org.springframework.orm.jpa.spi.toplink;

import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;
import oracle.toplink.essentials.platform.database.DB2Platform;
import oracle.toplink.essentials.platform.database.HSQLPlatform;
import oracle.toplink.essentials.platform.database.InformixPlatform;
import oracle.toplink.essentials.platform.database.MySQL4Platform;
import oracle.toplink.essentials.platform.database.PostgreSQLPlatform;
import oracle.toplink.essentials.platform.database.SQLServerPlatform;
import oracle.toplink.essentials.platform.database.SybasePlatform;
import oracle.toplink.essentials.platform.database.oracle.OraclePlatform;

import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.VendorProperties;
import org.springframework.orm.jpa.spi.Database;

/**
 * TopLink implementation of vendor-specific properties
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class TopLinkVendorProperties extends VendorProperties {
	
	private TopLinkJpaDialect topLinkJpaDialect = new TopLinkJpaDialect();

	public TopLinkVendorProperties() {
		// Register platform classes
		registerDatabasePlatformClass(Database.DB2, DB2Platform.class);
		registerDatabasePlatformClass(Database.HSQL, HSQLPlatform.class);
		registerDatabasePlatformClass(Database.INFORMIX, InformixPlatform.class);
		registerDatabasePlatformClass(Database.MYSQL, MySQL4Platform.class);
		registerDatabasePlatformClass(Database.ORACLE, OraclePlatform.class);
		registerDatabasePlatformClass(Database.POSTGRESQL, PostgreSQLPlatform.class);
		registerDatabasePlatformClass(Database.SQL_SERVER, SQLServerPlatform.class);
		registerDatabasePlatformClass(Database.SYBASE, SybasePlatform.class);
	}
	
	protected String getPersistenceUnitName(AbstractEntityManagerFactoryBean fb) {
		// TODO fix this, should look like this
		//String persistenceUnitName = fb.getPersistenceUnitName();
	
		String persistenceUnitName = "" + System.identityHashCode(fb);
		return persistenceUnitName;
	}
	
	
	@Override
	public void applyBeforeProviderCreation(AbstractEntityManagerFactoryBean fb) {
		fb.setPersistenceProviderClass(EntityManagerFactoryProvider.class);
		
		if (getGenerateDdl()) {			
			fb.addJpaProperty(EntityManagerFactoryProvider.DDL_GENERATION, EntityManagerFactoryProvider.DROP_AND_CREATE);
			
			fb.addJpaProperty(EntityManagerFactoryProvider.CREATE_JDBC_DDL_FILE, 
					getCreateDdlName(getPersistenceUnitName(fb)));
			fb.addJpaProperty(EntityManagerFactoryProvider.DROP_JDBC_DDL_FILE, 
					getDropDdlName(getPersistenceUnitName(fb)));
		}
		if (getDatabasePlatformClass() != null) {
			fb.addJpaProperty(EntityManagerFactoryProvider.TOPLINK_PLATFORM_PROPERTY, 
					getDatabasePlatformClass().getName());
		}
		
		if (getShowSql()) {
			fb.addJpaProperty(EntityManagerFactoryProvider.TOPLINK_LOGGING_LEVEL, "FINE");
		}
	}
	
	@Override
	public void applyAfterProviderCreation(AbstractEntityManagerFactoryBean fb) {
		if (getGenerateDdl()) {
			try {
				executeDdl("file:./" + getDropDdlName(getPersistenceUnitName(fb)), fb);
			}
			catch (Exception ex) {
				logger.warn("Error dropping schema: " + ex);
			}
			executeDdl("file:./" + 
					getCreateDdlName(getPersistenceUnitName(fb)),					
					fb);
		}
	}
	
	
	@Override
	public TopLinkJpaDialect getJpaDialect() {
		return topLinkJpaDialect ;
	}
}
