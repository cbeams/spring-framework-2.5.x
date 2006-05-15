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

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Interface introduced by Spring-managed EntityManagerFactory
 * proxies. This interface does not extend EntityManagerFactory, 
 * so can also be implemented by objects
 * that are not themselves EntityManagerFactorys, but know about the
 * configuration of an EntityManagerFactory (such as 
 * an AbstractEntityManagerFactoryBean).
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public interface EntityManagerFactoryInfo {
	
	/**
	 * Return the underlying EntityManagerFactory, returned
	 * by the PersistenceProvider
	 * @return the unadorned EntityManagerFactory
	 */
	EntityManagerFactory getNativeEntityManagerFactory();
	
	/**
	 * Return the PersistenceUnitInfo used to create this
	 * EntityManagerFactory, if the in-container API was used
	 * @return the PersistenceUnitInfo used to create this
	 * EntityManagerFactory, or null if the in-container contract
	 * was not used to configure the EntityManagerFactory
	 */
	PersistenceUnitInfo getPersistenceUnitInfo();
	
	/**
	 * Return the name of the EntityManager, or null if
	 * it is an unnamed default. If getPersistenceUnitInfo() returns
	 * non-null, the return type of getEntityManagerName() must
	 * be equal to the value returned by 
	 * PersistenceUnitInfo.getPersistenceUnitName() 
	 * @return the entity manager name, or null if it is not
	 * available
	 */
	String getPersistenceUnitName();
	
	/**
	 * 
	 * @return the vendor properties object if available.
	 * Can return null.
	 */
	VendorProperties getVendorProperties();

}
