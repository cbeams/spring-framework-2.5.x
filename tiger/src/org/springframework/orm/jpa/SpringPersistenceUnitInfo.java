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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.ThrowawayClassLoader;
import org.springframework.util.ClassUtils;

/**
 * Implementation of PersistenceUnitInfo interface used to
 * bootstrap an EntityManagerFactory in a container.
 * Largely a JavaBean, with some instrumentation hooks.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setLoadTimeWeaver
 */
class SpringPersistenceUnitInfo implements PersistenceUnitInfo {
	
	private String persistenceUnitName;
	
	private String persistenceProviderClassName;

	private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;

	private DataSource nonJtaDataSource;

	private DataSource jtaDataSource;

	private List<String> mappingFileNames = new LinkedList<String>();
	
	private List<String> managedClassNames = new LinkedList<String>();
	
	private List<URL> jarFileUrls = new LinkedList<URL>(); 

	private LoadTimeWeaver loadTimeWeaver;
	
	// When this is false, will load from Jar
	// TODO: should the default be false ?
	private boolean excludeUnlistedClasses = false;

	private Properties properties = new Properties();

	private URL persistenceUnitRootUrl;


	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public String getPersistenceUnitName() {
		return this.persistenceUnitName;
	}
	
	public void setPersistenceProviderClassName(String persistenceProviderClassName) {
		this.persistenceProviderClassName = persistenceProviderClassName;
	}

	public String getPersistenceProviderClassName() {
		return this.persistenceProviderClassName;
	}
	
	public void setTransactionType(PersistenceUnitTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public PersistenceUnitTransactionType getTransactionType() {
		return transactionType;
	}

	public void setJtaDataSource(DataSource jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	public DataSource getJtaDataSource() {
		return jtaDataSource;
	}

	public void setNonJtaDataSource(DataSource nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public DataSource getNonJtaDataSource() {
		return nonJtaDataSource;
	}

	public void addMappingFileName(String mappingFileName) {
		this.mappingFileNames.add(mappingFileName);
	}

	public List<String> getMappingFileNames() {
		return mappingFileNames;
	}
	
	public void addJarFileUrl(URL jarFileUrl) {
		this.jarFileUrls.add(jarFileUrl);
	}

	public List<URL> getJarFileUrls() {
		return jarFileUrls;
	}
	
	public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
		this.persistenceUnitRootUrl = persistenceUnitRootUrl;
	}

	public URL getPersistenceUnitRootUrl() {
		return persistenceUnitRootUrl;
	}

	public void addManagedClassName(String managedClassName) {
		this.managedClassNames.add(managedClassName);
	}

	public List<String> getManagedClassNames() {
		return managedClassNames;
	}

	public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
		this.excludeUnlistedClasses = excludeUnlistedClasses;
	}

	public boolean excludeUnlistedClasses() {
		return excludeUnlistedClasses;
	}
	
	public void addProperty(String name, String value) {
		if (this.properties == null) {
			this.properties = new Properties();
		}
		this.properties.setProperty(name, value);
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}

	/**
	 * Set the LoadTimeWeaver SPI strategy interface used by Spring
	 * to add instrumentation to the current class loader.
	 */
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	public ClassLoader getClassLoader() {
		if (this.loadTimeWeaver != null) {
			return this.loadTimeWeaver.getInstrumentableClassLoader();
		}
		else {
			return ClassUtils.getDefaultClassLoader();
		}
	}

	/**
	 * Method called by PersistenceProvider to add instrumentation to
	 * the current environment.
	 */
	public void addTransformer(ClassTransformer classTransformer) {
		if (this.loadTimeWeaver == null) {
			throw new IllegalStateException("Cannot apply class transformer without LoadTimeWeaver specified");
		}
		this.loadTimeWeaver.addClassFileTransformer(new ClassFileTransformerAdapter(classTransformer));
	}

	public ClassLoader getNewTempClassLoader() {
		if (this.loadTimeWeaver != null) {
			return this.loadTimeWeaver.getThrowawayClassLoader();
		}
		else {
			return new ThrowawayClassLoader(ClassUtils.getDefaultClassLoader());
		}
	}

}
