/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jms.listener.adapter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.SimpleMessageConverter102;

/**
 * A subclass of MessageListenerAdapter that uses the JMS 1.0.2 specification,
 * rather than the JMS 1.1 methods used by MessageListenerAdapter itself.
 * This class can be used for JMS 1.0.2 providers, offering the same facility
 * as MessageListenerAdapter does for JMS 1.1 providers.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class MessageListenerAdapter102 extends MessageListenerAdapter {

	/**
	 * Initialize the default implementations for the adapter's strategies:
	 * SimpleMessageConverter102.
	 * @see #setMessageConverter
	 * @see org.springframework.jms.support.converter.SimpleMessageConverter102
	 */
	protected void initDefaultStrategies() {
		setMessageConverter(new SimpleMessageConverter102());
	}

	/**
	 * This implementation overrides the superclass method to use JMS 1.0.2 API.
	 * <p>Uses the JMS pub-sub API if the given destination is a topic,
	 * else the JMS queue API.
	 */
	protected void sendResponse(Message response, Destination destination, Session session) throws JMSException {
		if (destination instanceof Topic) {
			TopicPublisher publisher = ((TopicSession) session).createPublisher((Topic) destination);
			try {
				postProcessProducer(publisher, response);
				publisher.publish(response);
			}
			finally {
				JmsUtils.closeMessageProducer(publisher);
			}
		}
		else {
			QueueSender sender = ((QueueSession) session).createSender((Queue) destination);
			try {
				postProcessProducer(sender, response);
				sender.send(response);
			}
			finally {
				JmsUtils.closeMessageProducer(sender);
			}
		}
	}

}
