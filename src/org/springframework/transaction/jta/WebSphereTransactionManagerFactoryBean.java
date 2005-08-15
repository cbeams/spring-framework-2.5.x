/*
 * Copyright 2002-2005 the original author or authors.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.TransactionSystemException;

/**
 * FactoryBean that retrieves the JTA TransactionManager for IBM's
 * WebSphere application servers (versions 6, 5.1, 5.0 and 4).
 *
 * <p>Uses WebSphere's static access methods to obtain the JTA TransactionManager,
 * which is different for WebSphere 5.1+, 5.0 and 4.
 *
 * <p>In combination with Spring's JtaTransactionManager, this FactoryBean
 * can be used to enable transaction suspension (PROPAGATION_REQUIRES_NEW,
 * PROPAGATION_NOT_SUPPORTED) on WebSphere:
 *
 * <pre>
 * &lt;bean id="wsJtaTm" class="org.springframework.transaction.jta.WebSphereTransactionManagerFactoryBean"/&gt;
 *
 * &lt;bean id="transactionManager" class="org.springframework.transaction.jta.JtaTransactionManager"&gt;
 *   &lt;property name="transactionManager ref="wsJtaTm"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * Note that Spring's JtaTransactionManager will continue to use the JTA
 * UserTransaction for standard transaction demarcation, as defined by
 * standard J2EE. It will only use the provided WebSphere TransactionManager
 * in case of actual transaction suspension needs.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see JtaTransactionManager#setTransactionManager
 * @see com.ibm.ws.Transaction.TransactionManagerFactory#getTransactionManager
 * @see com.ibm.ejs.jts.jta.JTSXA#getTransactionManager
 * @see com.ibm.ejs.jts.jta.TransactionManagerFactory#getTransactionManager
 */
public class WebSphereTransactionManagerFactoryBean implements FactoryBean {

	private static final String FACTORY_CLASS_5_1 = "com.ibm.ws.Transaction.TransactionManagerFactory";

	private static final String FACTORY_CLASS_5_0 = "com.ibm.ejs.jts.jta.TransactionManagerFactory";

	private static final String FACTORY_CLASS_4 = "com.ibm.ejs.jts.jta.JTSXA";


	protected final Log logger = LogFactory.getLog(getClass());

	private final TransactionManager transactionManager;


	/**
	 * This constructor retrieves the WebSphere TransactionManager factory class,
	 * so we can get access to the JTA TransactionManager.
	 */
	public WebSphereTransactionManagerFactoryBean() throws TransactionSystemException {
		Class clazz;
		try {
			logger.debug("Trying WebSphere 5.1+: " + FACTORY_CLASS_5_1);
			clazz = Class.forName(FACTORY_CLASS_5_1);
			logger.info("Found WebSphere 5.1+: " + FACTORY_CLASS_5_1);
		}
		catch (ClassNotFoundException ex) {
			logger.debug("Could not find WebSphere 5.1/6.0 TransactionManager factory class", ex);
			try {
				logger.debug("Trying WebSphere 5.0: " + FACTORY_CLASS_5_0);
				clazz = Class.forName(FACTORY_CLASS_5_0);
				logger.info("Found WebSphere 5.0: " + FACTORY_CLASS_5_0);
			}
			catch (ClassNotFoundException ex2) {
				logger.debug("Could not find WebSphere 5.0 TransactionManager factory class", ex2);
				try {
					logger.debug("Trying WebSphere 4: " + FACTORY_CLASS_4);
					clazz = Class.forName(FACTORY_CLASS_4);
					logger.info("Found WebSphere 4: " + FACTORY_CLASS_4);
				}
				catch (ClassNotFoundException ex3) {
					logger.debug("Could not find WebSphere 4 TransactionManager factory class", ex3);
					throw new TransactionSystemException(
							"Could not find any WebSphere TransactionManager factory class, " +
							"neither for WebSphere version 5.1+ nor 5.0 nor 4");
				}
			}
		}

		try {
			Method method = clazz.getMethod("getTransactionManager", (Class[]) null);
			this.transactionManager = (TransactionManager) method.invoke(null, (Object[]) null);
		}
		catch (InvocationTargetException ex) {
			throw new TransactionSystemException(
					"WebSphere's TransactionManagerFactory.getTransactionManager method failed", ex.getTargetException());
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not access WebSphere's TransactionManagerFactory.getTransactionManager method", ex);
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
