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

package org.springframework.jca.support;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This FactoryBean creates a local JCA connection factory in non-managed mode.
 *
 * <p>The type of the connection factory is dependent on the actual connector:
 * the connector can either expose its native API or follow the standard
 * Common Client Interface (CCI), as defined by the JCA spec. In the latter case,
 * the exposed interface is <code>javax.resource.cci.ConnectionFactory</code>.
 *
 * <p>In such a scenario, the connector uses a local ConnectionManager
 * and you can't participate in global transactions because the connector
 * will never be enlist/delist in the current JTA transaction.
 *
 * <p>In non-managed mode, a connector is not deployed on an application
 * server. Consequently, it can't use the server's system contracts:
 * connection management, transaction management, and security management.
 *
 * <p>In order to use this FactoryBean, you must specify the connector's
 * "managerConnectionFactory" (usually configured as separate JavaBean),
 * which will be used to create the actual connection factory.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setManagedConnectionFactory
 * @see javax.resource.cci.ConnectionFactory
 */
public class LocalConnectionFactoryBean implements FactoryBean, InitializingBean {

	private ManagedConnectionFactory managedConnectionFactory;

	private ConnectionManager connectionManager;

	private Object connectionFactory;


	/**
	 * Set the JCA ManagerConnectionFactory that should be used to create
	 * the desired connection factory.
	 * <p>The ManagerConnectionFactory will usually be set up as separate bean
	 * (potentially as inner bean), populated with JavaBean properties:
	 * a ManagerConnectionFactory is encouraged to follow the JavaBean pattern
	 * by the JCA specification, analogous to a JDBC DataSource and a JDO
	 * PersistenceManagerFactory.
	 * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
	 */
	public void setManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory) {
		this.managedConnectionFactory = managedConnectionFactory;
	}

	/**
	 * Set the JCA ConnectionManager that should be used to create the
	 * desired connection factory.
	 * <p>A ConnectionManager implementation for local usage is often
	 * included with a JCA connector. Such an included ConnectionManager
	 * might be set as default, with no need to explicitly specify one.
	 * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
	 */
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public void afterPropertiesSet() throws ResourceException {
		if (this.managedConnectionFactory == null) {
			throw new IllegalArgumentException("managedConnectionFactory is required");
		}
		if (this.connectionManager != null) {
			this.connectionFactory = this.managedConnectionFactory.createConnectionFactory(this.connectionManager);
		}
		else {
			this.connectionFactory = this.managedConnectionFactory.createConnectionFactory();
		}
	}


	public Object getObject() {
		return this.connectionFactory;
	}

	public Class getObjectType() {
		return (this.connectionFactory != null ? this.connectionFactory.getClass() : null);
	}

	public boolean isSingleton() {
		return true;
	}

}
