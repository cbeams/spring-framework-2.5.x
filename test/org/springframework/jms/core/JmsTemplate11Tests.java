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

import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiTemplate;

/**
 * Unit tests for the JmsTemplate implemented using JMS 1.1.
 * @author Andre Biryukov
 * @author Mark Pollack
 */
public class JmsTemplate11Tests extends TestCase {

    private Context mockJndiContext;
    private MockControl mockJndiControl;

    private MockControl _connectionFactoryControl;
    private ConnectionFactory _mockConnectionFactory;

    private MockControl _connectionControl;
    private Connection _mockConnection;

    private MockControl _sessionControl;
    private Session _mockSession;

    private MockControl _queueControl;
    private Queue _mockQueue;

    private int _deliveryMode = DeliveryMode.PERSISTENT;
    private int _priority = 9;
    private int _timeToLive = 10000;

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
        _connectionFactoryControl =
            MockControl.createControl(ConnectionFactory.class);
        _mockConnectionFactory =
            (ConnectionFactory) _connectionFactoryControl.getMock();

        _connectionControl = MockControl.createControl(Connection.class);
        _mockConnection = (Connection) _connectionControl.getMock();

        _sessionControl = MockControl.createControl(Session.class);
        _mockSession = (Session) _sessionControl.getMock();

        _queueControl = MockControl.createControl(Queue.class);
        _mockQueue = (Queue) _queueControl.getMock();

        _mockConnectionFactory.createConnection();
        _connectionFactoryControl.setReturnValue(_mockConnection);
        _connectionFactoryControl.replay();

        //TODO tests with TX= true
        _mockConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        _connectionControl.setReturnValue(_mockSession);

        mockJndiContext.lookup("testDestination");
        mockJndiControl.setReturnValue(_mockQueue);
    }

    public void testProducerCallback() throws Exception {
        JmsTemplate sender = new JmsTemplate();
        sender.setConnectionFactory(_mockConnectionFactory);
        setJndiTemplate(sender);

        //Session behavior
        _mockSession.getTransacted();
        _sessionControl.setReturnValue(true);

        //Mock the javax.jms MessageProducer
        MockControl messageProducerControl =
            MockControl.createControl(MessageProducer.class);
        MessageProducer mockMessageProducer =
            (MessageProducer) messageProducerControl.getMock();

        _mockSession.createProducer(null);
        _sessionControl.setReturnValue(mockMessageProducer);

        mockMessageProducer.getPriority();
        messageProducerControl.setReturnValue(4);

        messageProducerControl.replay();

        _mockSession.close();
        _sessionControl.setVoidCallable(1);

        //Connection behavior
        _mockConnection.close();
        _connectionControl.setVoidCallable(1);

        _sessionControl.replay();
        _connectionControl.replay();

        sender.execute(new ProducerCallback() {
            public Object doInJms(Session session, MessageProducer msgProducer)
                throws JMSException {
                boolean b = session.getTransacted();
                int i = msgProducer.getPriority();
                return null;
            }
        });

        _connectionFactoryControl.verify();
        _connectionControl.verify();
        _sessionControl.verify();
    }

    /**
     * Test the method execute(SessionCallback action).
     */
    public void testSessionCallback() throws Exception {
        JmsTemplate sender = new JmsTemplate();
        sender.setConnectionFactory(_mockConnectionFactory);
        setJndiTemplate(sender);

        //Session behavior
        _mockSession.getTransacted();
        _sessionControl.setReturnValue(true);

        _mockSession.close();
        _sessionControl.setVoidCallable(1);

        //Connection behavior
        _mockConnection.close();
        _connectionControl.setVoidCallable(1);

        _sessionControl.replay();
        _connectionControl.replay();

        sender.execute(new SessionCallback() {
            public Object doInJms(Session session) throws JMSException {
                boolean b = session.getTransacted();
                return null;
            }
        });

        _connectionFactoryControl.verify();
        _connectionControl.verify();
        _sessionControl.verify();
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
    private void doTestSendDestination(
        boolean ignoreQOS,
        boolean explicitDestination,
        boolean useDefaultDestination)
        throws Exception {
        JmsTemplate sender = new JmsTemplate();
        sender.setConnectionFactory(_mockConnectionFactory);
        setJndiTemplate(sender);
        if (useDefaultDestination) {
            sender.setDefaultDestination(_mockQueue);
        }

        //Mock the javax.jms MessageProducer
        MockControl messageProducerControl =
            MockControl.createControl(MessageProducer.class);
        MessageProducer mockMessageProducer =
            (MessageProducer) messageProducerControl.getMock();

        MockControl messageControl =
            MockControl.createControl(TextMessage.class);
        TextMessage mockMessage = (TextMessage) messageControl.getMock();

        _mockSession.close();
        _sessionControl.setVoidCallable(1);

        _mockConnection.close();
        _connectionControl.setVoidCallable(1);

        _mockSession.createProducer(_mockQueue);
        _sessionControl.setReturnValue(mockMessageProducer);
        _mockSession.createTextMessage("just testing");
        _sessionControl.setReturnValue(mockMessage);

        _sessionControl.replay();
        _connectionControl.replay();

        if (ignoreQOS) {
            mockMessageProducer.send(mockMessage);
        } else {
            sender.setExplicitQosEnabled(true);
            sender.setDeliveryMode(_deliveryMode);
            sender.setPriority(_priority);
            sender.setTimeToLive(_timeToLive);
            mockMessageProducer.send(
                mockMessage,
                _deliveryMode,
                _priority,
                _timeToLive);
        }

        messageProducerControl.replay();

        if (useDefaultDestination) {
            sender.send(new MessageCreator() {
                public Message createMessage(Session session)
                    throws JMSException {
                    return session.createTextMessage("just testing");
                }
            });
        } else {
            if (explicitDestination) {
                sender.send(_mockQueue, new MessageCreator() {
                    public Message createMessage(Session session)
                        throws JMSException {
                        return session.createTextMessage("just testing");
                    }
                });
            } else {
                sender.send("testDestination", new MessageCreator() {
                    public Message createMessage(Session session)
                        throws JMSException {
                        return session.createTextMessage("just testing");
                    }
                });
            }
        }

        _connectionFactoryControl.verify();
        _connectionControl.verify();
        messageProducerControl.verify();

        _sessionControl.verify();

    }

    public void testConverter() throws Exception {
        JmsTemplate sender = new JmsTemplate();
        sender.setConnectionFactory(_mockConnectionFactory);
        setJndiTemplate(sender);
        sender.setMessageConverter(new SimpleMessageConverter());
        String s = "Hello world";

        //Mock the javax.jms MessageProducer
        MockControl messageProducerControl =
            MockControl.createControl(MessageProducer.class);
        MessageProducer mockMessageProducer =
            (MessageProducer) messageProducerControl.getMock();

        MockControl messageControl =
            MockControl.createControl(TextMessage.class);
        TextMessage mockMessage = (TextMessage) messageControl.getMock();

        _mockSession.close();
        _sessionControl.setVoidCallable(1);

        _mockConnection.close();
        _connectionControl.setVoidCallable(1);

        _mockSession.createProducer(_mockQueue);
        _sessionControl.setReturnValue(mockMessageProducer);
        _mockSession.createTextMessage("Hello world");
        _sessionControl.setReturnValue(mockMessage);

        _sessionControl.replay();
        _connectionControl.replay();

        mockMessageProducer.send(mockMessage);

        messageProducerControl.replay();

        sender.convertAndSend(_mockQueue, s);

        _connectionFactoryControl.verify();
        _connectionControl.verify();
        messageProducerControl.verify();

        _sessionControl.verify();
    }

    private void doTestReceive(
        boolean explicitDestination,
        boolean useDefaultDestination,
        boolean testConverter)
        throws Exception {
        JmsTemplate sender = new JmsTemplate();
        sender.setConnectionFactory(_mockConnectionFactory);
        setJndiTemplate(sender);

        if (useDefaultDestination) {
            sender.setDefaultDestination(_mockQueue);
        }

        //Mock the javax.jms MessageConsumer
        MockControl messageConsumerControl =
            MockControl.createControl(MessageConsumer.class);
        MessageConsumer mockMessageConsumer =
            (MessageConsumer) messageConsumerControl.getMock();

        MockControl messageControl =
            MockControl.createControl(TextMessage.class);
        TextMessage mockMessage = (TextMessage) messageControl.getMock();

        if (testConverter) {
            mockMessage.getText();
            messageControl.setReturnValue("Hello World!");
        }
        mockMessage.acknowledge();
        messageControl.setVoidCallable(1);

        _mockSession.close();
        _sessionControl.setVoidCallable(1);

        _mockConnection.start();
        _connectionControl.setVoidCallable(1);
        _mockConnection.close();
        _connectionControl.setVoidCallable(1);

        _mockSession.createConsumer(_mockQueue);
        _sessionControl.setReturnValue(mockMessageConsumer);

        _sessionControl.replay();
        _connectionControl.replay();
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
            } else {
                m = sender.receive();
            }
        } else {
            if (explicitDestination) {
                if (testConverter) {
                    textFromMessage =
                        (String) sender.receiveAndConvert(_mockQueue);
                } else {
                    m = sender.receive(_mockQueue);
                }
            } else {
                if (testConverter) {
                    textFromMessage =
                        (String) sender.receiveAndConvert("testDestination");
                } else {
                    m = sender.receive("testDestination");
                }
            }
        }

        _connectionFactoryControl.verify();
        _connectionControl.verify();
        messageConsumerControl.verify();

        _sessionControl.verify();

        if (testConverter) {
            assertEquals(
                "Message Text should be equal",
                "Hello World!",
                textFromMessage);
        } else {

            assertEquals(
                "Messages should refer to the same object",
                m,
                mockMessage);
        }
    }

    public void testReceiveDefaultDestination() throws Exception {
        doTestReceive(false, true, false);
    }

    public void testReceiveDestination() throws Exception {
        doTestReceive(true, false, false);
    }

    public void testReceiveStringDestination() throws Exception {
        doTestReceive(false, false, false);
    }

    public void testReceiveAndConvertDefaultDestination() throws Exception {
        doTestReceive(false, true, true);
    }

    public void testReceiveAndConvertStringDestination() throws Exception {
        doTestReceive(false, false, true);
    }

    public void testReceiveAndConvertDestination() throws Exception {
        doTestReceive(true, false, true);
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
