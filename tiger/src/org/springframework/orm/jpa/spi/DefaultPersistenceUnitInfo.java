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

package org.springframework.orm.jpa.spi;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * Implementation of PersistenceUnitInfo interface used to
 * bootstrap an EntityManagerFactory in a container.
 * Largely a JavaBean, with some instrumentation hooks.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class DefaultPersistenceUnitInfo implements PersistenceUnitInfo {
	
	private String persistenceUnitName;
	
	private String persistenceProviderClassName;
	
	private List<String> mappingFileNames = new LinkedList<String>();
	
	private List<String> managedClassNames = new LinkedList<String>();
	
	private List<URL> jarFileUrls = new LinkedList<URL>(); 
	
	private LoadTimeWeaver loadTimeWeaver;
	
	private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;

	// When this is false, will load from Jar
	// TODO: should the default be false ?
	private boolean excludeUnlistedClasses = false;

	private Properties properties = new Properties();

	private URL persistenceUnitRootUrl;

	private DataSource nonJtaDataSource;

	private DataSource jtaDataSource;
	
	//-------------------------------------------------------------------------
	// Configuration properties
	//-------------------------------------------------------------------------	
	/**
	 * Set the LoadTimeWeaver SPI strategy interface used by Spring 
	 * to add instrumentation to the current class loader.
	 * @param loadTimeWeaver
	 *            the loadTimeWeaver to set
	 */
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}


	//-------------------------------------------------------------------------
	// Implementation of PersistenceUnitInfo
	//-------------------------------------------------------------------------	
	public String getPersistenceUnitName() {
		return this.persistenceUnitName;
	}
	
	/**
	 * @param persistenceUnitName the persistenceUnitName to set
	 */
	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public String getPersistenceProviderClassName() {
		return this.persistenceProviderClassName;
	}
	
	/**
	 * @param persistenceProviderClassName the persistenceProviderClassName to set
	 */
	public void setPersistenceProviderClassName(String persistenceProviderClassName) {
		this.persistenceProviderClassName = persistenceProviderClassName;
	}

	public PersistenceUnitTransactionType getTransactionType() {
		return transactionType;
	}
	
	/**
	 * @param transactionType the transactionType to set
	 */
	public void setTransactionType(PersistenceUnitTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public DataSource getJtaDataSource() {
		return jtaDataSource;
	}
	
	public void setJtaDataSource(DataSource jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	public DataSource getNonJtaDataSource() {
		return nonJtaDataSource;
	}
	
	public void setNonJtaDataSource(DataSource nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public List<String> getMappingFileNames() {
		return this.mappingFileNames;
	}
	
	public void addMappingFileName(String mappingFileName) {
		this.mappingFileNames.add(mappingFileName);
	}

	public List<URL> getJarFileUrls() {
		return this.jarFileUrls;
	}
	
	public void addJarFileUrl(URL jarFileUrl) {
		this.jarFileUrls.add(jarFileUrl);
	}

	public URL getPersistenceUnitRootUrl() {
		return this.persistenceUnitRootUrl;
	}
	
	/**
	 * @param persistenceUnitRootUrl the persistenceUnitRootUrl to set
	 */
	public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
		this.persistenceUnitRootUrl = persistenceUnitRootUrl;
	}

	public List<String> getManagedClassNames() {
		return this.managedClassNames;
	}
	
	public void addManagedClassName(String managedClassName) {
		managedClassNames.add(managedClassName);
	}

	public boolean excludeUnlistedClasses() {
		return excludeUnlistedClasses;
	}
	
	/**
	 * @param excludeUnlistedClasses the excludeUnlistedClasses to set
	 */
	public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
		this.excludeUnlistedClasses = excludeUnlistedClasses;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public void addProperty(String name, String value) {
		if (properties == null) {
			properties = new Properties();
		}
		properties.setProperty(name, value);
	}
	
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public ClassLoader getClassLoader() {
		return loadTimeWeaver.getInstrumentableClassLoader();
	}

	/**
	 * Method called by PersistenceProvider to add instrumentation to
	 * the current environment
	 */
	public void addTransformer(ClassTransformer classTransformer) {
		loadTimeWeaver.addClassFileTransformer(ClassFileTransformerFactory.createAdapter(classTransformer));
	}

	public ClassLoader getNewTempClassLoader() {
		return loadTimeWeaver.getThrowawayClassLoader();
	}

}
