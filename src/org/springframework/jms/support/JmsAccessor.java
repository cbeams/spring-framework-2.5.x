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

package org.springframework.jms.support;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.JmsException;

/**
 * Base class for JmsTemplate and other JMS-accessing gateway helpers,
 * defining common properties like the ConnectionFactory.
 *
 * <p>Not intended to be used directly. See JmsTemplate.
 *
 * @author Juergen Hoeller
 * @since 1.2
 */
public abstract class JmsAccessor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	protected ConnectionFactory connectionFactory;

	/**
	 * Set the connection factory used for obtaining JMS connections.
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Return the connection factory used for obtaining JMS connections.
	 */
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void afterPropertiesSet() {
		if (this.connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}
	}

	/**
	 * Convert the specified checked {@link javax.jms.JMSException JMSException} to
	 * a Spring runtime {@link org.springframework.jms.JmsException JmsException}
	 * equivalent.
	 * <p>Default implementation delegates to JmsUtils.
	 * @param ex the original checked JMSException to convert
	 * @return the Spring runtime JmsException wrapping <code>ex</code>
	 * @see org.springframework.jms.support.JmsUtils#convertJmsAccessException
	 */
	protected JmsException convertJmsAccessException(JMSException ex) {
		return JmsUtils.convertJmsAccessException(ex);
	}

}
