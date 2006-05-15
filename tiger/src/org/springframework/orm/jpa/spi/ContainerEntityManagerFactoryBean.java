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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.util.ClassUtils;

/**
 * Generic fully Spring-configured EntityManagerFactory FactoryBean for use with the
 * container contract for JPA bootstrapping, parsing XML files and
 * creating a PersistenceUnitInfo.
 * <p>Created EntityManagerFactory object implements all the interfaces
 * of the underlying native EntityManagerFactory returned by the
 * PersistenceProvider, plus the EntityManagerFactoryInfo interface,
 * which exposes additional information.
 * 
 * @author Rod Johnson
 * @since 2.0
 * @see EntityManagerFactoryInfo
 */
public class ContainerEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean {

	private final static String DEFAULT_PERSISTENCE_XML_LOCATION =
							"/META-INF/persistence.xml";
	
	/**
	 * Set of deployed EntityManagerFactory names. 
	 * This being static is intentional. It is illegal according
	 * to the 
	 */
	private static Set<String> entityManagerFactoryNamesDeployed = new HashSet<String>();
	
	private boolean allowRedeploymentWithSameName = false;

	/**
	 * Location of persistence.xml file
	 */
	private Resource location; 

	private LoadTimeWeaver loadTimeWeaver; 

	// Initialize to allow use without an ApplicationContext,
	// which will provide a container ResourceLoader
	protected ResourceLoader resourceLoader = new DefaultResourceLoader();

	private PersistenceUnitReader persistenceUnitReader = new DomPersistenceUnitReader();

	private DefaultPersistenceUnitInfo persistenceUnitInfo;

	
	//-------------------------------------------------------------------------
	// JavaBean properties
	//-------------------------------------------------------------------------	
	/**
	 * Set whether redeployment of an EntityManagerFactory with the same name
	 * in the same class loader is legal. The default is for it NOT to be legal.
	 */
	public void setAllowRedeploymentWithSameName(boolean preventRedeploymentWithSameName) {
		this.allowRedeploymentWithSameName = preventRedeploymentWithSameName;
	}
	
	/**
	 * Override the default strategy interface used to parse persistence.xml files
	 * @param persistenceUnitReader parser for reading persistence.xml files
	 */
	public void setPersistenceUnitReader(PersistenceUnitReader persistenceUnitReader) {
		this.persistenceUnitReader = persistenceUnitReader;
	}

	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	public LoadTimeWeaver getLoadTimeWeaver() {
		return loadTimeWeaver;
	}

	/**
	 * @return the persistenceUnitInfo
	 */
	public PersistenceUnitInfo getPersistenceUnitInfo() {
		return this.persistenceUnitInfo;
	}

	
	public void setLocation(Resource location) {
		this.location = location;
	}

	
	//-------------------------------------------------------------------------
	// Implementation of EntityManagerFactoryInfo
	//-------------------------------------------------------------------------	
	public String getPersistenceUnitName() {
		return persistenceUnitInfo.getPersistenceUnitName();
	}
	
	
	//-------------------------------------------------------------------------
	// Implementation of abstract methods from superclass
	//-------------------------------------------------------------------------	
	@Override
	protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
		validate();
		
		this.persistenceUnitInfo = parse();
		
		if (!this.allowRedeploymentWithSameName) {
			if (entityManagerFactoryNamesDeployed.contains(getPersistenceUnitName() )) {
				throw new IllegalStateException("EntityManagerFactory with name '" + getPersistenceUnitName() + "' " +
						"has already been deployed in this class loader; cannot deploy another");
			}
			else {
				logger.warn("Allowing redeployment of EntityManagerFactory with name '" + getPersistenceUnitName() + "' ");
			}
		}
		entityManagerFactoryNamesDeployed.add(getPersistenceUnitName());
		
		exposeEnvironment(persistenceUnitInfo);

		PersistenceProvider pp = instantiatePersistenceProvider();
		nativeEntityManagerFactory = pp.createContainerEntityManagerFactory(persistenceUnitInfo, getJpaPropertyMap());
		doAfterCreatingEntityManagerFactory(nativeEntityManagerFactory, persistenceUnitInfo);

		return nativeEntityManagerFactory;
	}

	/**
	 * Validate properties on this object
	 */
	protected void validate() {
		if (loadTimeWeaver == null) {
			throw new IllegalArgumentException("Please specify a load time weaving strategy to enable required JPA instrumentation");
		}
		if (location == null) {
			setLocation(resourceLoader.getResource(DEFAULT_PERSISTENCE_XML_LOCATION));
		}
		if (!location.exists()) {
			throw new IllegalArgumentException("Cannot find persistence unit at resource location " + location);
		}
	}

	
	/**
	 * Hook method allowing subclasses to customize the EntityManagerFactory
	 * @param emf EntityManagerFactory we are working with
	 * @param pui PersistenceUnitInfo used to configure the EntityManagerFactory
	 */
	protected void doAfterCreatingEntityManagerFactory(EntityManagerFactory emf, PersistenceUnitInfo pui) {
		// This implementation does nothing
	}

	/**
	 * Expose the runtime environment to our Spring-specific PersistenceUnitInfo
	 * implementation
	 * @param pui PersistenceUnitInfo to customize
	 */
	protected void exposeEnvironment(DefaultPersistenceUnitInfo pui) {
		pui.setLoadTimeWeaver(loadTimeWeaver);
		pui.setNonJtaDataSource(getDataSource());
		
		// use persistence provider if needed
		if (persistenceProviderClass == null && pui.getPersistenceProviderClassName() != null) {
			try {
				this.persistenceProviderClass = ClassUtils.forName(pui.getPersistenceProviderClassName());
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Cannot resolve provider classname '" + 
						pui.getPersistenceProviderClassName() + "'", 
						ex);
			}
		}
	}

	/**
	 * If no entity manager name was found, take first in the parsed array of
	 * ContainerPersistenceUnitInfo. Otherwise check for a matching name.
	 * 
	 * @return Spring-specific PersistenceUnitInfo
	 */
	protected DefaultPersistenceUnitInfo parse() {
		DefaultPersistenceUnitInfo[] infos = persistenceUnitReader.readPersistenceUnitInfo(location);
		DefaultPersistenceUnitInfo pui = null;

		if (infos.length == 0) {
			throw new IllegalArgumentException("No persistence units parsed from location: " + location);
		}

		if (getEntityManagerName() == null) {
			// Default to the first
			pui = infos[0];
		}
		else {
			// Find one with matching name
			for (DefaultPersistenceUnitInfo p : infos) {
				if (getEntityManagerName().equals(p.getPersistenceUnitName())) {
					pui = p;
					break;
				}
			}
		}

		if (pui == null) {
			throw new IllegalArgumentException("No persistence info with name matching '" + getEntityManagerName()
					+ "' parsed from " + location);
		}

		// loadTimeWeaver.setExplicitInclusions(pui.getManagedClassNames());

		return pui;
	}

}
