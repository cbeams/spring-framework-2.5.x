package org.springframework.jms;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Queue;

/**
 * This bean simplifies the process of subscribing a listener to a queue through the application context.
 * A listener could be automatically registered by configuring the following bean:
 *
 * <bean name="jmsTestQueueListener" class="org.springframework.jms.JmsQueueListener">
 *       <property name="connectionFactory"><ref local="queueConnectionFactory"/></property>
 *       <property name="destinationName"><value>jms/queue/testQueue</value></property>
 *       <property name="listener"><ref local="jmsTestSubService"/></property>
 * </bean>
 *
 * where, queueConnection factory references a jndi resource bean, listener is just another
 * implementation of MessageListener interface and user name and password are optional.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public final class JmsQueueListener extends JmsAbstractListener {
    //---------------------------------------------------------------------
    // Instance data
    //---------------------------------------------------------------------
    //this connection to be closed by the destroy method
    private QueueConnection queueConnection;

    //---------------------------------------------------------------------
    // End of Instance data
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------
    public JmsQueueListener() {
    }
    //---------------------------------------------------------------------
    // End of Constructors
    //---------------------------------------------------------------------

    /**
     * This method is part of InitializingBean interface. Properties are validated and
     * the queue connection started.
     *
     * @throws Exception any exception
     */
    public void afterPropertiesSet() throws Exception {
        if (!super.isValid()) {
            throw new IllegalArgumentException(
                    "JmsQueueListener: one or more of the required properties were not set or are nulls. ");
        }

        final JmsP2PTemplate template;
        if (super.getConnectionFactory() != null) {
            template = new JmsP2PTemplate((QueueConnectionFactory) super.getConnectionFactory());
        } else {
            template = new JmsP2PTemplate(super.getConnectionFactoryName());
        }

        this.startConnection(template);
    }

    /**
     * Starts a queue connection by calling the appropriate method from the template object.
     *
     * @param template a jms point-to-point template
     */
    private void startConnection(final JmsP2PTemplate template) {
        if (super.getDestination() == null) {
            this.queueConnection = template.receive(super.getDestinationName(),
                    super.getListener(),
                    super.getMessageSelector(),
                    super.getJmsAuthenticationUserName(),
                    super.getJmsAuthenticationPassword());
        } else {
            this.queueConnection = template.receive((Queue) super.getDestination(),
                    super.getListener(),
                    super.getMessageSelector(),
                    super.getJmsAuthenticationUserName(),
                    super.getJmsAuthenticationPassword());
        }
    }

    /**
     * Comes from DisposableBean interface. Just closes the queue connection.
     *
     * @throws Exception any exception
     */
    public void destroy() throws Exception {
        if (this.queueConnection != null) {
            this.queueConnection.close();
        }
    }
}
