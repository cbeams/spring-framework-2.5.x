package org.springframework.jms;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

import javax.jms.MessageListener;

/**
 * Abstract listener defines common instance members and functionality for
 * the actual topic and queue listeners.
 * The isValid method if called will return true only if the listener field is set,
 * connection factory object or connection name fields are set,and destination
 * object or destination name are set.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public abstract class JmsAbstractListener implements InitializingBean, DisposableBean {
    //could be javax.jms.TopicConnectioFactory or javax.jms.QueueConnectioFactory,
    //cannot be null if connectionFactoryName is also null
    private Object connectionFactory;
    private String connectionFactoryName;
    //cannot be null
    private MessageListener listener;
    //selector can be null
    private String messageSelector;
    //could be a topic or a queue name, not null
    private String destinationName;
    //this could be a javax.jms.Topic or a javax.jms.Queue object
    private Object destination;
    //user name can be null
    private String jmsAuthenticationUserName;
    //password can also be null
    private String jmsAuthenticationPassword;

    public final void setConnectionFactory(final Object connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public final void setConnectionFactoryName(final String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    public final void setListener(final MessageListener listener) {
        this.listener = listener;
    }

    public final void setMessageSelector(final String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public final void setDestinationName(final String destinationName) {
        this.destinationName = destinationName;
    }

    public final void setDestination(final Object destination) {
        this.destination = destination;
    }

    public final void setJmsAuthenticationUserName(final String jmsAuthenticationUserName) {
        this.jmsAuthenticationUserName = jmsAuthenticationUserName;
    }

    public final void setJmsAuthenticationPassword(final String jmsAuthenticationPassword) {
        this.jmsAuthenticationPassword = jmsAuthenticationPassword;
    }

    protected final Object getConnectionFactory() {
        return this.connectionFactory;
    }

    protected final String getConnectionFactoryName() {
        return this.connectionFactoryName;
    }

    protected final MessageListener getListener() {
        return this.listener;
    }

    protected final String getMessageSelector() {
        return this.messageSelector;
    }

    protected final String getDestinationName() {
        return this.destinationName;
    }

    protected final Object getDestination() {
        return this.destination;
    }

    protected final String getJmsAuthenticationUserName() {
        return this.jmsAuthenticationUserName;
    }

    protected final String getJmsAuthenticationPassword() {
        return this.jmsAuthenticationPassword;
    }

    /**
     * Tests if all the required properties are set.
     *
     * @return whether it's safe to continue or not.
     */
    protected final boolean isValid() {
        return (this.getListener() != null &&
                (this.getConnectionFactory() != null || this.getConnectionFactoryName() != null) &&
                (this.getDestinationName() != null || this.getDestination() != null)
                );
    }

    /**
     * This method is called by the framework after all the properties have been set.
     *
     * @throws Exception any exception thrown
     */
    public abstract void afterPropertiesSet() throws Exception;

    /**
     * Releases allocated resources such as JMS connections.
     *
     * @throws Exception any exception thrown
     */
    public abstract void destroy() throws Exception;
}
