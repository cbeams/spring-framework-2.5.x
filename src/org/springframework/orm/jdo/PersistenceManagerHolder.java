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

package org.springframework.orm.jdo;

import javax.jdo.PersistenceManager;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Holder wrapping a JDO PersistenceManager.
 * JdoTransactionManager binds instances of this class
 * to the thread, for a given PersistenceManagerFactory.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoTransactionManager
 * @see PersistenceManagerFactoryUtils
 */
public class PersistenceManagerHolder extends ResourceHolderSupport {

	private PersistenceManager persistenceManager;

	public PersistenceManagerHolder(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

}
