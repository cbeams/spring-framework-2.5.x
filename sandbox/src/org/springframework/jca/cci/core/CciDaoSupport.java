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

package org.springframework.jca.cci.core;

import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jca.cci.CannotGetCciConnectionException;
import org.springframework.jca.cci.connection.ConnectionFactoryUtils;

/**
 * Convenient super class for CCI data access objects.
 * Requires a ConnectionFactory to be set, providing a
 * CciTemplate based on it to subclasses.
 *
 * <p>This base class is mainly intended for CciTemplate usage
 * but can also be used when working with ConnectionFactoryUtils directly
 * or with org.springframework.cci.object classes.
 *
 * @author Thierry TEMPLIER
 * @see #setConnectionFactory
 * @see org.springframework.jdbc.core.CciTemplate
 * @see org.springframework.jdbc.datasource.ConnectionFactoryUtils
 */
public abstract class CciDaoSupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private CciTemplate cciTemplate;
	private ConnectionFactory connectionFactory;
	private OutputRecordCreator outputCreator;

	/**
	 * Set the ConnectionFactory to be used by this DAO.
	 */
	public final void setConnectionFactory(ConnectionFactory connectionFactory) {
	  this.connectionFactory=connectionFactory;
	}

	/**
	 * Return the ConnectionFactory used by this DAO.
	 */
	public final ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * Return the OutputCreator used by this DAO.
	 */
	public OutputRecordCreator getOutputCreator() {
		return outputCreator;
	}

	/**
	 * Set the OutputCreator used by this DAO.
	 */
	public void setOutputCreator(OutputRecordCreator outputCreator) {
		this.outputCreator = outputCreator;
	}

	/**
	 * Set the CciTemplate for this DAO explicitly,
	 * as an alternative to specifying a ConnectionFactory.
	 */
	public final void setCciTemplate(CciTemplate cciTemplate) {
		this.cciTemplate = cciTemplate;
	}

	/**
	 * Return the CciTemplate for this DAO,
	 * pre-initialized with the ConnectionFactory or set explicitly.
	 */
	public final CciTemplate getCciTemplate() {
	  return cciTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if( connectionFactory!=null ) {
			if( outputCreator!=null ) {
				this.cciTemplate = new CciTemplate(connectionFactory,outputCreator);
			} else {
				this.cciTemplate = new CciTemplate(connectionFactory);
			}
		}

		if (this.cciTemplate == null) {
			throw new IllegalArgumentException("connectionFactory or cciTemplate is required");
		}
		initDao();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails
	 */
	protected void initDao() throws Exception {
	}


	/**
	 * Get a Cci Connection, either from the current transaction or a new one.
	 * @return the Cci Connection
	 * @throws org.springframework.cci.CannotGetCciConnectionException if the attempt to get a Connection failed
	 */
	protected final Connection getConnection() throws CannotGetCciConnectionException {
		return ConnectionFactoryUtils.getConnection(getConnectionFactory());
	}

	/**
	 * Close the given Cci Connection if necessary, created via this bean's
	 * ConnectionFactory, if it isn't bound to the thread.
	 * @param con Connection to close
	 */
	protected final void closeConnectionIfNecessary(Connection con) {
		ConnectionFactoryUtils.closeConnectionIfNecessary(con, getConnectionFactory());
	}

}
