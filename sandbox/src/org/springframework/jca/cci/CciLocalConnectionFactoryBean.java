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

package org.springframework.jca.cci;

import java.util.Map;

import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.ManagedConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * CciLocalConnectionFactoryBean creates a CCI ConnectionFactory
 * instance in a non-managed mode. In this case, the connector
 * uses its default connection manager and you can't use global
 * transactions because the connector will never be enlist/delist
 * in the current transaction of the current thread. It can be a
 * problem if the transaction level of your connection is XATransaction.
 * You must be aware that, in non-managed mode, a connector doesn't
 * need to be deployed and configured on an application server and
 * so it can't use its system contracts (connection management,
 * transaction management and security management).
 * 
 * In order to use this factory bean, you must specify the managed
 * connection factory of the connector and set parameters on this
 * class in a JavaBean style.
 * 
 * @author Thierry TEMPLIER
 */
public class CciLocalConnectionFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
	private Class managedConnectionFactoryClass;
	private Map properties;
	private ConnectionFactory connectionFactory;

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Return the singleton ConnectionFactory.
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		return this.connectionFactory;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return (this.connectionFactory!= null) ? this.connectionFactory.getClass() : ConnectionFactory.class;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		logger.info("Building new ConnectionFactory");
		ManagedConnectionFactory managedConnectionFactory=(ManagedConnectionFactory)managedConnectionFactoryClass.newInstance();
		if( properties!=null ) {
			BeanWrapper wrapper=new BeanWrapperImpl(managedConnectionFactory);
			wrapper.setPropertyValues(properties);
		}
		this.connectionFactory=(ConnectionFactory)managedConnectionFactory.createConnectionFactory();
	}

	/**
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
	}

	/**
	 * @return
	 */
	public Class getManagedConnectionFactoryClass() {
		return managedConnectionFactoryClass;
	}

	/**
	 * @param class1
	 */
	public void setManagedConnectionFactoryClass(Class class1) {
		managedConnectionFactoryClass = class1;
	}

	/**
	 * @return
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * @param map
	 */
	public void setProperties(Map map) {
		properties = map;
	}

}
