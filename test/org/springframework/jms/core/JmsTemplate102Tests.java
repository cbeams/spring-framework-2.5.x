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

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiTemplate;

/**
 * Unit tests for the JmsTemplate implemented using JMS 1.0.2.
 * @author Andre Biryukov
 * @author Mark Pollack
 */
public class JmsTemplate102Tests extends TestCase {

	private Context mockJndiContext;
	private MockControl mockJndiControl;

	private MockControl queueConnectionFactoryControl;
	private QueueConnectionFactory mockQueueConnectionFactory;

	private MockControl queueConnectionControl;
	private QueueConnection mockQueueConnection;

	private MockControl queueSessionControl;
	private QueueSession mockQueueSession;

	private MockControl queueControl;
	private Queue mockQueue;

	private MockControl topicConnectionFactoryControl;
	private TopicConnectionFactory mockTopicConnectionFactory;

	private MockControl topicConnectionControl;
	private TopicConnection mockTopicConnection;

	private MockControl topicSessionControl;
	private TopicSession mockTopicSession;

	private MockControl topicControl;
	private Topic mockTopic;

	private int deliveryMode = DeliveryMode.PERSISTENT;
	private int priority = 9;
	private int timeToLive = 10000;

	/**
	 * Create the mock objects for testing.
	 */
	protected void setUp() throws Exception {
		mockJndiControl = MockControl.createControl(Context.class);
		mockJndiContext = (Context) this.mockJndiControl.getMock();

		createMockforQueues();
		createMockforTopics();

		mockJndiContext.close();
		mockJndiControl.replay();

	}

	private void createMockforQueues() throws JMSException, NamingException {
		queueConnectionFactoryControl = MockControl.createControl(QueueConnectionFactory.class);
		mockQueueConnectionFactory = (QueueConnectionFactory) queueConnectionFactoryControl.getMock();

		queueConnectionControl = MockControl.createControl(QueueConnection.class);
		mockQueueConnection = (QueueConnection) queueConnectionControl.getMock();

		queueControl = MockControl.createControl(Queue.class);
		mockQueue = (Queue) queueControl.getMock();

		queueSessionControl = MockControl.createControl(QueueSession.class);
		mockQueueSession = (QueueSession) queueSessionControl.getMock();

		mockQueueConnectionFactory.createQueueConnection();
		queueConnectionFactoryControl.setReturnValue(mockQueueConnection);
		queueConnectionFactoryControl.replay();

		// TODO tests with TX=true
		mockQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queueConnectionControl.setReturnValue(mockQueueSession);
		mockQueueSession.getTransacted();
		queueSessionControl.setReturnValue(false);

		mockJndiContext.lookup("testQueue");
		mockJndiControl.setReturnValue(mockQueue);
	}

	private void createMockforTopics() throws JMSException, NamingException {
		topicConnectionFactoryControl = MockControl.createControl(TopicConnectionFactory.class);
		mockTopicConnectionFactory = (TopicConnectionFactory) topicConnectionFactoryControl.getMock();

		topicConnectionControl = MockControl.createControl(TopicConnection.class);
		mockTopicConnection = (TopicConnection) topicConnectionControl.getMock();

		topicControl = MockControl.createControl(Topic.class);
		mockTopic = (Topic) topicControl.getMock();

		topicSessionControl = MockControl.createControl(TopicSession.class);
		mockTopicSession = (TopicSession) topicSessionControl.getMock();

		mockTopicConnectionFactory.createTopicConnection();
		topicConnectionFactoryControl.setReturnValue(mockTopicConnection);
		topicConnectionFactoryControl.replay();

		// TODO tests with TX =true
		mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		topicConnectionControl.setReturnValue(mockTopicSession);

		mockJndiContext.lookup("testTopic");
		mockJndiControl.setReturnValue(mockTopic);
	}

	public void testTopicSessionCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setPubSubDomain(true);
		sender.setConnectionFactory(mockTopicConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		mockTopicSession.getTransacted();
		topicSessionControl.setReturnValue(true);

		mockTopicSession.close();
		topicSessionControl.setVoidCallable(1);

		mockTopicConnection.close();
		topicConnectionControl.setVoidCallable(1);

		topicSessionControl.replay();
		topicConnectionControl.replay();

		sender.execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				boolean b = session.getTransacted();
				return null;
			}
		});

		topicConnectionFactoryControl.verify();
		topicConnectionControl.verify();
		topicSessionControl.verify();
	}

	/**
	 * Test the execute(ProducerCallback) using a topic.
	 */
	public void testTopicProducerCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setPubSubDomain(true);
		sender.setConnectionFactory(mockTopicConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		MockControl topicPublisherControl = MockControl.createControl(TopicPublisher.class);
		TopicPublisher mockTopicPublisher = (TopicPublisher) topicPublisherControl.getMock();

		mockTopicSession.createPublisher(null);
		topicSessionControl.setReturnValue(mockTopicPublisher);

		mockTopicSession.getTransacted();
		topicSessionControl.setReturnValue(true);

		mockTopicPublisher.getPriority();
		topicPublisherControl.setReturnValue(4);

		mockTopicSession.close();
		topicSessionControl.setVoidCallable(1);

		mockTopicConnection.close();
		topicConnectionControl.setVoidCallable(1);

		topicSessionControl.replay();
		topicConnectionControl.replay();

		sender.execute(new ProducerCallback() {
			public Object doInJms(Session session, MessageProducer msgProducer) throws JMSException {
				boolean b = session.getTransacted();
				int i = msgProducer.getPriority();
				return null;
			}
		});

		topicConnectionFactoryControl.verify();
		topicConnectionControl.verify();
		topicSessionControl.verify();
	}

	/**
	 * Test the method execute(SessionCallback action) with using the
	 * point to point domain as specified by the value of isPubSubDomain = false.
	 */
	public void testQueueSessionCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		// Point-to-Point (queues) are the default domain
		sender.setConnectionFactory(mockQueueConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		mockQueueSession.close();
		queueSessionControl.setVoidCallable(1);

		mockQueueConnection.close();
		queueConnectionControl.setVoidCallable(1);

		queueSessionControl.replay();
		queueConnectionControl.replay();

		sender.execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				boolean b = session.getTransacted();
				return null;
			}
		});

		queueConnectionFactoryControl.verify();
		queueConnectionControl.verify();
		queueSessionControl.verify();
	}

	/**
	 * Test the method execute(ProducerCallback) with a Queue.
	 */
	public void testQueueProducerCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		// Point-to-Point (queues) are the default domain.
		sender.setConnectionFactory(mockQueueConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		MockControl queueSenderControl = MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender = (QueueSender) queueSenderControl.getMock();

		mockQueueSession.createSender(null);
		queueSessionControl.setReturnValue(mockQueueSender);

		mockQueueSession.close();
		queueSessionControl.setVoidCallable(1);

		mockQueueSender.getPriority();
		queueSenderControl.setReturnValue(4);

		queueSenderControl.replay();

		mockQueueConnection.close();
		queueConnectionControl.setVoidCallable(1);

		queueSessionControl.replay();
		queueConnectionControl.replay();

		sender.execute(new ProducerCallback() {
			public Object doInJms(Session session, MessageProducer msgProducer)
			    throws JMSException {
				boolean b = session.getTransacted();
				int i = msgProducer.getPriority();
				return null;
			}
		});

		queueConnectionFactoryControl.verify();
		queueConnectionControl.verify();
		queueSessionControl.verify();
	}

	/**
	 * Test the setting of the JmsTemplate properties.
	 */
	public void testBeanProperties() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setConnectionFactory(mockQueueConnectionFactory);

		assertTrue("connection factory ok", sender.getConnectionFactory() == mockQueueConnectionFactory);

		JmsTemplate102 s102 = new JmsTemplate102();
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. ConnectionFactory should be set");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Exception message not matching", "connectionFactory is required", e.getMessage());
		}

		// The default is for the JmsTemplate102 to send to queues.
		// Test to make sure exeception is thrown and has reasonable message.
		s102 = new JmsTemplate102();
		s102.setConnectionFactory(mockTopicConnectionFactory);
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. Mismatch of Destination and ConnectionFactory types.");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		s102 = new JmsTemplate102();
		s102.setConnectionFactory(mockQueueConnectionFactory);
		s102.setPubSubDomain(true);
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. Mismatch of Destination and ConnectionFactory types.");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	/**
	 * Test the method send(String destination, MessgaeCreator c) using
	 * a queue and default QOS values.
	 */
	public void testSendStringQueue() throws Exception {
		sendQueue(true, false, false);
	}

	/**
	 * Test the method send(String destination, MessageCreator c) when
	 * explicit QOS parameters are enabled, using a queue.
	 */
	public void testSendStringQueueWithQOS() throws Exception {
		sendQueue(false, false, false);
	}

	/**
	 * Test the method send(MessageCreator c) using default QOS values.
	 */
	public void testSendDefaultDestinationQueue() throws Exception {
		sendQueue(true, false, true);
	}

	/**
	 * Test the method send(MessageCreator c) using explicit QOS values.
	 */
	public void testSendDefaultDestinationQueueWithQOS() throws Exception {
		sendQueue(false, false, true);
	}

	/**
	 * Test the method send(String destination, MessageCreator c) using
	 * a topic and default QOS values.
	 */
	public void testSendStringTopic() throws Exception {
		sendTopic(true, false);
	}

	/**
	 * Test the method send(String destination, MessageCreator c) using explicit
	 * QOS values.
	 */
	public void testSendStringTopicWithQOS() throws Exception {
		sendTopic(false, false);
	}

	/**
	 * Test the method send(Destination queue, MessgaeCreator c) using
	 * a queue and default QOS values.
	 */
	public void testSendQueue() throws Exception {
		sendQueue(true, false, false);
	}

	/**
	 * Test the method send(Destination queue, MessageCreator c) sing explicit
	 * QOS values.
	 */
	public void testSendQueueWithQOS() throws Exception {
		sendQueue(false, false, false);
	}

	/**
	 * Test the method send(Destination queue, MessgaeCreator c) using
	 * a topic and default QOS values.
	 */
	public void testSendTopic() throws Exception {
		sendTopic(true, false);
	}

	/**
	 * Test the method send(Destination queue, MessageCreator c) using explicity
	 * QOS values.
	 */
	public void testSendTopicWithQOS() throws Exception {
		sendQueue(false, false, false);
	}

	/**
	 * Common method for testing a send method that uses the MessageCreator
	 * callback but with different QOS options.
	 * @param ignoreQOS test using default QOS options.
	 */
	private void sendQueue(boolean ignoreQOS, boolean explicitQueue, boolean useDefaultDestination)
			throws Exception {

		JmsTemplate102 sender = new JmsTemplate102();
		setJndiTemplate(sender);
		sender.setConnectionFactory(mockQueueConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		if (useDefaultDestination) {
			sender.setDefaultDestination(mockQueue);
		}

		//Mock the javax.jms QueueSender
		MockControl queueSenderControl = MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender = (QueueSender) queueSenderControl.getMock();

		MockControl messageControl = MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		mockQueueConnection.close();
		queueConnectionControl.setVoidCallable(1);

		mockQueueSession.createSender(this.mockQueue);
		queueSessionControl.setReturnValue(mockQueueSender);
		mockQueueSession.createTextMessage("just testing");
		queueSessionControl.setReturnValue(mockMessage);

		mockQueueSession.close();
		queueSessionControl.setVoidCallable(1);

		queueConnectionControl.replay();
		queueSessionControl.replay();

		if (ignoreQOS) {
			mockQueueSender.send(mockMessage);
		}
		else {
			sender.setExplicitQosEnabled(true);
			sender.setDeliveryMode(deliveryMode);
			sender.setPriority(priority);
			sender.setTimeToLive(timeToLive);
			mockQueueSender.send(mockMessage, deliveryMode, priority, timeToLive);
		}
		queueSenderControl.replay();

		if (useDefaultDestination) {
			sender.send(new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createTextMessage("just testing");
				}
			});
		}
		else {
			if (explicitQueue) {
				sender.send(mockQueue, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage("just testing");
					}
				});
			}
			else {
				sender.send("testQueue", new MessageCreator() {
					public Message createMessage(Session session)
					    throws JMSException {
						return session.createTextMessage("just testing");
					}
				});
			}
		}

		queueConnectionFactoryControl.verify();
		queueConnectionControl.verify();
		queueSenderControl.verify();

		queueSessionControl.verify();
	}

	private void sendTopic(boolean ignoreQOS, boolean explicitTopic) throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setPubSubDomain(true);
		sender.setConnectionFactory(mockTopicConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		MockControl topicPublisherControl = MockControl.createControl(TopicPublisher.class);
		TopicPublisher mockTopicPublisher = (TopicPublisher) topicPublisherControl.getMock();

		MockControl messageControl = MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		mockTopicConnection.close();
		topicConnectionControl.setVoidCallable(1);

		mockTopicSession.getTransacted();
		topicSessionControl.setReturnValue(false);
		mockTopicSession.createPublisher(this.mockTopic);
		topicSessionControl.setReturnValue(mockTopicPublisher);
		mockTopicSession.createTextMessage("just testing");
		topicSessionControl.setReturnValue(mockMessage);

		mockTopicSession.close();
		topicSessionControl.setVoidCallable(1);

		topicConnectionControl.replay();
		topicSessionControl.replay();

		if (ignoreQOS) {
			mockTopicPublisher.publish(mockMessage);
		}
		else {
			sender.setExplicitQosEnabled(true);
			sender.setDeliveryMode(deliveryMode);
			sender.setPriority(priority);
			sender.setTimeToLive(timeToLive);
			mockTopicPublisher.publish(mockMessage, deliveryMode, priority, timeToLive);
		}
		topicPublisherControl.replay();

		sender.setPubSubDomain(true);

		if (explicitTopic) {
			sender.send(mockTopic, new MessageCreator() {
				public Message createMessage(Session session)
				    throws JMSException {
					return session.createTextMessage("just testing");
				}
			});
		}
		else {
			sender.send("testTopic", new MessageCreator() {
				public Message createMessage(Session session)
				    throws JMSException {
					return session.createTextMessage("just testing");
				}
			});
		}

		topicConnectionFactoryControl.verify();
		topicConnectionControl.verify();
		topicSessionControl.verify();
		topicPublisherControl.verify();
	}

	public void testConverter() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		setJndiTemplate(sender);
		sender.setConnectionFactory(mockQueueConnectionFactory);
		sender.setMessageConverter(new SimpleMessageConverter());
		String s = "Hello world";

		MockControl queueSenderControl = MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender = (QueueSender) queueSenderControl.getMock();

		MockControl messageControl = MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		mockQueueConnection.close();
		queueConnectionControl.setVoidCallable(1);

		mockQueueSession.createSender(this.mockQueue);
		queueSessionControl.setReturnValue(mockQueueSender);
		mockQueueSession.createTextMessage("Hello world");
		queueSessionControl.setReturnValue(mockMessage);

		mockQueueSession.close();
		queueSessionControl.setVoidCallable(1);

		queueConnectionControl.replay();
		queueSessionControl.replay();

		mockQueueSender.send(mockMessage);
		queueSenderControl.replay();

		sender.convertAndSend(mockQueue, s);

		queueConnectionFactoryControl.verify();
		queueConnectionControl.verify();
		queueSenderControl.verify();

		queueSessionControl.verify();
	}

	public void testReceiveDefaultDestination() throws Exception {
		doTestReceive(false, true, false, false);
	}
	
	public void testReceiveDestination() throws Exception {
		doTestReceive(true, false, false, false);
	}

	public void testReceiveDestinationWithClientAcknowledge() throws Exception {
		doTestReceive(true, false, false, true);
	}

	public void testReceiveStringDestination() throws Exception {
		doTestReceive(false, false, false, false);
	}

	public void testReceiveAndConvertDefaultDestination() throws Exception {
		doTestReceive(false, true, true, false);
	}

	public void testReceiveAndConvertStringDestination() throws Exception {
		doTestReceive(false, false, true, false);
	}

	public void testReceiveAndConvertDestination() throws Exception {
		doTestReceive(true, false, true, false);
	}
	
	private void doTestReceive(boolean explicitDestination, boolean useDefaultDestination,
			boolean testConverter, boolean clientAcknowledge) throws Exception {

		JmsTemplate102 sender = new JmsTemplate102();
		sender.setConnectionFactory(mockQueueConnectionFactory);
		//Override the default settings for client ack used in the testSetup - createMockForQueues()
		//Can't use Session.getAcknowledgeMode()
		queueConnectionControl.reset();
		if (clientAcknowledge) {
			sender.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
			mockQueueConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);			
		} else {
			sender.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
			mockQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		}
		queueConnectionControl.setReturnValue(mockQueueSession);
		setJndiTemplate(sender);


		if (useDefaultDestination) {
			sender.setDefaultDestination(mockQueue);
		}

		mockQueueConnection.start();
		queueConnectionControl.setVoidCallable(1);
		mockQueueConnection.close();
		queueConnectionControl.setVoidCallable(1);

		MockControl queueReceiverControl = MockControl.createControl(QueueReceiver.class);
		QueueReceiver mockQueueReceiver = (QueueReceiver) queueReceiverControl.getMock();

		mockQueueSession.createReceiver(mockQueue);
		queueSessionControl.setReturnValue(mockQueueReceiver);

		mockQueueSession.close();
		queueSessionControl.setVoidCallable(1);

		MockControl messageControl = MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		if (testConverter) {
			mockMessage.getText();
			messageControl.setReturnValue("Hello World!");
		}
		if (clientAcknowledge) {
			mockMessage.acknowledge();
			messageControl.setVoidCallable(1);
		}

		queueSessionControl.replay();
		queueConnectionControl.replay();
		messageControl.replay();

		mockQueueReceiver.receive();
		queueReceiverControl.setReturnValue(mockMessage);
		mockQueueReceiver.close();
		queueReceiverControl.setVoidCallable(1);
		queueReceiverControl.replay();

		Message m = null;
		String textFromMessage = null;
		if (useDefaultDestination) {
			if (testConverter) {
				textFromMessage = (String) sender.receiveAndConvert();
			}
			else {
				m = sender.receive();
			}
		}
		else {
			if (explicitDestination) {
				if (testConverter) {
					textFromMessage = (String) sender.receiveAndConvert(mockQueue);
				}
				else {
					m = sender.receive(mockQueue);
				}
			}
			else {
				if (testConverter) {
					textFromMessage = (String) sender.receiveAndConvert("testQueue");
				}
				else {
					m = sender.receive("testQueue");
				}
			}
		}

		queueConnectionFactoryControl.verify();
		queueConnectionControl.verify();
		queueSessionControl.verify();
		queueReceiverControl.verify();
		messageControl.verify();

		if (testConverter) {
			assertEquals("Message Text should be equal", "Hello World!", textFromMessage);
		}
		else {
			assertEquals("Messages should refer to the same object", m, mockMessage);
		}
	}
	private void setJndiTemplate(JmsTemplate sender) {
		JndiDestinationResolver destMan = new JndiDestinationResolver();
		destMan.setJndiTemplate(new JndiTemplate() {
			protected Context createInitialContext() throws NamingException {
				return mockJndiContext;
			}
		});
		sender.setDestinationResolver(destMan);
	}

}
