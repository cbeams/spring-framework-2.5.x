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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.jdbc.datasource.lookup.MapDataSourceLookup;
import org.springframework.util.ClassUtils;

/**
 * Powerful FactoryBean that creates a fully Spring-configured EntityManagerFactory
 * according to the container contract for JPA bootstrapping
 *
 * <p>As with LocalEntityManagerFactoryBean, configuration settings are usually read in
 * from a <code>META-INF/persistence.xml</code> config file, residing in the class path,
 * according to the general JPA configuration contract. However, this FactoryBean is
 * more flexible in that you can override the location of the <code>persistence.xml</code>
 * file, specify the JDBC DataSources to link to, etc. Furthermore, it allows for
 * pluggable class instrumentation through the Spring LoadTimeWeaver abstraction,
 * instead of being tied to a special VM agent specified on JVM startup.
 *
 * <p>Internally, this FactoryBean parses the <code>persistence.xml</code> file itself
 * and creates a corresponding PersistenceUnitInfo object (with further configuration
 * merged in, such as JDBC DataSources and the LoadTimeWeaver), to be passed to the
 * JPA PersistenceProvider. This corresponds to full-fledged local JPA container.
 *
 * <p>The exposed EntityManagerFactory object will implement all the interfaces of
 * the underlying native EntityManagerFactory returned by the PersistenceProvider,
 * plus the EntityManagerFactoryInfo interface which exposes additional metadata.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setPersistenceXmlLocation
 * @see #setJpaProperties
 * @see #setJpaVendorAdapter
 * @see #setLoadTimeWeaver
 * @see #setDataSource
 * @see EntityManagerFactoryInfo
 * @see LocalEntityManagerFactoryBean
 * @see javax.persistence.spi.PersistenceProvider#createContainerEntityManagerFactory
 */
public class LocalContainerEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean
		implements ResourceLoaderAware {

	/**
	 * Default location of the <code>persistence.xml</code> file:
	 * "classpath*:META-INF/persistence.xml".
	 */
	public final static String DEFAULT_PERSISTENCE_XML_LOCATION = "classpath*:META-INF/persistence.xml";
	
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

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private SpringPersistenceUnitInfo persistenceUnitInfo;

	
	/**
	 * Set the location of the <code>persistence.xml</code> file
	 * we want to use. This is a Spring resource location.
	 * <p>Default is "classpath:META-INF/persistence.xml".
	 * @param persistenceXmlLocation a Spring resource String
	 * identifying the location of the <code>persistence.xml</code> file
	 * that this LocalContainerEntityManagerFactoryBean should parse
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

	/**
	 * Specify the Spring LoadTimeWeaver to use for class instrumentation according
	 * to the JPA class transformer contract.
	 * <p>It is a not required to specify a LoadTimeWeaver: Most providers will be
	 * able to provide a subset of their functionality without class instrumentation
	 * as well, or operate with their VM agent specified on JVM startup.
	 * <p>In terms of Spring-provided weaving options, the most important ones are
	 * InstrumentationLoadTimeWeaver, which requires a Spring-specific (but very general)
	 * VM agent specified on JVM startup, and ReflectiveLoadTimeWeaver, which interacts
	 * with an underlying ClassLoader based on specific extended methods being available
	 * on it (for example, interacting with Spring's TomcatInstrumentableClassLoader).
	 * @see org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver
	 * @see org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver
	 * @see org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader
	 */
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	/**
	 * Specify the JDBC DataSource that the JPA persistence provider is supposed
	 * to use for accessing the database. This is an alternative to keeping the
	 * JDBC configuration in <code>persistence.xml</code>, passing in a Spring-managed
	 * DataSource instead.
	 * <p>In JPA speak, a DataSource passed in here will be uses as "nonJtaDataSource"
	 * on the PersistenceUnitInfo passed to the PersistenceProvider, overriding
	 * data source configuration in <code>persistence.xml</code> (if any).
	 * @see javax.persistence.spi.PersistenceUnitInfo#getNonJtaDataSource()
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Specify the JDBC DataSources that the JPA persistence provider is supposed
	 * to use for accessing the database, resolving data source names in
	 * <code>persistence.xml</code> against Spring-managed DataSources.
	 * <p>The specified Map needs to define data source names for specific DataSource
	 * objects, matching the data source names used in <code>persistence.xml</code>.
	 * If not specified, data source names will be resolved as JNDI names instead
	 * (as defined by standard JPA).
	 * @see org.springframework.jdbc.datasource.lookup.MapDataSourceLookup
	 */
	public void setDataSources(Map<String, DataSource> dataSources) {
		this.dataSourceLookup = new MapDataSourceLookup(dataSources);
	}

	/**
	 * Specify the JDBC DataSources that the JPA persistence provider is supposed
	 * to use for accessing the database, resolving data source names in
	 * <code>persistence.xml</code> against Spring-managed DataSources.
	 * <p>Default is JndiDataSourceLookup, which resolves data source names as
	 * JNDI names (as defined by standard JPA).
	 * @see org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup
	 */
	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = dataSourceLookup;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = (resourceLoader != null ?
				ResourcePatternUtils.getResourcePatternResolver(resourceLoader) :
				new PathMatchingResourcePatternResolver());
	}


	@Override
	protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
		this.persistenceUnitInfo = determinePersistenceUnitInfo();

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

		this.persistenceUnitInfo.setLoadTimeWeaver(this.loadTimeWeaver);

		if (this.persistenceUnitInfo.getNonJtaDataSource() == null) {
			this.persistenceUnitInfo.setNonJtaDataSource(this.dataSource);
		}
		if (this.persistenceUnitInfo.getPersistenceUnitRootUrl() == null) {
			this.persistenceUnitInfo.setPersistenceUnitRootUrl(determinePersistenceUnitRootUrl());
		}

		postProcessPersistenceUnitInfo(this.persistenceUnitInfo);

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
	 * Determine the PersistenceUnitInfo to use for the EntityManagerFactory
	 * created by this bean.
	 * <p>The default implementation reads in all persistence unit infos from
	 * <code>persistence.xml</code>, as defined in the JPA specification.
	 * If no entity manager name was specified, it takes the first info in the
	 * array as returned by the reader. Otherwise, it checks for a matching name.
	 * @return the chosen PersistenceUnitInfo
	 * @see #readPersistenceUnitInfos()
	 */
	protected SpringPersistenceUnitInfo determinePersistenceUnitInfo() {
		SpringPersistenceUnitInfo[] infos = readPersistenceUnitInfos();
		if (infos.length == 0) {
			throw new IllegalArgumentException(
					"No persistence units parsed from [" + this.persistenceXmlLocation + "]");
		}

		SpringPersistenceUnitInfo pui = null;
		if (getPersistenceUnitName() == null) {
			// Default to the first unit.
			pui = infos[0];
			if (logger.isDebugEnabled()) {
				logger.debug("No persistence unit name specified; choosing the first one " + pui.getPersistenceUnitName());
			}
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
	 * Read all persistence unit infos from <code>persistence.xml</code>,
	 * as defined in the JPA specification.
	 */
	protected SpringPersistenceUnitInfo[] readPersistenceUnitInfos() {
		PersistenceUnitReader reader = new PersistenceUnitReader(this.resourcePatternResolver, this.dataSourceLookup);
		return reader.readPersistenceUnitInfos(this.persistenceXmlLocation);
	}

	/**
	 * Try to determine the persistence unit root URL based on the given
	 * "persistenceUnitRootLocation".
	 * @return the persistence unit root URL to pass to the JPA PersistenceProvider
	 * @see #setPersistenceUnitRootLocation
	 */
	private URL determinePersistenceUnitRootUrl() {
		try {
			Resource res = this.resourcePatternResolver.getResource(this.persistenceUnitRootLocation);
			if (logger.isInfoEnabled()) {
				logger.info("Using persistence unit root location: " + res);
			}
			return res.getURL();
		}
		catch (IOException ex) {
			throw new PersistenceException("Unable to resolve persistence unit root URL", ex);
		}
	}


	/**
	 * Hook method allowing subclasses to customize the PersistenceUnitInfo.
	 * <p>The default implementation is empty; hence, the PersistenceUnitInfo
	 * will be used as read from <code>persistence.xml</code>. This method
	 * can be overridden to register further entity classes, jar files, etc.
	 * @param pui the chosen PersistenceUnitInfo, as read from <code>persistence.xml</code>.
	 * Passed in as mutable SpringPersistenceUnitInfo.
	 */
	protected void postProcessPersistenceUnitInfo(SpringPersistenceUnitInfo pui) {
	}

	/**
	 * Hook method allowing subclasses to customize the EntityManagerFactory
	 * after its creation via the PersistenceProvider.
	 * <p>The default implementation is empty.
	 * @param emf the newly created EntityManagerFactory we are working with
	 * @param pui the PersistenceUnitInfo used to configure the EntityManagerFactory
	 * @see javax.persistence.spi.PersistenceProvider#createContainerEntityManagerFactory
	 */
	protected void postProcessEntityManagerFactory(EntityManagerFactory emf, PersistenceUnitInfo pui) {
	}


	public DataSource getDataSource() {
		return this.dataSource;
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

}
