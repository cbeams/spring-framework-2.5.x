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

package org.springframework.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Sets up a JMS Connection and exposes it for bean references.
 * @author Mark Pollack
 */
public class JmsConnectionFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private ConnectionFactory connectionFactory;

	private String username;

	private String password;

	private String clientId;

	private ExceptionListener exceptionListener;

	private Connection connection;


	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setExceptionListener(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}


	public void afterPropertiesSet() throws JMSException {
		if (this.connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}

		logger.info("Creating JMS Connection");
		this.connection = (this.username != null) ?
				this.connectionFactory.createConnection(this.username, this.password) :
				this.connectionFactory.createConnection();

		if (this.clientId != null) {
			this.connection.setClientID(this.clientId);
		}
		if (this.exceptionListener != null) {
			this.connection.setExceptionListener(this.exceptionListener);
		}
	}


	public Object getObject() throws Exception {
		return connection;
	}

	public Class getObjectType() {
		return Connection.class;
	}

	public boolean isSingleton() {
		return true;
	}


	public void destroy() throws JMSException {
		logger.info("Closing JMS Connection");
		this.connection.close();
	}

}
