/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.factory.dynamic.persist;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource;

/**
 * 
 * @author Rod Johnson
 */
public abstract class AbstractPersistenceStoreRefreshableTargetSource extends AbstractRefreshableTargetSource
		implements DatabaseBean, BeanFactoryAware  {

	// TODO non-object PK support
	private long pk;

	private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

	private Class persistentClass;

	private AutowireCapableBeanFactory aabf;

	public AbstractPersistenceStoreRefreshableTargetSource() {
		// Don't want introduction to expose this SPI interface
		//suppressInterface(BeanFactoryAware.class);
	}


	/**
	 * @return Returns the autowireMode.
	 */
	public int getAutowireMode() {
		return autowireMode;
	}

	/**
	 * Set the type of autowiring to apply to the bean once it's
	 * returned from the persistent store.
	 * @param autowireMode The autowireMode to set.
	 * 0 means no autowiring.
	 */
	public void setAutowireMode(int autowireMode) {
		if (autowireMode != AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE &&
				autowireMode != AutowireCapableBeanFactory.AUTOWIRE_BY_NAME) {
			throw new IllegalArgumentException("Autowiring only supported by type or name");
		}
		this.autowireMode = autowireMode;
	}


	public void setBeanFactory(BeanFactory bf) {
		if (bf instanceof AutowireCapableBeanFactory) {
			aabf = (AutowireCapableBeanFactory) bf;
			logger.info("Will autowire instances: running in an AutowireCapableBeanFactory");
		}
		else {
			logger.warn("Cannot autowire bean instances: not in an AutowireCapableBeanFactory");
		}

		refresh();
	}

	/**
	 * @return Returns the persistentClass.
	 */
	public Class getPersistentClass() {
		return persistentClass;
	}

	public final String getStoreDetails() {
		return "class=" + persistentClass + "; pk=" + pk + "; " + storeDetails();
	}

	protected abstract String storeDetails();

	/**
	 * @param persistentClass The persistentClass to set.
	 */
	public void setPersistentClass(Class persistentClass) {
		this.persistentClass = persistentClass;
	}

	public synchronized long getPrimaryKey() {
		return pk;
	}

	public synchronized void setPrimaryKey(long pk) throws DataAccessException {
		this.pk = pk;
	}

	/**
	 */
	protected Object freshTarget() throws DataAccessException {
		// Use Hibernate to load the object

		logger.info("Loading persistent object with class=" + persistentClass.getName() + " and pk=" + pk);
		Object o = loadFromPersistentStore();

		// If we have an autowire capable factory and autowiring is not switched off, 
		// wire the instance up
		if (aabf != null && autowireMode != 0) {
			// TODO What if duping between ORM populated and BF populated??
			// BF will win
			logger.info("Autowiring properties of persistent object with class=" + persistentClass.getName() + " and pk=" + pk);
			aabf.autowireBeanProperties(o, autowireMode, false);
		}

		// TODO what about publishing with the factory the dependencies of the persistent object?
		return o;
	}

	/**
	 * Subclasses must implement this using Hibernate/another ORM tool.
	 * @return
	 */
	protected abstract Object loadFromPersistentStore() throws DataAccessException;

}
