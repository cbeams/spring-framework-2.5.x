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

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
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

import org.springframework.jms.core.DefaultJmsAdmin;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.JmsTemplate102;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.ProducerCallback;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jndi.JndiTemplate;

/**
 * Unit tests for the JmsTemplate implemented using JMS 1.0.2.
 * @author Andre Biryukov
 * @author Mark Pollack
 */
public class JmsTemplate102Tests extends TestCase {

	private Context mockJndiContext;
	private MockControl mockJndiControl;

	private MockControl _queueConnectionFactoryControl;
	private QueueConnectionFactory _mockQueueConnectionFactory;

	private MockControl _queueConnectionControl;
	private QueueConnection _mockQueueConnection;

	private MockControl _queueSessionControl;
	private QueueSession _mockQueueSession;

	private MockControl _queueControl;
	private Queue _mockQueue;

	private MockControl _topicConnectionFactoryControl;
	private TopicConnectionFactory _mockTopicConnectionFactory;

	private MockControl _topicConnectionControl;
	private TopicConnection _mockTopicConnection;

	private MockControl _topicSessionControl;
	private TopicSession _mockTopicSession;

	private MockControl _topicControl;
	private Topic _mockTopic;

	private int _deliveryMode = DeliveryMode.PERSISTENT;
	private int _priority = 9;
	private int _timeToLive = 10000;

	/**
	 * Constructor for JmsSenderTests.
	 * @param name The name of the test.
	 */
	public JmsTemplate102Tests(String name) {
		super(name);
	}

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
		_queueConnectionFactoryControl =
		    MockControl.createControl(QueueConnectionFactory.class);
		_mockQueueConnectionFactory =
		    (QueueConnectionFactory) this
		    ._queueConnectionFactoryControl
		    .getMock();

		_queueConnectionControl =
		    MockControl.createControl(QueueConnection.class);
		_mockQueueConnection =
		    (QueueConnection) _queueConnectionControl.getMock();

		_queueControl = MockControl.createControl(Queue.class);
		_mockQueue = (Queue) _queueControl.getMock();

		_queueSessionControl = MockControl.createControl(QueueSession.class);
		_mockQueueSession = (QueueSession) _queueSessionControl.getMock();

		_mockQueueConnectionFactory.createQueueConnection();
		_queueConnectionFactoryControl.setReturnValue(_mockQueueConnection);
		_queueConnectionFactoryControl.replay();

		//		TODO tests with TX = true
		_mockQueueConnection.createQueueSession(
		    false,
		    Session.AUTO_ACKNOWLEDGE);
		_queueConnectionControl.setReturnValue(_mockQueueSession);

		mockJndiContext.lookup("testQueue");
		mockJndiControl.setReturnValue(_mockQueue);
	}

	private void createMockforTopics() throws JMSException, NamingException {
		_topicConnectionFactoryControl =
		    MockControl.createControl(TopicConnectionFactory.class);
		_mockTopicConnectionFactory =
		    (TopicConnectionFactory) this
		    ._topicConnectionFactoryControl
		    .getMock();

		_topicConnectionControl =
		    MockControl.createControl(TopicConnection.class);
		_mockTopicConnection =
		    (TopicConnection) _topicConnectionControl.getMock();

		_topicControl = MockControl.createControl(Topic.class);
		_mockTopic = (Topic) _topicControl.getMock();

		_topicSessionControl = MockControl.createControl(TopicSession.class);
		_mockTopicSession = (TopicSession) _topicSessionControl.getMock();

		//Specify behavior of the TopicConnectionFactory
		_mockTopicConnectionFactory.createTopicConnection();
		_topicConnectionFactoryControl.setReturnValue(_mockTopicConnection);
		_topicConnectionFactoryControl.replay();

		//Specify behavior of the TopicConnection
		//		TODO tests with TX = true
		_mockTopicConnection.createTopicSession(
		    false,
		    Session.AUTO_ACKNOWLEDGE);
		_topicConnectionControl.setReturnValue(_mockTopicSession);

		//Specify behavior of the JndiContext
		mockJndiContext.lookup("testTopic");
		mockJndiControl.setReturnValue(_mockTopic);
	}

	public void testTopicSessionCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setPubSubDomain(true);
		sender.setConnectionFactory(_mockTopicConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		//Session behavior
		_mockTopicSession.getTransacted();
		_topicSessionControl.setReturnValue(true);
		_topicSessionControl.replay();

		//connection behavior
		_mockTopicConnection.close();
		_topicConnectionControl.replay();

		sender.execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				boolean b = session.getTransacted();
				return null;
			}
		});

		_topicConnectionFactoryControl.verify();
		_topicConnectionControl.verify();
		_topicSessionControl.verify();
	}

	private void setJndiTemplate(JmsTemplate sender) {
		((DefaultJmsAdmin) sender.getJmsAdmin()).setJndiTemplate(new JndiTemplate() {
			protected Context createInitialContext() throws NamingException {
				return mockJndiContext;
			}
		});
	}

	/**
	 * Test the execute(ProducerCallback) using a topic
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testTopicJmsSenderCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setPubSubDomain(true);
		sender.setConnectionFactory(_mockTopicConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		//Mock the javax.jms TopicPublisher
		MockControl topicPublisherControl =
		    MockControl.createControl(TopicPublisher.class);
		TopicPublisher mockTopicPublisher =
		    (TopicPublisher) topicPublisherControl.getMock();

		_mockTopicSession.createPublisher(null);
		_topicSessionControl.setReturnValue(mockTopicPublisher);

		_queueSessionControl.replay();

		//Session behavior
		_mockTopicSession.getTransacted();
		_topicSessionControl.setReturnValue(true);
		_topicSessionControl.replay();

		mockTopicPublisher.getPriority();
		topicPublisherControl.setReturnValue(4);

		//connection behavior
		_mockTopicConnection.close();
		_topicConnectionControl.replay();

		sender.execute(new ProducerCallback() {
			public Object doInJms(Session session, MessageProducer msgProducer)
			    throws JMSException {
				boolean b = session.getTransacted();
				int i = msgProducer.getPriority();
				return null;
			}
		});

		_topicConnectionFactoryControl.verify();
		_topicConnectionControl.verify();
		_topicSessionControl.verify();
	}

	/**
	 * Test the method execute(SessionCallback action) with using the
	 * point to point domain as specified by the value of isPubSubDomain = false.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testQueueSessionCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		//Point to Point (queues) are the default domain.
		sender.setConnectionFactory(_mockQueueConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		//Session behavior
		_mockQueueSession.getTransacted();
		_queueSessionControl.setReturnValue(true);
		_queueSessionControl.replay();

		//connection behavior
		_mockQueueConnection.close();
		_queueConnectionControl.replay();

		sender.execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				boolean b = session.getTransacted();
				return null;
			}
		});

		_queueConnectionFactoryControl.verify();
		_queueConnectionControl.verify();
		_queueSessionControl.verify();

	}

	/**
	 * Test the method execute(ProducerCallback) with a Queue.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testQueueJmsSenderCallback() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		//Point to Point (queues) are the default domain.
		sender.setConnectionFactory(_mockQueueConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		//Session behavior
		_mockQueueSession.getTransacted();
		_queueSessionControl.setReturnValue(true);

		//Mock the javax.jms QueueSender
		MockControl queueSenderControl =
		    MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender =
		    (QueueSender) queueSenderControl.getMock();

		_mockQueueSession.createSender(null);
		_queueSessionControl.setReturnValue(mockQueueSender);

		_queueSessionControl.replay();

		mockQueueSender.getPriority();
		queueSenderControl.setReturnValue(4);

		queueSenderControl.replay();

		//additional connection behavior
		_mockQueueConnection.close();
		_queueConnectionControl.replay();

		sender.execute(new ProducerCallback() {
			public Object doInJms(Session session, MessageProducer msgProducer)
			    throws JMSException {
				boolean b = session.getTransacted();
				int i = msgProducer.getPriority();
				return null;
			}
		});

		_queueConnectionFactoryControl.verify();
		_queueConnectionControl.verify();
		_queueSessionControl.verify();
	}

	/**
	 * Test the setting of the JmsTemplate Properites.
	 * @throws Exception
	 */
	public void testBeanProperties() throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setConnectionFactory(_mockQueueConnectionFactory);

		assertTrue(
		    "connection factory ok",
		    sender.getConnectionFactory() == _mockQueueConnectionFactory);

		JmsTemplate102 s102 = new JmsTemplate102();
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. ConnectionFactory should be set");
		}
		catch (IllegalArgumentException e) {
			assertEquals(
			    "Exception message not matching",
			    "connectionFactory is required",
			    e.getMessage());
		}

		//The default is for the JmsTemplate102 to send to queues.
		//Test to make sure exeception is thrown and has reasonable
		//message.
		s102 = new JmsTemplate102();
		s102.setConnectionFactory(_mockTopicConnectionFactory);
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. Mismatch of Destination and ConnectionFactory types.");
		}
		catch (IllegalArgumentException e) {
			assertEquals(
			    "Exception message not matching",
			    "Specified a Spring JMS 1.0.2 Sender for queues but did not supply an instance of a QueueConnectionFactory",
			    e.getMessage());
		}

		s102 = new JmsTemplate102();
		s102.setConnectionFactory(_mockQueueConnectionFactory);
		s102.setPubSubDomain(true);
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. Mismatch of Destination and ConnectionFactory types.");
		}
		catch (IllegalArgumentException e) {
			assertEquals(
			    "Exception message not matching",
			    "Specified a Spring JMS 1.0.2 Sender for topics but did not supply an instance of a TopicConnectionFactory",
			    e.getMessage());
		}
	}

	/**
	 * Test the method send(String destination, MessgaeCreator c) using
	 * a queue and default QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendStringQueue() throws Exception {
		sendQueue(true, false, false);
	}

	/**
	 * Test the method send(String destination, MessageCreator c) when
	 * explicit QOS parameters are enabled, using a queue.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendStringQueueWithQOS() throws Exception {
		sendQueue(false, false, false);
	}

	/**
	 * Test the method send(MessageCreator c) using default QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendDefaultDestinationQueue() throws Exception {
		sendQueue(true, false, true);
	}

	/**
	 * Test the method send(MessageCreator c) using explicit QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendDefaultDestinationQueueWithQOS() throws Exception {
		sendQueue(false, false, true);
	}

	/**
	 * Test the method send(String destination, MessageCreator c) using
	 * a topic and default QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendStringTopic() throws Exception {
		sendTopic(true, false);
	}

	/**
	 * Test the method send(String destination, MessageCreator c) using explicit
	 * QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendStringTopicWithQOS() throws Exception {
		sendTopic(false, false);
	}

	/**
	 * Test the method send(Destination queue, MessgaeCreator c) using
	 * a queue and default QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendQueue() throws Exception {
		sendQueue(true, false, false);
	}

	/**
	 * Test the method send(Destination queue, MessageCreator c) sing explicit
	 * QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendQueueWithQOS() throws Exception {
		sendQueue(false, false, false);
	}

	/**
	 * Test the method send(Destination queue, MessgaeCreator c) using
	 * a topic and default QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendTopic() throws Exception {
		sendTopic(true, false);
	}

	/**
	 * Test the method send(Destination queue, MessageCreator c) using explicity
	 * QOS values.
	 * @throws Exception unexpected, let JUnit handle it.
	 */
	public void testSendTopicWithQOS() throws Exception {
		sendQueue(false, false, false);
	}

	/**
	 * Common method for testing a send method that uses the MessageCreator
	 * callback but with different QOS options.
	 * @param ignoreQOS test using default QOS options.
	 * @throws Exception unexpected, let junit handle it.
	 */
	private void sendQueue(
	    boolean ignoreQOS,
	    boolean explicitQueue,
	    boolean useDefaultDestination)
	    throws Exception {
		JmsTemplate102 sender = new JmsTemplate102();
		setJndiTemplate(sender);
		sender.setConnectionFactory(_mockQueueConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();


		if (useDefaultDestination) {
			sender.setDefaultDestination(_mockQueue);
		}

		//Mock the javax.jms QueueSender
		MockControl queueSenderControl =
		    MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender =
		    (QueueSender) queueSenderControl.getMock();

		MockControl messageControl =
		    MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		_mockQueueConnection.close();
		_queueConnectionControl.replay();

		_mockQueueSession.createSender(this._mockQueue);
		_queueSessionControl.setReturnValue(mockQueueSender);
		_mockQueueSession.createTextMessage("just testing");
		_queueSessionControl.setReturnValue(mockMessage);
		_queueSessionControl.replay();

		if (ignoreQOS) {
			mockQueueSender.send(mockMessage);
		}
		else {
			sender.setExplicitQosEnabled(true);
			sender.setDeliveryMode(_deliveryMode);
			sender.setPriority(_priority);
			sender.setTimeToLive(_timeToLive);
			mockQueueSender.send(
			    mockMessage,
			    _deliveryMode,
			    _priority,
			    _timeToLive);
		}
		queueSenderControl.replay();

		if (useDefaultDestination) {
			sender.send(new MessageCreator() {
				public Message createMessage(Session session)
				    throws JMSException {
					return session.createTextMessage("just testing");
				}
			});
		}
		else {
			if (explicitQueue) {
				sender.send(_mockQueue, new MessageCreator() {
					public Message createMessage(Session session)
					    throws JMSException {
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

		_queueConnectionFactoryControl.verify();
		_queueConnectionControl.verify();
		queueSenderControl.verify();

		_queueSessionControl.verify();

	}

	private void sendTopic(boolean ignoreQOS, boolean explicitTopic)
	    throws Exception {

		//Setup the test
		JmsTemplate102 sender = new JmsTemplate102();
		sender.setPubSubDomain(true);
		sender.setConnectionFactory(_mockTopicConnectionFactory);
		setJndiTemplate(sender);
		sender.afterPropertiesSet();

		//Mock the javax.jms TopicPublisher
		MockControl topicPublisherControl =
		    MockControl.createControl(TopicPublisher.class);
		TopicPublisher mockTopicPublisher =
		    (TopicPublisher) topicPublisherControl.getMock();

		MockControl messageControl =
		    MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		_mockTopicConnection.close();
		_topicConnectionControl.replay();

		_mockTopicSession.createPublisher(this._mockTopic);
		_topicSessionControl.setReturnValue(mockTopicPublisher);
		_mockTopicSession.createTextMessage("just testing");
		_topicSessionControl.setReturnValue(mockMessage);
		_topicSessionControl.replay();

		if (ignoreQOS) {
			mockTopicPublisher.publish(mockMessage);
		}
		else {
			sender.setExplicitQosEnabled(true);
			sender.setDeliveryMode(_deliveryMode);
			sender.setPriority(_priority);
			sender.setTimeToLive(_timeToLive);
			mockTopicPublisher.publish(
			    mockMessage,
			    _deliveryMode,
			    _priority,
			    _timeToLive);
		}
		topicPublisherControl.replay();

		sender.setPubSubDomain(true);

		if (explicitTopic) {
			sender.send(_mockTopic, new MessageCreator() {
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

		_topicConnectionFactoryControl.verify();
		_topicConnectionControl.verify();
		_topicSessionControl.verify();
		topicPublisherControl.verify();
	}

	public void testConverter() throws Exception {

		JmsTemplate102 sender = new JmsTemplate102();
		setJndiTemplate(sender);
		sender.setConnectionFactory(_mockQueueConnectionFactory);
		sender.setConverter(new ToStringConverter());
		String s = "Hello world";

		//Mock the javax.jms QueueSender
		MockControl queueSenderControl =
		    MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender =
		    (QueueSender) queueSenderControl.getMock();

		MockControl messageControl =
		    MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		_mockQueueConnection.close();
		_queueConnectionControl.replay();

		_mockQueueSession.createSender(this._mockQueue);
		_queueSessionControl.setReturnValue(mockQueueSender);
		_mockQueueSession.createTextMessage("Hello world");
		_queueSessionControl.setReturnValue(mockMessage);
		_queueSessionControl.replay();

		mockQueueSender.send(mockMessage);
		queueSenderControl.replay();

		sender.send(_mockQueue, s);

		_queueConnectionFactoryControl.verify();
		_queueConnectionControl.verify();
		queueSenderControl.verify();

		_queueSessionControl.verify();


	}


}
