package org.springframework.jms;

import junit.framework.TestCase;

import javax.jms.*;
import javax.naming.spi.NamingManager;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.InitialContextFactory;
import javax.naming.NamingException;
import javax.naming.Context;

import java.util.Hashtable;
import java.util.Properties;

import org.easymock.MockControl;
import org.springframework.jms.support.JmsTemplateSupport;

/**
 * Unit tests for the Spring Jms publish/subscribe Template class.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public class PubSubTemplateTests extends TestCase{

    private static Context mockJndiContext;

    private MockControl mockJndiControl;

    private MockControl topicConnectionFactoryControl;
    private TopicConnectionFactory mockTopicConnectionFactory;

    private MockControl topicConnectionControl;
    private TopicConnection mockTopicConnection;

    private MockControl topicControl;
    private Topic mockTopic;

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

    /**
     * Standard JUnit constructor.
     *
     * @param name
     */
    public PubSubTemplateTests(String name){
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

        this.topicSessionControl = MockControl.createControl(TopicSession.class);
        this.mockTopicSession = (TopicSession)topicSessionControl.getMock();

        this.mockJndiContext.lookup("testTopic");
        this.mockJndiControl.setReturnValue(this.mockTopic);
        this.mockJndiContext.close();
        this.mockJndiControl.replay();

        this.mockTopicConnectionFactory.createTopicConnection();
        this.topicConnectionFactoryControl.setReturnValue(this.mockTopicConnection);
        this.topicConnectionFactoryControl.replay();
    }

    public void tearDown() throws Exception{
        this.mockJndiControl.verify();
        this.topicConnectionFactoryControl.verify();
        this.topicConnectionControl.verify();
        this.topicSessionControl.verify();
    }

    /**
     * Tests a simple case of publishing a single message to a topic.
     *
     * @throws Exception
     */
    public void testSendMessage() throws Exception{

        MockControl topicPublisherControl = MockControl.createControl(TopicPublisher.class);
        TopicPublisher mockTopicPublisher = (TopicPublisher)topicPublisherControl.getMock();

        MockControl messageControl = MockControl.createControl(TextMessage.class);
        Message mockMessage = (Message)messageControl.getMock();

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);
        this.mockTopicConnection.close();
        this.topicConnectionControl.replay();

        this.mockTopicSession.createPublisher(this.mockTopic);
        this.topicSessionControl.setReturnValue(mockTopicPublisher);
        this.mockTopicSession.createTextMessage("just testing");
        this.topicSessionControl.setReturnValue(mockMessage);
        this.topicSessionControl.replay();

        mockTopicPublisher.publish(mockMessage,2,4,0);
        topicPublisherControl.replay();

        JmsPubSubTemplate template = new JmsPubSubTemplate(this.mockTopicConnectionFactory);
        template.publish("testTopic",
                new JmsTemplateSupport.MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage("just testing");
                    }
                });

        topicPublisherControl.verify();
    }

    /**
     * Tests a non-durable subscriber.
     *
     * @throws Exception
     */
    public void testSubscribeNonDurable() throws Exception{

        MockControl topicSubscriberControl = MockControl.createControl(TopicSubscriber.class);
        TopicSubscriber mockTopicSubscriber = (TopicSubscriber)topicSubscriberControl.getMock();

        MockControl messageListenerControl = MockControl.createControl(MessageListener.class);
        MessageListener mockMessageListener = (MessageListener)messageListenerControl.getMock();

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);
        this.mockTopicConnection.start();
        this.topicConnectionControl.replay();

        this.mockTopicSession.createSubscriber(this.mockTopic,null,false);
        this.topicSessionControl.setReturnValue(mockTopicSubscriber);
        this.topicSessionControl.replay();

        mockTopicSubscriber.setMessageListener(mockMessageListener);
        topicSubscriberControl.replay();

        new JmsPubSubTemplate(this.mockTopicConnectionFactory).subscribe("testTopic",
                mockMessageListener,
                null);

        topicSubscriberControl.verify();
    }

    /**
     * Tests a synchronous receiver.
     *
     * @throws Exception
     */
    public void testReceiveSynch() throws Exception{
        MockControl topicSubscriberControl = MockControl.createControl(TopicSubscriber.class);
        TopicSubscriber mockTopicSubscriber = (TopicSubscriber)topicSubscriberControl.getMock();

        MockControl messageControl = MockControl.createControl(Message.class);
        Message mockMessage = (Message)messageControl.getMock();

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);

        this.mockTopicSession.createSubscriber(this.mockTopic, null, false);
        this.topicSessionControl.setReturnValue(mockTopicSubscriber);
        this.topicSessionControl.replay();

        mockTopicSubscriber.receive(1000);
        topicSubscriberControl.setReturnValue(mockMessage);
        topicSubscriberControl.replay();

        this.mockTopicConnection.start();
        this.mockTopicConnection.close();
        this.topicConnectionControl.replay();

        new JmsPubSubTemplate(this.mockTopicConnectionFactory,new Properties()).consumeMessageSynch("testTopic",null,1000,null,null);

        topicSubscriberControl.verify();
    }

    /**
     * Tests a durable subscriber.
     *
     * @throws Exception
     */
    public void testSubscribeDurable() throws Exception{
        MockControl topicSubscriberControl = MockControl.createControl(TopicSubscriber.class);
        TopicSubscriber mockTopicSubscriber = (TopicSubscriber)topicSubscriberControl.getMock();

        MockControl messageListenerControl = MockControl.createControl(MessageListener.class);
        MessageListener mockMessageListener = (MessageListener)messageListenerControl.getMock();

        this.mockTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topicConnectionControl.setReturnValue(this.mockTopicSession);

        this.mockTopicSession.createDurableSubscriber(this.mockTopic, "name", null, false);
        this.topicSessionControl.setReturnValue(mockTopicSubscriber);
        this.topicSessionControl.replay();

        this.mockTopicConnection.start();
        this.topicConnectionControl.replay();

        new JmsPubSubTemplate(this.mockTopicConnectionFactory,new Properties()).
                subscribeDurable("testTopic","name", mockMessageListener);
    }
}
