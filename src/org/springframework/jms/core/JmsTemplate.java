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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

/**
 * Helper class that simplifies JMS access code. This class requires a
 * JMS 1.1 provider, because it builds on the new domain-independent API.
 * Use the JmsTemplate102 subclass for JMS 1.0.2 providers.
 *
 * <p>If you want to use dynamic destination creation, you must specify
 * the type of JMS destination to create using the method
 * {@link JmsTemplate#setPubSubDomain setPubSubDomain}. For other usages,
 * this is not necessary, in contrast to when working with JmsTemplate102.
 * Point-to-Point (Queues) is the default domain.
 *
 * <p>Default settings for JMS sessions are not transacted and auto-acknowledge.
 * As per section 17.3.5 of the EJB specification, the transaction and
 * acknowledgement parameters are ignored when a JMS Session is created
 * inside the container environment.
 *
 * @author Mark Pollack
 * @author Juergen Hoeller
 */
public class JmsTemplate implements JmsOperations, DynamicDestinationFactory, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());


	/**
	 * Used to obtain JMS connections.
	 */
	private ConnectionFactory connectionFactory;

	/**
	 * By default uses the Point-to-Point domain.
	 */
	private boolean pubSubDomain = false;

	/**
	 * Default transaction mode for a JMS Session.
	 */
	private boolean sessionTransacted = false;

	/**
	 * Default ack mode for a JMS Session.
	 */
	private int sessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;


	/**
	 * The default destination to use on send operations that do not specify an explicit destination.
	 */
	private Destination defaultDestination;

	/**
	 * Delegate management of JNDI lookups and dynamic destination creation
	 * to a DestinationResolver implementation.
	 */
	private DestinationResolver destinationResolver;

	/**
	 * The messageConverter to use for send(object) methods.
	 */
	private MessageConverter messageConverter;


	/**
	 * Use the default or explicit QOS parameters.
	 */
	private boolean explicitQosEnabled;

	/**
	 * The delivery mode to use when sending a message. Only used if isExplicitQosEnabled = true.
	 */
	private int deliveryMode;

	/**
	 * The priority of the message. Only used if isExplicitQosEnabled = true.
	 */
	private int priority;

	/**
	 * The message's lifetime in milliseconds. Only used if isExplicitQosEnabled = true.
	 */
	private long timeToLive;


	/**
	 * Construct a new JmsTemplate for bean usage.
	 * <p>Note: The ConnectionFactory has to be set before using the instance.
	 * This constructor can be used to prepare a JmsTemplate via a BeanFactory,
	 * typically setting the ConnectionFactory via setConnectionFactory.
	 * @see #setConnectionFactory
	 */
	public JmsTemplate() {
		this.destinationResolver = new DynamicDestinationResolver();
	}

	/**
	 * Construct a new JmsTemplate, given a ConnectionFactory.
	 * @param connectionFactory the ConnectionFactory to obtain connections from
	 */
	public JmsTemplate(ConnectionFactory connectionFactory) {
		this();
		setConnectionFactory(connectionFactory);
		afterPropertiesSet();
	}


	/**
	 * Set the connection factory used for sending messages.
	 * @param connectionFactory the connection factory
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
    
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * Configure the JmsTemplate with knowledge of the JMS domain used.
	 * For the JMS 1.0.2 based senders this tells the JMS 1.0.2 which
	 * class hierarchy to use in the implementation of the various send
	 * and execute methods. For the JMS 1.1 based senders it does not
	 * affect send methods. In both implementations it tells what type
	 * of destination to create if dynamic destinations are enabled.
	 * @param pubSubDomain true for Publish/Subscribe domain (Topics),
	 * false for Point-to-Point domain (Queues)
	 */
	public void setPubSubDomain(boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	/**
	 * Return whether the Publish/Subscribe domain (Topics) is used.
	 * Otherwise, the Point-to-Point domain (Queues) is used.
	 */
	public boolean isPubSubDomain() {
		return pubSubDomain;
	}

	/**
	 * Set the transaction mode that is used when creating a JMS session to send a message.
	 * <p>Note that that within a JTA transaction, the parameters to
	 * create<Queue|Topic>Session(boolean transacted, int acknowledgeMode) method are not
	 * taken into account. Depending on the J2EE transaction context, the container
	 * makes its own decisions on these values. See section 17.3.5 of the EJB Spec.
	 * @param sessionTransacted the transaction mode
	 */
	public void setSessionTransacted(boolean sessionTransacted) {
		this.sessionTransacted = sessionTransacted;
	}

	/**
	 * Determine if the JMS session used for sending a message is transacted.
	 * @return Return true if using a transacted JMS session, false otherwise.
	 */
	public boolean isSessionTransacted() {
		return sessionTransacted;
	}

	/**
	 * Set the JMS acknowledgement mode that is used when creating a JMS session to send
	 * a message.  Vendor extensions to the acknowledgment mode can be set here as well.
	 * <p>Note that that inside an EJB the parameters to
	 * create<Queue|Topic>Session(boolean transacted, int acknowledgeMode) method are not
	 * taken into account. Depending on the transaction context in the EJB, the container
	 * makes its own decisions on these values. See section 17.3.5 of the EJB Spec.
	 * @param sessionAcknowledgeMode the acknowledgement mode
	 */
	public void setSessionAcknowledgeMode(int sessionAcknowledgeMode) {
		this.sessionAcknowledgeMode = sessionAcknowledgeMode;
	}

	/**
	 * Determine if acknowledgement mode of the JMS session used for sending a message.
	 * @return The ack mode used for sending a message.
	 */
	public int getSessionAcknowledgeMode() {
		return sessionAcknowledgeMode;
	}


	/**
	 * Set the destination to be used on send operations that do not
	 * have a destination parameter.
	 * @param destination the destination to send messages to when
	 * no destination is specified
	 */
	public void setDefaultDestination(Destination destination) {
		this.defaultDestination = destination;
	}

	public Destination getDefaultDestination() {
		return defaultDestination;
	}

	/**
	 * Set the destination resolver for this template. Used to resolve
	 * destination names and to support dynamic destination functionality.
	 * <p>The default resolver is a DynamicDestinationResolver. Specify a
	 * JndiDestinationResolver for resolving destination names as JNDI locations.
	 * @see org.springframework.jms.support.destination.DynamicDestinationResolver
	 * @see org.springframework.jms.support.destination.JndiDestinationResolver
	 */
	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	/**
	 * Get the administration helper class.
	 */
	public DestinationResolver getDestinationResolver() {
		return destinationResolver;
	}

	/**
	 * Set the messageConverter to use when using the send methods that take an Object parameter.
	 * @param messageConverter the JMS messageConverter
	 */
	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	public MessageConverter getMessageConverter() {
		return messageConverter;
	}


	/**
	 * Set if the QOS values (deliveryMode, priority, timeToLive)
	 * should be used for sending a message.
	 */
	public void setExplicitQosEnabled(boolean explicitQosEnabled) {
		this.explicitQosEnabled = explicitQosEnabled;
	}

	/**
	 * If true, then the values of deliveryMode, priority, and timeToLive
	 * will be used when sending a message. Otherwise, the default values,
	 * that may be set administratively, will be used.
	 * @return true if overriding default values of QOS parameters
	 * (deliveryMode, priority, and timeToLive)
	 * @see #setDeliveryMode
	 * @see #setPriority
	 * @see #setTimeToLive
	 */
	public boolean isExplicitQosEnabled() {
		return explicitQosEnabled;
	}

	/**
	 * Set the delivery mode to use when sending a message. Since a default value may be
	 * defined administratively, it is only used when isExplicitQosEnabled equals true.
	 * @param deliveryMode the delivery mode to use
	 * @see #isExplicitQosEnabled
	 */
	public void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	public int getDeliveryMode() {
		return deliveryMode;
	}

	/**
	 * Set the priority of the message when sending. Since a default value may be defined
	 * administratively, it is only used when isExplicitQosEnabled equals true.
	 * @param priority the priority of the message
	 * @see #isExplicitQosEnabled
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Set the timetoLive of the message when sending. Since a default value may be defined
	 * administratively, it is only used when isDefaultQosEnabled equals true.
	 * @param timeToLive the message's lifetime (in milliseconds)
	 * @see #isExplicitQosEnabled
	 */
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public long getTimeToLive() {
		return timeToLive;
	}


	/**
	 * Make sure the connection factory has been set.
	 */
	public void afterPropertiesSet() {
		if (this.connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}
	}


	/**
	 * Create a JMS Connection via this template's ConnectionFactory.
	 * <p>This implementation uses JMS 1.1 API.
	 * @return the new JMS Connection
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Connection createConnection() throws JMSException {
		return getConnectionFactory().createConnection();
	}

	/**
	 * Create a JMS Session for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Session createSession(Connection con) throws JMSException {
		return con.createSession(isSessionTransacted(), getSessionAcknowledgeMode());
	}

	/**
	 * Create a JMS MessageProducer for the given Session and Destination.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to create a MessageProducer for
	 * @param destination the JMS Destination to create a MessageProducer for
	 * @return the new JMS MessageProducer
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected MessageProducer createProducer(Session session, Destination destination) throws JMSException {
		return session.createProducer(destination);
	}


	/**
	 * Resolve the given destination name into a JMS Destination,
	 * via this template's DestinationResolver.
	 * @param destinationName the name of the destination
	 * @return the located Destination
	 * @throws JmsException if resolution failed
	 * @see #setDestinationResolver
	 */
	protected Destination resolveDestinationName(String destinationName) throws JmsException {
		return getDestinationResolver().resolveDestinationName(destinationName, isPubSubDomain(), this);
	}

	/**
	 * Convert the specified checked {@link javax.jms.JMSException JMSException} to
	 * a Spring runtime {@link org.springframework.jms.JmsException JmsException}
	 * equivalent.
	 * <p>Default implementation delegates to JmsUtils.
	 * @param task readable text describing the task being attempted
	 * @param ex the original checked JMSException to convert
	 * @return the Spring runtime JmsException wrapping <code>ex</code>
	 * @see org.springframework.jms.support.JmsUtils#convertJmsAccessException
	 */
	protected JmsException convertJmsAccessException(String task, JMSException ex) {
		if (logger.isInfoEnabled()) {
			logger.info("Translating JMSException with errorCode '" + ex.getErrorCode() +
			             "' and message [" + ex.getMessage() + "]; for task [" + task + "]");
		}
		return JmsUtils.convertJmsAccessException(ex);
	}


	/**
	 * Execute the action specified by the given action object within a
	 * JMS Session. Generalized version of execute(SessionCallback),
	 * allowing to start the JMS Connection on the fly.
	 * <p>Use execute(SessionCallback) for the general case. Starting
	 * the JMS Connection is just necessary for receiving messages,
	 * which is preferably achieve through the <code>receive</code> methods.
	 * @param action callback object that exposes the session.
	 * @return The result object from working with the session.
	 * @throws JmsException if there is any problem
	 * @see #execute(SessionCallback)
	 * @see #receive
	 */
	public Object execute(SessionCallback action, boolean startConnection) throws JmsException {
		Connection con = null;
		Session session = null;
		try {
			con = createConnection();
			if (startConnection) {
				con.start();
			}
			session = createSession(con);
			return action.doInJms(session);
		}
		catch (JMSException ex) {
			throw convertJmsAccessException("SessionCallback", ex);
		}
		finally {
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(con);
		}
	}

	public Object execute(SessionCallback action) throws JmsException {
		return execute(action, false);
	}

	public Object execute(ProducerCallback action) throws JmsException {
		Connection con = null;
		Session session = null;
		try {
			con = createConnection();
			session = createSession(con);
			MessageProducer producer = createProducer(session, null);
			return action.doInJms(session, producer);
		}
		catch (JMSException ex) {
			throw convertJmsAccessException("ProducerCallback", ex);
		}
		finally {
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(con);
		}
	}


	public void send(MessageCreator messageCreator) throws JmsException {
		if (this.getDefaultDestination() == null) {
			throw new IllegalStateException("No default destination was specified. Check configuration of JmsTemplate.");
		}
		send(getDefaultDestination(), messageCreator);
	}

	public void send(Destination destination, MessageCreator messageCreator) throws JmsException {
		Connection con = null;
		Session session = null;
		try {
			con = createConnection();
			session = createSession(con);
			MessageProducer producer = createProducer(session, destination);
			Message message = messageCreator.createMessage(session);
			if (logger.isInfoEnabled()) {
				logger.info("Message created was [" + message + "]");
			}
			doSend(producer, message);
			if (isSessionTransacted()) {
				session.commit();
			}
		}
		catch (JMSException ex) {
			throw convertJmsAccessException("Send message on destination [" + destination + "]", ex);
		}
		finally {
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(con);
		}
	}

	public void send(String destinationName, MessageCreator messageCreator) throws JmsException {
		send(resolveDestinationName(destinationName), messageCreator);
	}

	protected void doSend(MessageProducer producer, Message message) throws JMSException {
		if (isExplicitQosEnabled()) {
			producer.send(message, getDeliveryMode(), getPriority(), getTimeToLive());
		}
		else {
			producer.send(message);
		}
	}


	public void convertAndSend(Object message) throws JmsException {
		if (this.getDefaultDestination() == null) {
			throw new IllegalStateException("No default destination was specified. Check configuration of JmsTemplate.");
		}
		convertAndSend(getDefaultDestination(), message);
	}

	public void convertAndSend(Destination destination, final Object message) throws JmsException {
		if (this.getMessageConverter() == null) {
			throw new IllegalStateException("No JmsConverter. Check configuration of JmsTemplate.");
		}
		send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return getMessageConverter().toMessage(message, session);
			}
		});
	}

	public void convertAndSend(String destinationName, Object message) throws JmsException {
		convertAndSend(resolveDestinationName(destinationName), message);
	}

	public void convertAndSend(Object message, MessagePostProcessor postProcessor) throws JmsException {
		if (this.getDefaultDestination() == null) {
			throw new IllegalStateException("No default destination was specified. Check configuration of JmsTemplate.");
		}
		convertAndSend(getDefaultDestination(), message, postProcessor);
	}

	public void convertAndSend(Destination destination, final Object message,
	                           final MessagePostProcessor postProcessor) throws JmsException {
		if (this.getMessageConverter() == null) {
			throw new IllegalStateException("No JmsConverter. Check configuration of JmsTemplate.");
		}
		send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				Message m = getMessageConverter().toMessage(message, session);
				return postProcessor.postProcessMessage(m);
			}
		});
	}

	public void convertAndSend(String destinationName, Object message, MessagePostProcessor postProcessor)
	    throws JmsException {
		convertAndSend(resolveDestinationName(destinationName), message, postProcessor);
	}


	public Message receive(final Destination destination, final long timeout) throws JmsException {
		return (Message) execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				MessageConsumer consumer = session.createConsumer(destination);
				try {
					return consumer.receive(timeout);
				}
				finally {
					JmsUtils.closeMessageConsumer(consumer);
				}
			}
		}, true);
	}

	public Message receive(String destinationName, long timeout) throws JmsException {
		return receive(resolveDestinationName(destinationName), timeout);
	}


	public Queue createQueue(final String queueName) throws JmsException {
		return (Queue) execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				// TODO: look into side effects of calling twice
				Queue queue = doCreateQueue(session, queueName);
				if (logger.isInfoEnabled()) {
					logger.info("Created dynamic queue with name '" + queueName + "'");
				}
				return queue;
			}
		});
	}

	public Topic createTopic(final String topicName) throws JmsException {
		return (Topic) execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				// TODO: look into side effects of calling twice
				Topic topic = doCreateTopic(session, topicName);
				if (logger.isInfoEnabled()) {
					logger.info("Created dynamic topic with name '" + topicName + "'");
				}
				return topic;
			}
		});
	}

	/**
	 * Create a new Queue for the given Session.
	 * @param session the JMS Session to create a Queue for
	 * @param queueName the name of the queue
	 * @return the new Queue
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Queue doCreateQueue(Session session, String queueName) throws JMSException {
		return session.createQueue(queueName);
	}

	/**
	 * Create a new Topic for the given Session.
	 * @param session the JMS Session to create a Topic for
	 * @param topicName the name of the topic
	 * @return the new Topic
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Topic doCreateTopic(Session session, String topicName) throws JMSException {
		return session.createTopic(topicName);
	}

}
