package org.springframework.jms;

import junit.framework.TestCase;
import org.easymock.MockControl;

import javax.jms.*;
import javax.naming.spi.NamingManager;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.InitialContextFactory;
import javax.naming.NamingException;
import javax.naming.Context;
import java.util.Hashtable;

/**
 * The queue listener tests.
 *
 * @author Andre Biryukov
 * @version 0.1
 */
public class JmsQueueListenerTests extends TestCase{

    private static Context mockJndiContext;
    private MockControl mockJndiControl;

    private MockControl queueConnectionFactoryControl;
    private QueueConnectionFactory mockQueueConnectionFactory;

    private MockControl queueConnectionControl;
    private QueueConnection mockQueueConnection;

    private MockControl queueControl;
    private Queue mockQueue;

    private MockControl queueSessionControl;
    private QueueSession mockQueueSession;

    public JmsQueueListenerTests(String name){
        super(name);
    }

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

    public void setUp() throws Exception{
        this.mockJndiControl = MockControl.createControl(Context.class);
        this.mockJndiContext = (Context)this.mockJndiControl.getMock();

        this.queueConnectionFactoryControl = MockControl.createControl(QueueConnectionFactory.class);
        this.mockQueueConnectionFactory = (QueueConnectionFactory)this.queueConnectionFactoryControl.getMock();

        this.queueConnectionControl = MockControl.createControl(QueueConnection.class);
        this.mockQueueConnection = (QueueConnection)this.queueConnectionControl.getMock();

        this.queueControl = MockControl.createControl(Queue.class);
        this.mockQueue = (Queue)this.queueControl.getMock();

        this.mockQueueConnectionFactory.createQueueConnection();
        this.queueConnectionFactoryControl.setReturnValue(this.mockQueueConnection);
        this.queueConnectionFactoryControl.replay();

        this.queueSessionControl = MockControl.createControl(QueueSession.class);
        this.mockQueueSession = (QueueSession)queueSessionControl.getMock();
    }

    public void testQueueListener() throws Exception{
        MockControl queueReceiverControl = MockControl.createControl(QueueReceiver.class);
        QueueReceiver mockQueueSubscriber = (QueueReceiver)queueReceiverControl.getMock();

        this.mockQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        this.queueConnectionControl.setReturnValue(this.mockQueueSession);
        this.mockQueueConnection.start();
        this.mockQueueConnection.close();
        this.queueConnectionControl.replay();

        this.mockQueueSession.createReceiver(this.mockQueue,"all");
        this.queueSessionControl.setReturnValue(mockQueueSubscriber);
        this.queueSessionControl.replay();

        JmsQueueListener listener = new JmsQueueListener();

        listener.setConnectionFactory(this.mockQueueConnectionFactory);
        listener.setDestination(this.mockQueue);
        listener.setMessageSelector("all");
        listener.setJmsAuthenticationUserName(null);
        listener.setJmsAuthenticationPassword(null);
        listener.setListener(new MessageListener() {
            public void onMessage(Message message) {
                return;
            }
        });

        try {
            listener.afterPropertiesSet();
        } catch (Exception e){
            fail(e.toString());
        }
        listener.destroy();
    }

    public void testQueueListenerValidation(){
        JmsQueueListener listener = new JmsQueueListener();

        try {
            listener.afterPropertiesSet();
            fail("expected illegal argument exception here.");
        } catch (IllegalArgumentException e){
            //expected
        } catch (Exception e){
            fail("exception thrown was not IllegalArgumentException.");
        }

        listener.setConnectionFactoryName("connectionfactory");
        listener.setDestinationName("queue");
        listener.setListener(new MessageListener() {
            public void onMessage(Message message) {
                return;
            }
        });

        try{
            listener.afterPropertiesSet();
            fail("expected an exception here, indicating a jms/jndi error.");
        } catch (IllegalArgumentException e){
            fail("illegal argument exception is not expected here.");
        } catch (Exception e){
            //expected
        }
    }
}
