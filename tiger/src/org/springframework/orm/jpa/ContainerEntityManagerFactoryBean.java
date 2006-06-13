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
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.jdbc.datasource.lookup.MapDataSourceLookup;
import org.springframework.util.ClassUtils;

/**
 * Generic fully Spring-configured EntityManagerFactory FactoryBean for use with
 * the container contract for JPA bootstrapping, parsing XML files and creating
 * a PersistenceUnitInfo.
 *
 * <p>Created EntityManagerFactory object implements all the interfaces
 * of the underlying native EntityManagerFactory returned by the
 * PersistenceProvider, plus the EntityManagerFactoryInfo interface,
 * which exposes additional information.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see EntityManagerFactoryInfo
 */
public class ContainerEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean
		implements ResourceLoaderAware {

	/**
	 * Default location of the <code>persistence.xml</code> file:
	 * "classpath:META-INF/persistence.xml".
	 */
	public final static String DEFAULT_PERSISTENCE_XML_LOCATION = "classpath:META-INF/persistence.xml";
	
	/**
	 * Default location of the <code>persistence.xml</code> file:
	 * "classpath:", indicating the root of the class path.
	 */
	public final static String DEFAULT_PERSISTENCE_UNIT_ROOT_LOCATION = "classpath:";

	/** Set of deployed EntityManagerFactory names */
	private static Set<String> entityManagerFactoryNamesDeployed = new HashSet<String>();


	/** Location of persistence.xml file */
	private String persistenceXmlLocation = DEFAULT_PERSISTENCE_XML_LOCATION;

	private String persistenceUnitRootLocation = DEFAULT_PERSISTENCE_UNIT_ROOT_LOCATION;

	private boolean allowRedeploymentWithSameName = false;

	private LoadTimeWeaver loadTimeWeaver;

	private DataSource dataSource;

	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private SpringPersistenceUnitInfo persistenceUnitInfo;

	
	/**
	 * Set the location of the <code>persistence.xml</code> file
	 * we want to use. This is a Spring resource location.
	 * <p>Default is "classpath:META-INF/persistence.xml".
	 * @param persistenceXmlLocation a Spring resource String
	 * identifying the location of the <code>persistence.xml</code> file
	 * that this ContainerEntityManagerFactoryBean should parse
	 */
	public void setPersistenceXmlLocation(String persistenceXmlLocation) {
		this.persistenceXmlLocation = persistenceXmlLocation;
	}

	/**
	 * Optional property setting the location of the persistence unit root URL,
	 * as a Spring resource location string. The value may need to be platform-specific
	 * (for example, a file path) and may differ between a test and deployed environment.
	 * <p>It may be necessary to set this property if the persistence provider needs to
	 * locate the <code>orm.xml</code> file. With a pure annotation approach, this
	 * should not be required. If this property is not specified, it will automatically
	 * be set to the root of the class path as returned by the Spring resource loading
	 * infrastructure. This defaulting may not work reliably if there are multiple
	 * directories or JARs on the class path.
	 */
	public void setPersistenceUnitRootLocation(String persistenceUnitRootPath) {
		this.persistenceUnitRootLocation = persistenceUnitRootPath;
	}

	/**
	 * Set whether redeployment of an EntityManagerFactory with the same name
	 * in the same class loader is legal. The default is for it NOT to be legal.
	 */
	public void setAllowRedeploymentWithSameName(boolean allowRedeploymentWithSameName) {
		this.allowRedeploymentWithSameName = allowRedeploymentWithSameName;
	}

	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDataSources(Map<String, DataSource> dataSources) {
		this.dataSourceLookup = new MapDataSourceLookup(dataSources);
	}

	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = dataSourceLookup;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}


	@Override
	protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
		this.persistenceUnitInfo = parsePersistenceUnitInfo();
		String unitName = this.persistenceUnitInfo.getPersistenceUnitName();

		if (!this.allowRedeploymentWithSameName) {
			if (entityManagerFactoryNamesDeployed.contains(unitName)) {
				throw new IllegalStateException("EntityManagerFactory with name '" + unitName + "' " +
						"has already been deployed in this class loader; cannot deploy another");
			}
			else {
				if (logger.isInfoEnabled()) {
					logger.info("Allowing redeployment of EntityManagerFactory with name '" + unitName + "' ");
				}
			}
		}
		entityManagerFactoryNamesDeployed.add(unitName);

		if (this.persistenceUnitInfo.getNonJtaDataSource() == null) {
			this.persistenceUnitInfo.setNonJtaDataSource(this.dataSource);
		}
		this.persistenceUnitInfo.setLoadTimeWeaver(this.loadTimeWeaver);

		this.persistenceUnitInfo.setPersistenceUnitRootUrl(findPersistenceUnitRootUrl());

		Class persistenceProviderClass = getPersistenceProviderClass();
		String puiProviderClassName = this.persistenceUnitInfo.getPersistenceProviderClassName();
		if (persistenceProviderClass == null && puiProviderClassName != null) {
			try {
				persistenceProviderClass = ClassUtils.forName(puiProviderClassName);
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Cannot resolve provider class name '" + puiProviderClassName + "'", ex);
			}
		}
		
		if (persistenceProviderClass == null) {
			throw new IllegalStateException("Unable to determine persistence provider class. " +
					"Please check configuration of " + getClass().getName() + "; " +
					"ideally specify the appropriate JpaVendorAdapter class for this provider");
		}
		PersistenceProvider pp = (PersistenceProvider) BeanUtils.instantiateClass(persistenceProviderClass);
		this.nativeEntityManagerFactory =
				pp.createContainerEntityManagerFactory(this.persistenceUnitInfo, getJpaPropertyMap());
		postProcessEntityManagerFactory(this.nativeEntityManagerFactory, this.persistenceUnitInfo);

		return this.nativeEntityManagerFactory;
	}

	/**
	 * If no entity manager name was found, take first in the parsed array of
	 * ContainerPersistenceUnitInfo. Otherwise check for a matching name.
	 * @return Spring-specific PersistenceUnitInfo
	 */
	private SpringPersistenceUnitInfo parsePersistenceUnitInfo() {
		PersistenceUnitReader reader = new PersistenceUnitReader(this.resourceLoader, this.dataSourceLookup);

		SpringPersistenceUnitInfo[] infos = reader.readPersistenceUnitInfos(this.persistenceXmlLocation);
		if (infos.length == 0) {
			throw new IllegalArgumentException(
					"No persistence units parsed from [" + this.persistenceXmlLocation + "]");
		}

		SpringPersistenceUnitInfo pui = null;
		if (getPersistenceUnitName() == null) {
			// Default to the first unit.
			pui = infos[0];
		}
		else {
			// Find a unit with matching name.
			for (SpringPersistenceUnitInfo candidate : infos) {
				if (getPersistenceUnitName().equals(candidate.getPersistenceUnitName())) {
					pui = candidate;
					break;
				}
			}
		}

		if (pui == null) {
			throw new IllegalArgumentException(
					"No persistence info with name matching '" + getPersistenceUnitName() + "' found");
		}

		return pui;
	}

	/**
	 * Try to deduce the persistence unit root URL using multiple strategies.
	 * @return the persistence unit root URL to pass to the JPA PersistenceProvider
	 */
	private URL findPersistenceUnitRootUrl() {
		try {
			Resource res = this.resourceLoader.getResource(this.persistenceUnitRootLocation);
			if (logger.isInfoEnabled()) {
				logger.info("Using explicit persistence unit root location: " + res);
			}
			return res.getURL();
		}
		catch (IOException ex) {
			throw new PersistenceException("Unable to resolve persistence unit root URL", ex);
		}
	}


	public PersistenceUnitInfo getPersistenceUnitInfo() {
		return this.persistenceUnitInfo;
	}

	public String getPersistenceUnitName() {
		if (this.persistenceUnitInfo != null) {
			return this.persistenceUnitInfo.getPersistenceUnitName();
		}
		return super.getPersistenceUnitName();
	}


	/**
	 * Hook method allowing subclasses to customize the EntityManagerFactory.
	 * <p>The default implementation is empty.
	 * @param emf EntityManagerFactory we are working with
	 * @param pui PersistenceUnitInfo used to configure the EntityManagerFactory
	 */
	protected void postProcessEntityManagerFactory(EntityManagerFactory emf, PersistenceUnitInfo pui) {
	}

}
