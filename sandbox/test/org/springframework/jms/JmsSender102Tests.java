/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.JMSException;
import javax.jms.Message;
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


import org.easymock.MockControl;

/**
 * Unit test for the JmsSender using the JmsSender102 implementation.
 * 
 * 
 */
public class JmsSender102Tests extends JmsTestCase {

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

	/**
	 * Constructor for JmsSenderTests.
	 * @param name The name of the test.
	 */
	public JmsSender102Tests(String name) {
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
		queueConnectionFactoryControl =
			MockControl.createControl(QueueConnectionFactory.class);
		mockQueueConnectionFactory =
			(QueueConnectionFactory) this
				.queueConnectionFactoryControl
				.getMock();

		queueConnectionControl =
			MockControl.createControl(QueueConnection.class);
		mockQueueConnection =
			(QueueConnection) queueConnectionControl.getMock();

		queueControl = MockControl.createControl(Queue.class);
		mockQueue = (Queue) queueControl.getMock();

		queueSessionControl = MockControl.createControl(QueueSession.class);
		mockQueueSession = (QueueSession) this.queueSessionControl.getMock();

		mockQueueConnectionFactory.createQueueConnection();
		queueConnectionFactoryControl.setReturnValue(mockQueueConnection);
		queueConnectionFactoryControl.replay();

		mockQueueConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
		queueConnectionControl.setReturnValue(this.mockQueueSession);

		mockJndiContext.lookup("testQueue");
		mockJndiControl.setReturnValue(this.mockQueue);
	}

	private void createMockforTopics() throws JMSException, NamingException {
		topicConnectionFactoryControl =
			MockControl.createControl(TopicConnectionFactory.class);
		mockTopicConnectionFactory =
			(TopicConnectionFactory) this
				.topicConnectionFactoryControl
				.getMock();

		topicConnectionControl =
			MockControl.createControl(TopicConnection.class);
		mockTopicConnection =
			(TopicConnection) topicConnectionControl.getMock();

		topicControl = MockControl.createControl(Topic.class);
		mockTopic = (Topic) topicControl.getMock();

		topicSessionControl = MockControl.createControl(TopicSession.class);
		mockTopicSession = (TopicSession) topicSessionControl.getMock();

		//Specify behavior of the TopicConnectionFactory
		mockTopicConnectionFactory.createTopicConnection();
		topicConnectionFactoryControl.setReturnValue(mockTopicConnection);
		topicConnectionFactoryControl.replay();

		//Specify behavior of the TopicConnection
		mockTopicConnection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
		topicConnectionControl.setReturnValue(mockTopicSession);

		//Specify behavior of the JndiContext
		mockJndiContext.lookup("testTopic");
		mockJndiControl.setReturnValue(mockTopic);
	}

	/**
	 * Test the setting of the JmsSender Properites.
	 * @throws Exception
	 */
	public void testBeanProperties() throws Exception {
		JmsSender102 sender = new JmsSender102();
		sender.setConnectionFactory(mockQueueConnectionFactory);
		assertTrue(
			"connection factory ok",
			sender.getConnectionFactory() == mockQueueConnectionFactory);

		JmsSender102 s102 = new JmsSender102();
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. ConnectionFactory should be set");
		} catch (IllegalArgumentException e) {
			assertEquals(
				"Exception message not matching",
				"ConnectionFactory is required",
				e.getMessage());
		}

		//The default is for the JmsSender102 to send to queues.
		//Test to make sure exeception is thrown and has reasonable
		//message.
		s102 = new JmsSender102();
		s102.setConnectionFactory(mockTopicConnectionFactory);
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. Mismatch of Destination and ConnectionFactory types.");
		} catch (IllegalArgumentException e) {
			assertEquals(
				"Exception message not matching",
				"Specified a Spring JMS 1.0.2 Sender for queues but did not supply an instance of a QueueConnectionFactory",
				e.getMessage());
		}

		s102 = new JmsSender102();
		s102.setConnectionFactory(mockQueueConnectionFactory);
		s102.setPubSubDomain(true);
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. Mismatch of Destination and ConnectionFactory types.");
		} catch (IllegalArgumentException e) {
			assertEquals(
				"Exception message not matching",
				"Specified a Spring JMS 1.0.2 Sender for topics but did not supply an instance of a TopicConnectionFactory",
				e.getMessage());
		}
	}

	public void testSendQueue() throws Exception {
		JmsSender102 sender = new JmsSender102();
		sender.setConnectionFactory(mockQueueConnectionFactory);

		//Mock the javax.jms QueueSender
		MockControl queueSenderControl =
			MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender =
			(QueueSender) queueSenderControl.getMock();

		MockControl messageControl =
			MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		this.mockQueueConnection.close();
		this.queueConnectionControl.replay();

		this.mockQueueSession.createSender(this.mockQueue);
		this.queueSessionControl.setReturnValue(mockQueueSender);
		this.mockQueueSession.createTextMessage("just testing");
		this.queueSessionControl.setReturnValue(mockMessage);
		this.queueSessionControl.replay();

		mockQueueSender.send(mockQueue, mockMessage);
		queueSenderControl.replay();

		sender.send("testQueue", new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("just testing");
			}
		});
		
		this.queueConnectionFactoryControl.verify();
		this.queueConnectionControl.verify();
		queueSenderControl.verify();

		this.queueSessionControl.verify();

	}

	public void testSendTopic() throws Exception {

		//Setup the test
		JmsSender102 sender = new JmsSender102();
		sender.setConnectionFactory(mockTopicConnectionFactory);

		//Mock the javax.jms TopicPublisher
		MockControl topicPublisherControl =
			MockControl.createControl(TopicPublisher.class);
		TopicPublisher mockTopicPublisher =
			(TopicPublisher) topicPublisherControl.getMock();

		MockControl messageControl =
			MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		this.mockTopicConnection.close();
		this.topicConnectionControl.replay();

		this.mockTopicSession.createPublisher(this.mockTopic);
		this.topicSessionControl.setReturnValue(mockTopicPublisher);
		this.mockTopicSession.createTextMessage("just testing");
		this.topicSessionControl.setReturnValue(mockMessage);
		this.topicSessionControl.replay();

		mockTopicPublisher.publish(mockTopic, mockMessage);
		topicPublisherControl.replay();

		sender.setPubSubDomain(true);
		sender.send("testTopic", new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("just testing");
			}
		});

		this.topicConnectionFactoryControl.verify();
		this.topicConnectionControl.verify();
		this.topicSessionControl.verify();
		topicPublisherControl.verify();
	}

}
