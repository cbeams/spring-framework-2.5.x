package org.springframework.jms;

import junit.framework.TestCase;

import javax.naming.spi.NamingManager;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.InitialContextFactory;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.jms.*;
import java.util.Hashtable;
import java.util.Properties;

import org.easymock.MockControl;
import org.springframework.jms.support.JmsTemplateSupport;

/**
 * Point-to-point template tests.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public class P2PTemplateTests extends TestCase{

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

    static {
        try {
            NamingManager.setInitialContextFactoryBuilder(
                    new InitialContextFactoryBuilder(){
                        public InitialContextFactory createInitialContextFactory(Hashtable hashtable) throws NamingException {
                            return new InitialContextFactory(){
                                public Context getInitialContext(Hashtable hashtable) throws NamingException {
                                    return mockJndiContext;
                                }
                            };
                        }
                    }
            );
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Standard JUnit constructor.
     * @param name
     */
    public P2PTemplateTests(String name){
        super(name);
    }

    /**
     * Setup mock objects common for all the tests.
     *
     * @throws Exception
     */
    public void setUp() throws Exception{
        this.mockJndiControl = MockControl.createControl(Context.class);
        this.mockJndiContext = (Context)this.mockJndiControl.getMock();

        this.queueConnectionFactoryControl = MockControl.createControl(QueueConnectionFactory.class);
        this.mockQueueConnectionFactory = (QueueConnectionFactory)this.queueConnectionFactoryControl.getMock();

        this.queueConnectionControl = MockControl.createControl(QueueConnection.class);
        this.mockQueueConnection = (QueueConnection)this.queueConnectionControl.getMock();

        this.queueControl = MockControl.createControl(Queue.class);
        this.mockQueue = (Queue)this.queueControl.getMock();

        this.queueSessionControl = MockControl.createControl(QueueSession.class);
        this.mockQueueSession = (QueueSession)this.queueSessionControl.getMock();

        this.mockQueueConnectionFactory.createQueueConnection();
        this.queueConnectionFactoryControl.setReturnValue(this.mockQueueConnection);
        this.queueConnectionFactoryControl.replay();

        this.mockQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        this.queueConnectionControl.setReturnValue(this.mockQueueSession);

        this.mockJndiContext.lookup("testQueue");
        this.mockJndiControl.setReturnValue(this.mockQueue);
        this.mockJndiContext.close();
        this.mockJndiControl.replay();
    }

    public void tearDown() throws Exception{
        this.mockJndiControl.verify();
        this.queueConnectionControl.verify();
        this.queueConnectionFactoryControl.verify();
    }

    /**
     * Send a single message to a test queue.
     *
     * @throws Exception
     */
    public void testSend2Queue() throws Exception{
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

        mockQueueSender.send(mockMessage,2,4,0);
        queueSenderControl.replay();

        new JmsP2PTemplate(this.mockQueueConnectionFactory).send("testQueue",
                new JmsTemplateSupport.MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage("just testing");
                    }
                });

        queueSenderControl.verify();
        this.queueSessionControl.verify();
    }

    /**
     * Register a listener to a test queue.
     *
     * @throws Exception
     */
    public void testReceiveFromQueue() throws Exception{
        MockControl queueReceiverControl = MockControl.createControl(QueueReceiver.class);
        QueueReceiver mockQueueReceiver = (QueueReceiver)queueReceiverControl.getMock();

        MockControl messageListenerControl = MockControl.createControl(MessageListener.class);
        MessageListener mockMessageListener = (MessageListener)messageListenerControl.getMock();

        this.mockQueueSession.createReceiver(this.mockQueue, null);
        this.queueSessionControl.setReturnValue(mockQueueReceiver);
        this.queueSessionControl.replay();

        mockQueueReceiver.setMessageListener(mockMessageListener);
        queueReceiverControl.replay();

        this.mockQueueConnection.start();
        this.queueConnectionControl.replay();

        new JmsP2PTemplate(this.mockQueueConnectionFactory).receive("testQueue", mockMessageListener);

        queueReceiverControl.verify();
    }

    public void testReceiveSynch() throws Exception{
        MockControl queueReceiverControl = MockControl.createControl(QueueReceiver.class);
        QueueReceiver mockQueueReceiver = (QueueReceiver)queueReceiverControl.getMock();

        MockControl messageControl = MockControl.createControl(Message.class);
        Message mockMessage = (Message)messageControl.getMock();

        this.mockQueueSession.createReceiver(this.mockQueue, null);
        this.queueSessionControl.setReturnValue(mockQueueReceiver);
        this.queueSessionControl.replay();

        mockQueueReceiver.receive(1000);
        queueReceiverControl.setReturnValue(mockMessage);
        queueReceiverControl.replay();

        this.mockQueueConnection.start();
        this.mockQueueConnection.close();
        this.queueConnectionControl.replay();

        new JmsP2PTemplate(this.mockQueueConnectionFactory,new Properties()).consumeMessageSynch("testQueue",null,1000,null,null);

        queueReceiverControl.verify();
    }
}
