/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TopicConnectionFactory;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the JmsSender interface that uses the 
 * JMS 1.0.2 specification.  You must specify the domain or
 * style of messaging to be either Point-to-Point (Queues) 
 * or Publish/Subscribe(Topics) The default is to use the Point-to-Point domain.   
 * The property 'setPubSubDomain(boolean)' to true will configure 
 * the JmsSender to use the Pub/Sub domain (Topics).
 * This boolean property is an implementation detail due to the use of similar
 * but seperate class heirarchies in the JMS 1.0.2 API.  JMS 1.1
 * provides a new domain independent API that allows for easy 
 * mix-and-match use of Point-to-Point and Publish/Subscribe domain.
 */
public class JmsSender102 extends AbstractJmsSender {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * By default the JmsSender uses the Point-to-Point domain.
	 */
	private boolean _isPubSub = false;

	/**
	 * Construct a new JmsSender for bean usage.
	 * Note: The ConnectionFactory has to be set before using the instance.
	 * This constructor can be used to prepare a JmsSender via a BeanFactory,
	 * typically setting the ConnectionFactory via setConnectionFactory.
	 * @see #setConnectionFactory
	 */
	public JmsSender102() {
	}

	/**
	 * Set the JmsSender to be for the Publish/Subscribe domain only. This
	 * implies the use of Topics.
	 * @param isTopic 
	 */
	public void setPubSubDomain(boolean isPubSub) {
		_isPubSub = isPubSub;
	}

	/**
	 * Returns true if the destination(s) of the JmsSender is to a topic.
	 * False indicates sending to a Queue which is the default destination
	 * type.
	 * @return true if the destination(s) is a Topic.
	 */
	public boolean isPubSubDomain() {
		return _isPubSub;
	}

	/**
	 * Construct a new JmsSender, given a ConnectionFactory instance 
	 * to obtain connections from.
	 * @param cf The JMS ConnectionFactory to obtain connections from
	 */
	public JmsSender102(ConnectionFactory cf) {
		setConnectionFactory(cf);
	}

	/* TODO
	public JmsSender102(String connectionFactoryJndiName)
	{
		
	}
	*/

	public void send(String jndiDestinationName, MessageCreator messageCreator)
		throws JmsException {
		if (isPubSubDomain()) {
			//TODO
		} else {
			sendP2P(jndiDestinationName, messageCreator);
		}
	}

	

	/**
	 * @param jndiDestinationName
	 * @param messageCreator
	 */
	private void sendP2P(String queueName, MessageCreator messageCreator) {
		//TODO code to deal with JNDI prefix.

		QueueConnection queueConnection = null;
		try {
			//TODO provide our own stand alone connectionfactory impl to
			//set the username, password, and client id can be set on the 
			//connection.  This seems to be how it is done in administration
			//of j2ee apps...otherwise move username,password into this class.
			queueConnection =
				((QueueConnectionFactory) getConnectionFactory())
					.createQueueConnection();

			//TODO think if properties in the sender class is the
			//place to hold this config information.
			QueueSession queueSession =
				queueConnection.createQueueSession(
					isSessionTransacted(),
					getSessionAcknowledgeMode());

			final Queue queue = (Queue) super.lookupJndiResource(queueName);
			logger.info("Looked up queue with name [" + queueName + "]");
			QueueSender queueSender = queueSession.createSender(queue);
			Message message = messageCreator.createMessage(queueSession);
			//TODO put in pretty printer.
			logger.info("Message created was [" + message + "]");

			
			queueSender.send(queue, message);
			logger.info("Message sent OK");

		} catch (NamingException e) {
			throw new JmsException(
				"Couldn't get queue name [" + queueName + "]",
				e);

		} catch (JMSException e) {
			throw new JmsException(
				"Couln't create JMS QueueConnection or QueueSession",
				e);
		}
	}

	/**
	 * In addition to checking if the connection factory is set, make sure
	 * that the supplied connection factory is of the appropriate type for
	 * the specified destination type.  QueueConnectionFactory for queues
	 * and TopicConnectionFactory for topics.
	 */
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		//Make sure that the ConnectionFactory passed is consistent
		//with sending on the specifies destination type.
		//Some providers implementations of the ConnectionFactory implement both
		//domain interfaces under the cover, so just check if the selected
		//domain is consistent with the type of connection factory.
		if (isPubSubDomain()) {
			if (getConnectionFactory() instanceof TopicConnectionFactory) {
				//Do nothing, all ok.
			} else {
				throw new IllegalArgumentException("Specified a Spring JMS 1.0.2 Sender for topics but did not supply an instance of a TopicConnectionFactory");
			}
		} else {
			if (getConnectionFactory() instanceof QueueConnectionFactory) {
				//Do nothing, all ok.
			} else {
				throw new IllegalArgumentException("Specified a Spring JMS 1.0.2 Sender for queues but did not supply an instance of a QueueConnectionFactory");
			}
		}

	}

}
