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
 *
 * Tests to make sure that jms listeners work properly.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public class JmsTopicListenerTests extends TestCase{

    private MockControl topicConnectionFactoryControl;
    private TopicConnectionFactory mockTopicConnectionFactory;

    private static Context mockJndiContext;
    private MockControl mockJndiControl;

    private MockControl topicControl;
    private Topic mockTopic;

    private MockControl topicConnectionControl;
    private TopicConnection mockTopicConnection;

    private MockControl topicSessionControl;
    private TopicSession mockTopicSession;

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

    public JmsTopicListenerTests(String name){
        super(name);
    }

    public void setUp() throws Exception{
        this.mockJndiControl = MockControl.createControl(Context.class);
        this.mockJndiContext = (Context)this.mockJndiControl.getMock();

        this.topicConnectionFactoryControl = MockControl.createControl(TopicConnectionFactory.class);
        this.mockTopicConnectionFactory = (TopicConnectionFactory)this.topicConnectionFactoryControl.getMock();

        this.topicConnectionControl = MockControl.createControl(TopicConnection.class);
        this.mockTopicConnection = (TopicConnection)this.topicConnectionControl.getMock();

        this.topicControl = MockControl.createControl(Topic.class);
        this.mockTopic = (Topic)this.topicControl.getMock();

        this.mockTopicConnectionFactory.createTopicConnection();
        this.topicConnectionFactoryControl.setReturnValue(this.mockTopicConnection);
        this.topicConnectionFactoryControl.replay();

        this.topicSessionControl = MockControl.createControl(TopicSession.class);
        this.mockTopicSession = (TopicSession)topicSessionControl.getMock();
    }

    public void testTopicListenerValidation(){
        JmsTopicListener listener = new JmsTopicListener();

        try {
            listener.afterPropertiesSet();
            fail("expected illegal argument exception here.");
        } catch (IllegalArgumentException e){
            //expected
        } catch (Exception e){
            fail("exception thrown was not IllegalArgumentException.");
        }

        listener.setConnectionFactoryName("connectionfactory");
        listener.setDestinationName("topic");
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

    public void testTopicListener() throws Exception{
        MockControl topicSubscriberControl = MockControl.createControl(TopicSubscriber.class);
        TopicSubscriber mockTopicSubscriber = (TopicSubscriber)topicSubscriberControl.getMock();

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);
        this.mockTopicConnection.start();
        this.topicConnectionControl.replay();

        this.mockTopicSession.createSubscriber(this.mockTopic,null,false);
        this.topicSessionControl.setReturnValue(mockTopicSubscriber);
        this.topicSessionControl.replay();

        JmsTopicListener listener = new JmsTopicListener();

        listener.setConnectionFactory(this.mockTopicConnectionFactory);
        listener.setDestination(this.mockTopic);
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
    }

    public void testDurableListener() throws Exception{
        MockControl topicSubscriberControl = MockControl.createControl(TopicSubscriber.class);
        TopicSubscriber mockTopicSubscriber = (TopicSubscriber)topicSubscriberControl.getMock();

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);
        this.mockTopicConnection.start();
        this.mockTopicConnection.close();
        this.topicConnectionControl.replay();

        this.mockTopicSession.createDurableSubscriber(this.mockTopic,"iamdurable",null,false);
        this.topicSessionControl.setReturnValue(mockTopicSubscriber);
        this.topicSessionControl.replay();

        JmsTopicListener listener = new JmsTopicListener();

        listener.setConnectionFactory(this.mockTopicConnectionFactory);
        listener.setDurable(true);
        listener.setDurableName("iamdurable");
        listener.setDestination(this.mockTopic);
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
}
