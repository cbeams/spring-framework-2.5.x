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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jms.JmsException;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiTemplate;

/**
 * Unit tests for the JmsTemplate implemented using JMS 1.1.
 * @author Andre Biryukov
 * @author Mark Pollack
 */
public class JmsTemplateTests extends TestCase {

	private Context mockJndiContext;
	private MockControl mockJndiControl;

	private MockControl connectionFactoryControl;
	private ConnectionFactory mockConnectionFactory;

	private MockControl connectionControl;
	private Connection mockConnection;

	private MockControl sessionControl;
	private Session mockSession;

	private MockControl queueControl;
	private Queue mockQueue;

	private int deliveryMode = DeliveryMode.PERSISTENT;
	private int priority = 9;
	private int timeToLive = 10000;

	/**
	 * Create the mock objects for testing.
	 */
	protected void setUp() throws Exception {
		mockJndiControl = MockControl.createControl(Context.class);
		mockJndiContext = (Context) this.mockJndiControl.getMock();

		createMockforDestination();

		mockJndiContext.close();
		mockJndiControl.replay();
	}

	private void createMockforDestination()
			throws JMSException, NamingException {
		connectionFactoryControl =
				MockControl.createControl(ConnectionFactory.class);
		mockConnectionFactory =
				(ConnectionFactory) connectionFactoryControl.getMock();

		connectionControl = MockControl.createControl(Connection.class);
		mockConnection = (Connection) connectionControl.getMock();

		sessionControl = MockControl.createControl(Session.class);
		mockSession = (Session) sessionControl.getMock();

		queueControl = MockControl.createControl(Queue.class);
		mockQueue = (Queue) queueControl.getMock();

		mockConnectionFactory.createConnection();
		connectionFactoryControl.setReturnValue(mockConnection);
		connectionFactoryControl.replay();

		//TODO tests with TX=true
		mockConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		connectionControl.setReturnValue(mockSession);
		mockSession.getTransacted();
		sessionControl.setReturnValue(false);

		mockJndiContext.lookup("testDestination");
		mockJndiControl.setReturnValue(mockQueue);
	}

	public void testExceptionStackTrace() {
		JMSException jmsEx = new JMSException("could not connect");
		Exception innerEx = new Exception("host not found");
		jmsEx.setLinkedException(innerEx);
		JmsException springJmsEx = JmsUtils.convertJmsAccessException(jmsEx);
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		springJmsEx.printStackTrace(out);
		String trace = sw.toString();
		//System.out.println("trace = " + trace);
		assertTrue("inner jms exception not found", trace.indexOf("host not found") > 0);
		
	}
	
	public void testProducerCallback() throws Exception {
		JmsTemplate sender = new JmsTemplate();
		sender.setConnectionFactory(mockConnectionFactory);
		setJndiTemplate(sender);

		MockControl messageProducerControl = MockControl.createControl(MessageProducer.class);
		MessageProducer mockMessageProducer = (MessageProducer) messageProducerControl.getMock();

		mockSession.createProducer(null);
		sessionControl.setReturnValue(mockMessageProducer);

		mockMessageProducer.getPriority();
		messageProducerControl.setReturnValue(4);

		messageProducerControl.replay();

		mockSession.close();
		sessionControl.setVoidCallable(1);

		mockConnection.close();
		connectionControl.setVoidCallable(1);

		sessionControl.replay();
		connectionControl.replay();

		sender.execute(new ProducerCallback() {
			public Object doInJms(Session session, MessageProducer msgProducer) throws JMSException {
				boolean b = session.getTransacted();
				int i = msgProducer.getPriority();
				return null;
			}
		});

		connectionFactoryControl.verify();
		connectionControl.verify();
		sessionControl.verify();
	}

	/**
	 * Test the method execute(SessionCallback action).
	 */
	public void testSessionCallback() throws Exception {
		JmsTemplate sender = new JmsTemplate();
		sender.setConnectionFactory(mockConnectionFactory);
		setJndiTemplate(sender);

		mockSession.close();
		sessionControl.setVoidCallable(1);

		mockConnection.close();
		connectionControl.setVoidCallable(1);

		sessionControl.replay();
		connectionControl.replay();

		sender.execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				boolean b = session.getTransacted();
				return null;
			}
		});

		connectionFactoryControl.verify();
		connectionControl.verify();
		sessionControl.verify();
	}

	/**
	 * Test sending to a destination using the method
	 * send(Destination d, MessageCreator messageCreator)
	 */
	public void testSendDestination() throws Exception {
		doTestSendDestination(true, true, false);
	}

	/**
	 * Test sending to a destination using the method
	 * send(Destination d, MessageCreator messageCreator) using QOS parameters.
	 */
	public void testSendDestinationWithQOS() throws Exception {
		doTestSendDestination(false, true, false);
	}

	/**
	 * Test seding to a destination using the method
	 * send(String d, MessageCreator messageCreator)
	 */
	public void testSendStringDestination() throws Exception {
		doTestSendDestination(true, false, false);
	}

	/**
	 * Test sending to a destination using the method
	 * send(String d, MessageCreator messageCreator) using QOS parameters.
	 */
	public void testSendStringDestinationWithQOS() throws Exception {
		doTestSendDestination(false, false, false);
	}

	/**
	 * Test sending to the default destination.
	 */
	public void testSendDefaultDestination() throws Exception {
		doTestSendDestination(true, true, true);
	}

	/**
	 * Test sending to the default destination using explicit QOS parameters.
	 */
	public void testSendDefaultDestinationWithQOS() throws Exception {
		doTestSendDestination(false, true, true);
	}

	/**
	 * Common method for testing a send method that uses the MessageCreator
	 * callback but with different QOS options.
	 * @param ignoreQOS test using default QOS options.
	 */
	private void doTestSendDestination(boolean ignoreQOS, boolean explicitDestination,
			boolean useDefaultDestination) throws Exception {

		JmsTemplate sender = new JmsTemplate();
		sender.setConnectionFactory(mockConnectionFactory);
		setJndiTemplate(sender);
		if (useDefaultDestination) {
			sender.setDefaultDestination(mockQueue);
		}

		MockControl messageProducerControl = MockControl.createControl(MessageProducer.class);
		MessageProducer mockMessageProducer = (MessageProducer) messageProducerControl.getMock();

		MockControl messageControl = MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		mockSession.close();
		sessionControl.setVoidCallable(1);

		mockConnection.close();
		connectionControl.setVoidCallable(1);

		mockSession.createProducer(mockQueue);
		sessionControl.setReturnValue(mockMessageProducer);
		mockSession.createTextMessage("just testing");
		sessionControl.setReturnValue(mockMessage);

		sessionControl.replay();
		connectionControl.replay();

		if (ignoreQOS) {
			mockMessageProducer.send(mockMessage);
		}
		else {
			sender.setExplicitQosEnabled(true);
			sender.setDeliveryMode(deliveryMode);
			sender.setPriority(priority);
			sender.setTimeToLive(timeToLive);
			mockMessageProducer.send(mockMessage, deliveryMode, priority, timeToLive);
		}

		messageProducerControl.replay();

		if (useDefaultDestination) {
			sender.send(new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createTextMessage("just testing");
				}
			});
		}
		else {
			if (explicitDestination) {
				sender.send(mockQueue, new MessageCreator() {
					public Message createMessage(Session session)
							throws JMSException {
						return session.createTextMessage("just testing");
					}
				});
			}
			else {
				sender.send("testDestination", new MessageCreator() {
					public Message createMessage(Session session)
							throws JMSException {
						return session.createTextMessage("just testing");
					}
				});
			}
		}

		connectionFactoryControl.verify();
		connectionControl.verify();
		messageProducerControl.verify();

		sessionControl.verify();

	}

	public void testConverter() throws Exception {
		JmsTemplate sender = new JmsTemplate();
		sender.setConnectionFactory(mockConnectionFactory);
		setJndiTemplate(sender);
		sender.setMessageConverter(new SimpleMessageConverter());
		String s = "Hello world";

		MockControl messageProducerControl = MockControl.createControl(MessageProducer.class);
		MessageProducer mockMessageProducer = (MessageProducer) messageProducerControl.getMock();

		MockControl messageControl = MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		mockSession.close();
		sessionControl.setVoidCallable(1);

		mockConnection.close();
		connectionControl.setVoidCallable(1);

		mockSession.createProducer(mockQueue);
		sessionControl.setReturnValue(mockMessageProducer);
		mockSession.createTextMessage("Hello world");
		sessionControl.setReturnValue(mockMessage);

		sessionControl.replay();
		connectionControl.replay();

		mockMessageProducer.send(mockMessage);

		messageProducerControl.replay();

		sender.convertAndSend(mockQueue, s);

		connectionFactoryControl.verify();
		connectionControl.verify();
		messageProducerControl.verify();

		sessionControl.verify();
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

		JmsTemplate sender = new JmsTemplate();
		sender.setConnectionFactory(mockConnectionFactory);
		setJndiTemplate(sender);

		if (useDefaultDestination) {
			sender.setDefaultDestination(mockQueue);
		}

		mockConnection.start();
		connectionControl.setVoidCallable(1);
		mockConnection.close();
		connectionControl.setVoidCallable(1);

		MockControl messageConsumerControl = MockControl.createControl(MessageConsumer.class);
		MessageConsumer mockMessageConsumer = (MessageConsumer) messageConsumerControl.getMock();

		mockSession.createConsumer(mockQueue);
		sessionControl.setReturnValue(mockMessageConsumer);
		mockSession.getAcknowledgeMode();
		if (clientAcknowledge) {
			sessionControl.setReturnValue(Session.CLIENT_ACKNOWLEDGE);
		}
		else {
			sessionControl.setReturnValue(Session.AUTO_ACKNOWLEDGE);
		}
		mockSession.close();
		sessionControl.setVoidCallable(1);

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

		sessionControl.replay();
		connectionControl.replay();
		messageControl.replay();

		mockMessageConsumer.receive();
		messageConsumerControl.setReturnValue(mockMessage);
		mockMessageConsumer.close();
		messageConsumerControl.setVoidCallable(1);
		messageConsumerControl.replay();

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
					textFromMessage = (String) sender.receiveAndConvert("testDestination");
				}
				else {
					m = sender.receive("testDestination");
				}
			}
		}

		connectionFactoryControl.verify();
		connectionControl.verify();
		sessionControl.verify();
		messageConsumerControl.verify();
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
