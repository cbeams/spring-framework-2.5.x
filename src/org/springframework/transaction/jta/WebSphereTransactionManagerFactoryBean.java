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
 * FactoryBean that retrieves the JTA TransactionManager for IBM's
 * WebSphere application servers (versions 5.1, 5.0 and 4).
 *
 * <p>Uses WebSphere's static access methods to obtain the JTA
 * TransactionManager (different for WebSphere 5.1, 5.0 and 4).
 *
 * <p>The strategy has been kindly borrowed from Hibernate's
 * WebSphereTransactionManagerLookup class.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see JtaTransactionManager#setTransactionManager
 * @see net.sf.hibernate.transaction.WebSphereTransactionManagerLookup
 * @see com.ibm.ws.Transaction.TransactionManagerFactory#getTransactionManager
 * @see com.ibm.ejs.jts.jta.JTSXA#getTransactionManager
 * @see com.ibm.ejs.jts.jta.TransactionManagerFactory#getTransactionManager
 */
public class WebSphereTransactionManagerFactoryBean implements FactoryBean {

	public static final String FACTORY_CLASS_5_1 = "com.ibm.ws.Transaction.TransactionManagerFactory";

	public static final String FACTORY_CLASS_5_0 = "com.ibm.ejs.jts.jta.TransactionManagerFactory";

	public static final String FACTORY_CLASS_4 = "com.ibm.ejs.jts.jta.JTSXA";

	private static final Log logger = LogFactory.getLog(WebSphereTransactionManagerFactoryBean.class);

	private final TransactionManager transactionManager;

	public WebSphereTransactionManagerFactoryBean() throws TransactionSystemException {
		Class clazz;
		try {
			logger.debug("Trying WebSphere 5.1: " + FACTORY_CLASS_5_1);
			clazz = Class.forName(FACTORY_CLASS_5_1);
			logger.info("Found WebSphere 5.1: " + FACTORY_CLASS_5_1);
		}
		catch (ClassNotFoundException ex) {
			try {
				logger.debug("Trying WebSphere 5.0: " + FACTORY_CLASS_5_0);
				clazz = Class.forName(FACTORY_CLASS_5_0);
				logger.info("Found WebSphere 5.0: " + FACTORY_CLASS_5_0);
			}
			catch (ClassNotFoundException ex2) {
				try {
					logger.debug("Trying WebSphere 4: " + FACTORY_CLASS_4);
					clazz = Class.forName(FACTORY_CLASS_4);
					logger.info("Found WebSphere 4: " + FACTORY_CLASS_4);
				}
				catch (ClassNotFoundException ex3) {
					throw new TransactionSystemException("Couldn't find any WebSphere TransactionManager factory class, " +
					                                     "neither for WebSphere version 5.1 nor 5.0 nor 4");
				}
			}
		}
		try {
			Method method = clazz.getMethod("getTransactionManager", null);
			this.transactionManager = (TransactionManager) method.invoke(null, null);
		}
		catch (Exception ex) {
			throw new TransactionSystemException("Found WebSphere TransactionManager factory class [" + clazz.getName() +
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
