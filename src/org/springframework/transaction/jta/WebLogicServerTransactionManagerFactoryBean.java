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

package org.springframework.transaction.jta;

import java.lang.reflect.Method;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.TransactionSystemException;

/**
 * FactoryBean that retrieves the JTA TransactionManager for BEA's
 * WebLogic application server version 7.0.  
 * 
 * <p>This class doesn't need be used with version 8.1 since for this version 
 * the regular JNDI lookup returns a ClientTransactionManagerImpl that can 
 * handle the necessary transaction management tasks. 
 *
 * <p>Uses WebLogic TxHelper's static access methods to obtain the JTA
 * TransactionManager.
 *
 * @author Thomas Risberg
 * @since 1.1
 * @see JtaTransactionManager#setTransactionManager
 * @see weblogic.transaction.TxHelper#getTransactionManager
 */
public class WebLogicServerTransactionManagerFactoryBean implements FactoryBean {

	private static final String FACTORY_CLASS = "weblogic.transaction.TxHelper";

	protected final Log logger = LogFactory.getLog(getClass());

	private final TransactionManager transactionManager;

	/**
	 * This constructor retrieves the WebLogic TransactionManager factory class,
	 * so we can get access to the JTA TransactionManager.
	 */
	public WebLogicServerTransactionManagerFactoryBean() throws TransactionSystemException {
		Class clazz;
		try {
			logger.debug("Trying Weblogic: " + FACTORY_CLASS);
			clazz = Class.forName(FACTORY_CLASS);
			logger.info("Found WebLogic: " + FACTORY_CLASS);
		}
		catch (ClassNotFoundException ex) {
			logger.debug("Could not find WebLogic TransactionManager factory class", ex);
			throw new TransactionSystemException(
					"Couldn't find the WebLogic TransactionManager factory class");
		}
		try {
			Method method = clazz.getMethod("getTransactionManager", null);
			this.transactionManager = (TransactionManager) method.invoke(null, null);
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Found WebLogic TransactionManager factory class [" + clazz.getName() +
					"], but couldn't invoke its static 'getTransactionManager' method", ex);
		}
	}

	public Object getObject() {
		return this.transactionManager;
	}

	public Class getObjectType() {
		return this.transactionManager.getClass();
	}

	public boolean isSingleton() {
		return true;
	}

}
