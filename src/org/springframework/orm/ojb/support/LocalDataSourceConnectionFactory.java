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

package org.springframework.orm.ojb.support;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.ojb.broker.accesslayer.ConnectionFactoryManagedImpl;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.metadata.JdbcConnectionDescriptor;

import org.springframework.beans.factory.BeanFactory;

/**
 * OJB connection factory that delegates to Spring-managed DataSource beans.
 *
 * <p>Define the following entry in your OJB.properties to use this connection factory:
 *
 * <pre>
 * ConnectionFactoryClass=org.springframework.orm.ojb.support.LocalDataSourceConnectionFactory</pre>
 *
 * Interprets JCD aliases in OJB's JDBC connection descriptors as Spring bean names.
 * For example, the following will delegate to the Spring bean named "myDataSource":
 *
 * <pre>
 * &lt;jdbc-connection-descriptor jcd-alias="myDataSource" default-connection="true" useAutoCommit="1"/&gt;</pre>
 *
 * Depends on LocalOjbConfigurer being defined as Spring bean, which will expose
 * the Spring BeanFactory to the corresponding static field of this connection factory.
 *
 * @author Juergen Hoeller
 * @since 03.07.2004
 * @see LocalOjbConfigurer
 */
public class LocalDataSourceConnectionFactory extends ConnectionFactoryManagedImpl {

	/**
	 * This will hold the BeanFactory to retrieve DataSource beans from.
	 */
	protected static BeanFactory beanFactory;

	public LocalDataSourceConnectionFactory() {
		if (beanFactory == null) {
			throw new IllegalStateException("No BeanFactory found for configuration - " +
																			"LocalOjbConfigurer must be defined as Spring bean");
		}
	}

	public Connection lookupConnection(JdbcConnectionDescriptor jcd) throws LookupException {
		try {
			DataSource dataSource = (DataSource) beanFactory.getBean(jcd.getJcdAlias(), DataSource.class);
			return dataSource.getConnection();
		}
		catch (Exception ex) {
			throw new LookupException("Could not obtain connection from data source", ex);
		}
	}

}
