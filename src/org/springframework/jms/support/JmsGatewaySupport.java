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

package org.springframework.jms.support;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;

/**
 * Convenient super class for JMS access.
 * 
 * <p>Requires a ConnectionFactory to be set.  This can be done
 * directly or by using a JmsTemplate.  This class will create
 * its own JmsTemplate if a ConnectionFactory is passed in.   
 * A custom JmsTemplate instance can be used through overriding
 * <code>createJmsTemplate</code>
 * 
 * @author Mark Pollack
 */
public abstract class JmsGatewaySupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());
	
	private JmsTemplate jmsTemplate;
	
	/**
	 * Will set the JMS Connection factory to be used by the gateway.
	 * Will automatically create a JmsTemplate for the given ConnectionFactory
	 * @see #createJmsTemplate
	 * @see #setConnectionFactory(ConnectionFactory)
	 * @param connFactory
	 */
	public final void setConnectionFactory(ConnectionFactory connFactory) {
		jmsTemplate = createJmsTemplate(connFactory);
	}
	
	/**
	 * Create a JmsTemplate for the given ConnectionFactory.
	 * Only invoked if populating the gatway with a ConnectionFactory reference.
	 * <p>Can be overridden in subclasses to provide a JmsTemplate instance with
	 * a different configuration or the 1.0.2 version, JmsTemplate102.
	 * 
	 * @param connFactory the JMS ConnectionFactory to create a JmsTemplate for
	 * @return the new JmsTemplate instance
	 * @see #setConnectionFactory(ConnectionFactory)
	 */
	protected JmsTemplate createJmsTemplate(ConnectionFactory connFactory) {
		return new JmsTemplate(connFactory);
	}
	
	/**
	 * Return the JMS ConnectionFactory used by the gateway.
	 */
	public final ConnectionFactory getConnectionFactory() {
		return (this.jmsTemplate != null ? this.jmsTemplate.getConnectionFactory() : null);
	}
	
	/**
	 * Set the JmsTemplate for the gateway.
	 * @param jmsTemplate
	 * @see #setConnectionFactory(ConnectionFactory)
	 */
	public final void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	/**
	 * Return the JmsTemplate for the gateway.
	 */
	public final JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}
	
	public final void afterPropertiesSet() throws Exception {
		if (this.jmsTemplate == null) {
			throw new IllegalArgumentException("connectionFactory or jmsTemplate is required");
		}
		initGateway();
	}
	
	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails.
	 * @throws Exception
	 */
	protected void initGateway() throws Exception {		
	}
}
