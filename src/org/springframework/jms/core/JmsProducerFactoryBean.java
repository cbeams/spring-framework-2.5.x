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

package org.springframework.jms.core;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Create a JMS Producer from a Session.  It is creates non-singleton/prototype bean.
 * Requires a JMS 1.1 provider.
 *
 * @author Mark Pollack
 */
public class JmsProducerFactoryBean
    implements FactoryBean, InitializingBean {

	private MessageProducer messageProducer;

	private Session session;

	private Destination destination;

	private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;

	private int priority = Message.DEFAULT_PRIORITY;

	private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;

	private boolean disableMessageTimestamp = false;

	private boolean disableMessageID = false;


	protected final Log logger = LogFactory.getLog(getClass());

	public Object getObject() throws Exception {
		return messageProducer;
	}

	public Class getObjectType() {
		return MessageProducer.class;
	}

	public boolean isSingleton() {
		return false;
	}


	public void afterPropertiesSet() throws Exception {
		if (session == null) {
			throw new IllegalArgumentException("Did not set required JMS session property");
		}
		logger.info("Creating JMS Producer");

		if (destination != null) {
			messageProducer = session.createProducer(destination);
		}
		else {
			logger.info("Creating a MessageProducer without a destination.  Just specify destination on each " +
			            "send operation.");
			messageProducer = session.createProducer(null);
		}

		messageProducer.setDeliveryMode(deliveryMode);
		messageProducer.setPriority(priority);
		messageProducer.setTimeToLive(timeToLive);
		messageProducer.setDisableMessageID(disableMessageID);
		messageProducer.setDisableMessageTimestamp(disableMessageTimestamp);


	}


	/**
	 * Set the delivery mode for the MessageProducer being created.
	 * @param i
	 */
	public void setDeliveryMode(int i) {
		deliveryMode = i;
	}

	/**
	 * @param i
	 */
	public void setPriority(int i) {
		priority = i;
	}

	/**
	 * @param l
	 */
	public void setTimeToLive(long l) {
		timeToLive = l;
	}

	/**
	 * @param s
	 */
	public void setSession(Session s) {
		session = s;
	}

	/**
	 * @param b
	 */
	public void setDisableMessageID(boolean b) {
		disableMessageID = b;
	}

	/**
	 * @param b
	 */
	public void setDisableMessageTimestamp(boolean b) {
		disableMessageTimestamp = b;
	}

	/**
	 * @param d
	 */
	public void setDestination(Destination d) {
		destination = d;
	}


}
