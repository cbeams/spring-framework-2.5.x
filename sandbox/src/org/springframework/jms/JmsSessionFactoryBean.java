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
import javax.jms.Session;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create a JMS Session from a Connection.  Requires a JMS 1.1 provider.
 *
 * @author Mark Pollack
 */
public class JmsSessionFactoryBean
    implements FactoryBean, InitializingBean, DisposableBean {

	private Connection connection;

	private Session session;

	private boolean transacted = false;

	private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	protected final Log logger = LogFactory.getLog(getClass());

	public Object getObject() throws Exception {
		return session;
	}

	public Class getObjectType() {
		return Session.class;
	}

	public boolean isSingleton() {
		return true;
	}


	public void afterPropertiesSet() throws Exception {
		if (connection == null) {
			throw new IllegalArgumentException("Did not set required JMS connection property");
		}
		logger.info("Creating JMS Session");
		session = connection.createSession(transacted, acknowledgeMode);
	}


	public void destroy() throws Exception {
		logger.info("Closing JMS Session");
		session.close();
	}

	/**
	 * Set the acknowledgement mode
	 * @param i ack mode
	 */
	public void setAcknowledgeMode(int i) {
		acknowledgeMode = i;
	}

	/**
	 * Set the transaction attribute of the session.
	 * @param b true or false
	 */
	public void setTransacted(boolean b) {
		transacted = b;
	}

	/**
	 * Set the connection to use to create a session.
	 * @param c The JMS connection
	 */
	public void setConnection(Connection c) {
		connection = c;
	}

}
