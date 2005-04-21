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
 * FactoryBean that retrieves the internal JTA TransactionManager of BEA's
 * WebLogic version 7.0, which is required for proper transaction suspension
 * support on that application server version.
 * 
 * <p>Uses WebLogic <code>TxHelper</code>'s static access methods to obtain
 * the server's internal JTA TransactionManager.
 *
 * <p>This class doesn't need be used with WebLogic 8.1 or higher, since the
 * regular JNDI lookup is sufficient there: it returns a JTA TransactionManager
 * that can handle all transaction management tasks properly.
 *
 * <p><b>Note that as of Spring 1.2, this class is effectively superseded by
 * WebLogicJtaTransactionManager's autodetection of WebLogic 7.0 or 8.1+.</b>
 * It is only kept as a way to explicitly expose the JTA TransactionManager
 * on WebLogic 7.0, for non-Spring code that needs access to this facility.
 *
 * <p><b>For typical scenarios, use Spring's WebLogicJtaTransactionManager
 * as-is and do not bother with setting up this FactoryBean.</b>
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see WebLogicJtaTransactionManager
 * @see JtaTransactionManager#setTransactionManager
 * @see weblogic.transaction.TxHelper#getTransactionManager
 */
public class WebLogicServerTransactionManagerFactoryBean implements FactoryBean {

	private static final String TX_HELPER_CLASS_NAME = "weblogic.transaction.TxHelper";

	protected final Log logger = LogFactory.getLog(getClass());

	private final TransactionManager transactionManager;

	/**
	 * This constructor retrieves the WebLogic TransactionManager factory class,
	 * so we can get access to the JTA TransactionManager.
	 */
	public WebLogicServerTransactionManagerFactoryBean() throws TransactionSystemException {
		try {
			logger.debug("Looking for WebLogic TxHelper: " + TX_HELPER_CLASS_NAME);
			Class helperClass = Class.forName(TX_HELPER_CLASS_NAME);
			if (logger.isDebugEnabled()) {
				logger.debug("Found WebLogic TxHelper: " + helperClass.getName());
			}
			Method method = helperClass.getMethod("getTransactionManager", (Class[]) null);
			this.transactionManager = (TransactionManager) method.invoke(null, (Object[]) null);
		}
		catch (ClassNotFoundException ex) {
			throw new TransactionSystemException("Could not find WebLogic's TxHelper class", ex);
		}
		catch (InvocationTargetException ex) {
			throw new TransactionSystemException(
					"WebLogic's TxHelper.getTransactionManager method failed", ex.getTargetException());
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not access WebLogic's TxHelper.getTransactionManager method", ex);
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
