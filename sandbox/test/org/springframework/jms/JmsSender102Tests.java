/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
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
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.easymock.MockControl;

import junit.framework.TestCase;

/**
 * Unit test for the JmsSender using the JmsSender102 implementation.
 * 
 * 
 */
public class JmsSender102Tests extends TestCase {

	private static Context mockJndiContext;
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

	static {
		try {
			NamingManager
				.setInitialContextFactoryBuilder(
					new InitialContextFactoryBuilder() {
				public InitialContextFactory createInitialContextFactory(Hashtable hashtable)
					throws NamingException {
					return new InitialContextFactory() {
						public Context getInitialContext(Hashtable hashtable)
							throws NamingException {
							return mockJndiContext;
						}
					};
				}
			});
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

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

		mockTopicConnectionFactory.createTopicConnection();
		topicConnectionFactoryControl.setReturnValue(mockTopicConnection);
		topicConnectionFactoryControl.replay();

		mockTopicConnection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
		topicConnectionControl.setReturnValue(mockTopicSession);

		mockJndiContext.lookup("testTopic");
		mockJndiControl.setReturnValue(mockTopic);
	}

	/**
	 * Test the setting of the JmsSender Properites.
	 * @throws Exception
	 */
	public void testBeanProperties() throws Exception {
		JmsSender sender = new JmsSender102(mockQueueConnectionFactory);
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
		s102 = new JmsSender102(mockTopicConnectionFactory);
		try {
			s102.afterPropertiesSet();
			fail("IllegalArgumentException not thrown. Mismatch of Destination and ConnectionFactory types.");
		} catch (IllegalArgumentException e) {
			assertEquals(
				"Exception message not matching",
				"Specified a Spring JMS 1.0.2 Sender for queues but did not supply an instance of a QueueConnectionFactory",
				e.getMessage());
		}

		s102 = new JmsSender102(mockQueueConnectionFactory);
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
		JmsSender sender = new JmsSender102(mockQueueConnectionFactory);

		MockControl queueSenderControl = MockControl.createControl(QueueSender.class);
		QueueSender mockQueueSender = (QueueSender)queueSenderControl.getMock();

		MockControl messageControl = MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage)messageControl.getMock();

		this.mockQueueConnection.close();
		this.queueConnectionControl.replay();
		
		this.mockQueueSession.createSender(this.mockQueue);
		this.queueSessionControl.setReturnValue(mockQueueSender);
		this.mockQueueSession.createTextMessage("just testing");
		this.queueSessionControl.setReturnValue(mockMessage);
		this.queueSessionControl.replay();
		
		mockQueueSender.send(mockQueue, mockMessage);
		queueSenderControl.replay();

		sender.send("testQueue", new SimpleMessageCreator());
		
		queueSenderControl.verify();
		
		this.queueSessionControl.verify();

	}

	/**
	 * Inner class that implements the MessageCreator interface to
	 * create a simple text message
	 */
	class SimpleMessageCreator implements MessageCreator {
		public Message createMessage(Session session) throws JMSException {
			return session.createTextMessage("just testing");
		}

	}

}
