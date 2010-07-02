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
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Create a JMS Consumer from a Session.
 * Requires a JMS 1.1 provider.
 * @author Mark Pollack
 */
public class JmsConsumerFactoryBean implements FactoryBean, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Session session;

	private Destination destination;

	private String durableIdentity;

	private Boolean noLocal;

	private String messageSelector;

	private MessageListener messageListener;

	private MessageConsumer messageConsumer;


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

	public void setDurableIdentity(String durableIdentity) {
		this.durableIdentity = durableIdentity;
	}

	/**
	 * Set the nolocal property, used when creating a consumer.
	 */
	public void setNoLocal(boolean noLocal) {
		this.noLocal = new Boolean(noLocal);
	}

	/**
	 * Set the message selector to apply to the consumer.
	 */
	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}

	/**
	 * Set the message listener to use for asynchronous receiving of messages.
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}


	public void afterPropertiesSet() throws JMSException {
		if (this.session == null) {
			throw new IllegalArgumentException("session is required");
		}
		if (this.destination == null) {
			throw new IllegalArgumentException("destination is required");
		}

		logger.info("Creating JMS Consumer");

		if (this.durableIdentity != null) {
			if (!(this.destination instanceof Topic)) {
				throw new IllegalArgumentException("Destination must be a topic when creating a durable subscriber");
			}
			if (this.noLocal == null) {
				this.messageConsumer =
						this.session.createDurableSubscriber((Topic) this.destination, this.durableIdentity);
			}
			else {
				this.messageConsumer =
				    this.session.createDurableSubscriber(
								(Topic) this.destination, this.durableIdentity, this.messageSelector, this.noLocal.booleanValue());
			}
		}

		else {
			if (this.noLocal == null) {
				this.messageConsumer = this.session.createConsumer(this.destination, this.messageSelector);
			}
			else {
				this.messageConsumer =
				    this.session.createConsumer(this.destination, this.messageSelector, this.noLocal.booleanValue());
			}
		}

		if (this.messageListener != null) {
			this.messageConsumer.setMessageListener(this.messageListener);
		}
	}


	public Object getObject() {
		return this.messageConsumer;
	}

	public Class getObjectType() {
		return (this.messageConsumer != null ? this.messageConsumer.getClass() : MessageConsumer.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
