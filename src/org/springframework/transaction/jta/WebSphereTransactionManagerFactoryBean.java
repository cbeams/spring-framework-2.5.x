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

import java.lang.reflect.InvocationTargetException;

import javax.transaction.TransactionManager;

import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean that retrieves the JTA TransactionManager for IBM's
 * WebSphere 4 and 5 application servers.
 *
 * <p>Uses WebSphere's static access methods to obtain the JTA
 * TransactionManager (different for WebSphere 4.x and 5.x).
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see JtaTransactionManager#setTransactionManager
 * @see com.ibm.ejs.jts.jta.JTSXA#getTransactionManager
 * @see com.ibm.ejs.jts.jta.TransactionManagerFactory#getTransactionManager
 */
public class WebSphereTransactionManagerFactoryBean implements FactoryBean {

	private final TransactionManager transactionManager;

	public WebSphereTransactionManagerFactoryBean()
	    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class clazz;
		try {
			// try WebSphere 5
			clazz = Class.forName("com.ibm.ejs.jts.jta.TransactionManagerFactory");
		}
		catch (ClassNotFoundException ex) {
			// try WebSphere 4
			clazz = Class.forName("com.ibm.ejs.jts.jta.JTSXA");
		}
		this.transactionManager = (TransactionManager) clazz.getMethod("getTransactionManager", null).invoke(null, null);
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
