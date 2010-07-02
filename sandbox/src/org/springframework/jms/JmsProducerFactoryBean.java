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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Create a JMS Producer from a Session.
 * Requires a JMS 1.1 provider.
 * @author Mark Pollack
 */
public class JmsProducerFactoryBean implements FactoryBean, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Session session;

	private Destination destination;

	private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;

	private int priority = Message.DEFAULT_PRIORITY;

	private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;

	private boolean disableMessageID = false;

	private boolean disableMessageTimestamp = false;

	private MessageProducer messageProducer;


	/**
	 * Set the JMS session used to create the Consumer.
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * Set the JMS destination to consume messages from.
	 */
	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setDisableMessageID(boolean disableMessageID) {
		this.disableMessageID = disableMessageID;
	}

	public void setDisableMessageTimestamp(boolean disableMessageTimestamp) {
		this.disableMessageTimestamp = disableMessageTimestamp;
	}


	public void afterPropertiesSet() throws JMSException {
		if (this.session == null) {
			throw new IllegalArgumentException("session is required");
		}

		logger.info("Creating JMS Producer");

		if (this.destination != null) {
			this.messageProducer = this.session.createProducer(this.destination);
		}
		else {
			logger.debug("Creating a MessageProducer without a destination. " +
					"Destination needs to be specified on each send operation.");
			this.messageProducer = this.session.createProducer(null);
		}

		this.messageProducer.setDeliveryMode(this.deliveryMode);
		this.messageProducer.setPriority(this.priority);
		this.messageProducer.setTimeToLive(this.timeToLive);
		this.messageProducer.setDisableMessageID(this.disableMessageID);
		this.messageProducer.setDisableMessageTimestamp(this.disableMessageTimestamp);
	}


	public Object getObject() {
		return this.messageProducer;
	}

	public Class getObjectType() {
		return (this.messageProducer != null ? this.messageProducer.getClass() : MessageProducer.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
