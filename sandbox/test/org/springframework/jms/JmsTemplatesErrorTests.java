package org.springframework.jms;

import junit.framework.TestCase;
import org.easymock.MockControl;

import javax.jms.*;
import java.util.Properties;

/**
 * These tests are to to make sure that the error handling works as expected.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public class JmsTemplatesErrorTests extends TestCase{

    /**
     * Tests the point-to-point JMS template for the case when the connection factory
     * for some reason returns a null connection.
     *
     * @throws Exception
     */
    public void testNullConnectionReturnedP2P() throws Exception{
        MockControl queueConnectionFactoryControl = MockControl.createControl(QueueConnectionFactory.class);
        QueueConnectionFactory mockQueueConnectionFactory = (QueueConnectionFactory)queueConnectionFactoryControl.getMock();

        mockQueueConnectionFactory.createQueueConnection();
        queueConnectionFactoryControl.setReturnValue(null);
        queueConnectionFactoryControl.replay();

        try{
            new JmsP2PTemplate(mockQueueConnectionFactory, new Properties()).receive("testQueue", null,null,null,null);
            fail("Queue connection is a null object, expected an exception thrown here.");
        } catch (JmsException e){
            //expected
        }
    }

    /**
     * Tests the publish/subscribe JMS template for the case when the connection factory
     * for some reason returns a null connection.
     *
     * @throws Exception
     */
    public void testNullConnectionReturnedPubAndSub() throws Exception{
        MockControl topicConnectionFactoryControl = MockControl.createControl(TopicConnectionFactory.class);
        TopicConnectionFactory mockQueueConnectionFactory =
                (TopicConnectionFactory)topicConnectionFactoryControl.getMock();

        mockQueueConnectionFactory.createTopicConnection("name","password");
        topicConnectionFactoryControl.setReturnValue(null);
        topicConnectionFactoryControl.replay();

        try{
            new JmsPubSubTemplate(mockQueueConnectionFactory, new Properties()).
                    subscribe("testTopic", null,null,"name","password");
            fail("Queue connection is a null object, expected an exception thrown here.");
        } catch (JmsException e){
            //expected
        }
    }

    /**
     * Make sure that JMSException is caught and properly wrapped.
     *
     * @throws Exception
     */
    public void testJmsExceptionHandlingP2P() throws Exception{
        MockControl queueConnectionFactoryControl = MockControl.createControl(QueueConnectionFactory.class);
        QueueConnectionFactory mockQueueConnectionFactory = (QueueConnectionFactory)queueConnectionFactoryControl.getMock();

        MockControl queueConnectionControl = MockControl.createControl(QueueConnection.class);
        QueueConnection mockQueueConnection = (QueueConnection)queueConnectionControl.getMock();

        mockQueueConnectionFactory.createQueueConnection("","");
        queueConnectionFactoryControl.setReturnValue(mockQueueConnection);
        queueConnectionFactoryControl.replay();

        mockQueueConnection.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
        queueConnectionControl.setThrowable(new JMSException(""));
        queueConnectionControl.replay();

        try{
            new JmsP2PTemplate(mockQueueConnectionFactory).receive("testQueue", null,null,"","");
            fail("Should've thrown a jms exception here.");
        } catch (JmsException e){
            //expected
        }
    }

    public void testJmsExceptionHandlingPubSub() throws Exception{
        MockControl topicConnectionFactoryControl = MockControl.createControl(TopicConnectionFactory.class);
        TopicConnectionFactory mockTopicConnectionFactory = (TopicConnectionFactory)topicConnectionFactoryControl.getMock();

        MockControl topicConnectionControl = MockControl.createControl(TopicConnection.class);
        TopicConnection mockTopicConnection = (TopicConnection)topicConnectionControl.getMock();

        mockTopicConnectionFactory.createTopicConnection("","");
        topicConnectionFactoryControl.setReturnValue(mockTopicConnection);
        topicConnectionFactoryControl.replay();

        mockTopicConnection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
        topicConnectionControl.setThrowable(new JMSException(""));
        topicConnectionControl.replay();

        try{
            new JmsPubSubTemplate(mockTopicConnectionFactory).subscribe("testTopic", null,null,"","");
            fail("Should've thrown a jms exception here.");
        } catch (JmsException e){
            //expected
        }
    }
}
