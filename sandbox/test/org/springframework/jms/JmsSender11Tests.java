/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;


import org.easymock.MockControl;
import org.springframework.jms.support.DefaultJmsAdmin;


/**
 * Unit test for the JmsSender implemented using JMS 1.1
 * 
 * @author Andre Biryukov
 * @author Mark Pollack
 */
public class JmsSender11Tests extends JmsTestCase {


	private MockControl _connectionFactoryControl;
	private ConnectionFactory _mockConnectionFactory;
	
	private MockControl _connectionControl;
	private Connection _mockConnection;
	
	private MockControl _sessionControl;
	private Session _mockSession;
	
	private MockControl _queueControl;
	private Queue _mockQueue;
	
	/**
	 * Constructor for JmsSender11Tests.
	 * @param name name of the test
	 */
	public JmsSender11Tests(String name) {
		super(name);
	}
	
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
	
	private void createMockforDestination() throws JMSException, NamingException {
		_connectionFactoryControl =
			MockControl.createControl(ConnectionFactory.class);
		_mockConnectionFactory =
			(ConnectionFactory) _connectionFactoryControl.getMock();

		_connectionControl =
			MockControl.createControl(Connection.class);
		_mockConnection =
			(Connection) _connectionControl.getMock();

		_sessionControl = MockControl.createControl(Session.class);
		_mockSession = (Session) _sessionControl.getMock();
		
		_queueControl = MockControl.createControl(Queue.class);
		_mockQueue = (Queue) _queueControl.getMock();		

		_mockConnectionFactory.createConnection();
		_connectionFactoryControl.setReturnValue(_mockConnection);
		_connectionFactoryControl.replay();

		_mockConnection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		_connectionControl.setReturnValue(_mockSession);

		mockJndiContext.lookup("testDestination");
		mockJndiControl.setReturnValue(_mockQueue);
	}
	
	public void testSendDestination() throws Exception
	{
		JmsSender11 sender = new JmsSender11();
		sender.setConnectionFactory(_mockConnectionFactory);
		sender.setJmsAdmin(new DefaultJmsAdmin());

		//Mock the javax.jms MessageProducer
		MockControl messageProducerControl =
			MockControl.createControl(MessageProducer.class);
		MessageProducer mockMessageProducer =
			(MessageProducer) messageProducerControl.getMock();

		MockControl messageControl =
			MockControl.createControl(TextMessage.class);
		TextMessage mockMessage = (TextMessage) messageControl.getMock();

		_mockConnection.close();
		_connectionControl.replay();

		_mockSession.createProducer(_mockQueue);
		_sessionControl.setReturnValue(mockMessageProducer);
		_mockSession.createTextMessage("just testing");
		_sessionControl.setReturnValue(mockMessage);
		_sessionControl.replay();

		mockMessageProducer.send(_mockQueue, mockMessage);
		messageProducerControl.replay();

		sender.send("testDestination", new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("just testing");
			}
		});
		
		_connectionFactoryControl.verify();
		_connectionControl.verify();
		messageProducerControl.verify();

		_sessionControl.verify();
	
	}


}
