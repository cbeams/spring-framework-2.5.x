package org.springframework.jms;

import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.Topic;

/**
 * This class is instrumental in helping your beans register with a topic at the time
 * of loading of your application context. The following example illustrates possible
 * configuration:
 *
 * <bean name="jmsTestTopicListener" class="org.springframework.jms.JmsTopicListener">
 *       <property name="connectionFactory"><ref local="topicConnectionFactory"/></property>
 *       <property name="destinationName"><value>jms/topic/testTopic</value></property>
 *       <property name="listener"><ref local="jmsTestSubService"/></property>
 * </bean>
 *
 * where topicConnectionFactory references a jndi resource as a JndiObjectFactoryBean, listener
 * implements MessageListener interface, user name and password are optional.
 *
 * @author Andre Biryukov
 * @version 0.1
 */
public final class JmsTopicListener extends JmsAbstractListener {

    //---------------------------------------------------------------------
    // Instance data
    //---------------------------------------------------------------------
    //so we can close the connection upon shutdown
    private TopicConnection topicConnection;

    //indicates whether the subscription shd be durable or not, optional, default to false
    private boolean durable = false;

    //a unique identifier for a durable suscription, required if durable is set to true
    private String durableName;

    //---------------------------------------------------------------------
    // End of instance data
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------
    public JmsTopicListener() {
    }
    //---------------------------------------------------------------------
    // End of Constructors
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Setters and Getters
    //---------------------------------------------------------------------

    public void setDurable(final boolean durable) {
        this.durable = durable;
    }

    public void setDurableName(final String durableName) {
        this.durableName = durableName;
    }
    //---------------------------------------------------------------------
    // End of Setters and Getters
    //---------------------------------------------------------------------

    /**
     * This method comes from InitializingBean interface. It validates the content of the instance data first and
     * throws an error if it's incomplete.
     * It, then, creates a jms template and subscribes the listener to the topic specified.
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (!super.isValid() || (this.durable && this.durableName == null)) {
            throw new IllegalArgumentException(
                    "JmsTopicListener: one or more of the required properties were not set or are nulls. ");
        }

        final JmsPubSubTemplate template;
        if (super.getConnectionFactory() != null) {
            template = new JmsPubSubTemplate((TopicConnectionFactory) super.getConnectionFactory());
        } else {
            template = new JmsPubSubTemplate(super.getConnectionFactoryName());
        }

        if (this.durable) {
            this.startDurableConnection(template);
        } else {
            this.startConnection(template);
        }
    }

    /**
     * Starts a non durable subscription.
     *
     * @param template
     */
    private void startConnection(final JmsPubSubTemplate template) {
        if (super.getDestination() == null) {
            this.topicConnection = template.subscribe(super.getDestinationName(),
                    super.getListener(),
                    super.getMessageSelector(),
                    super.getJmsAuthenticationUserName(),
                    super.getJmsAuthenticationPassword());
        } else {
            this.topicConnection = template.subscribe((Topic) super.getDestination(),
                    super.getListener(),
                    super.getMessageSelector(),
                    super.getJmsAuthenticationUserName(),
                    super.getJmsAuthenticationPassword());
        }
    }

    /**
     * Starts a durable subscription using a given jms template.
     *
     * @param template
     */
    private void startDurableConnection(final JmsPubSubTemplate template) {
        if (super.getDestination() == null) {
            this.topicConnection = template.subscribeDurable(super.getDestinationName(),
                    this.durableName,
                    super.getListener(),
                    super.getMessageSelector(),
                    super.getJmsAuthenticationUserName(),
                    super.getJmsAuthenticationPassword());

        } else {
            this.topicConnection = template.subscribeDurable((Topic) super.getDestination(),
                    this.durableName,
                    super.getListener(),
                    super.getMessageSelector(),
                    super.getJmsAuthenticationUserName(),
                    super.getJmsAuthenticationPassword());
        }
    }

    /**
     * This will take care of the open topic connection upon exiting.
     *
     * @throws Exception
     */
    public void destroy() throws Exception {
        if (this.topicConnection != null) {
            this.topicConnection.close();
        }
    }
}
