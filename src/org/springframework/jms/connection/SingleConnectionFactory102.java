package org.springframework.jms.connection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

/**
 * A subclass of SingleConnectionFactory that uses the JMS 1.0.2 specification,
 * rather than the JMS 1.1 methods used by SingleConnectionFactory itself.
 * This class can be used for JMS 1.0.2 providers, offering the same API as
 * SingleConnectionFactory does for JMS 1.1 providers.
 *
 * <p>You need to set the pubSubDomain property accordingly, as this class
 * will always create either a QueueConnection or a TopicConnection.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setTargetConnectionFactory
 * @see #setPubSubDomain
 */
public class SingleConnectionFactory102 extends SingleConnectionFactory {

	private boolean pubSubDomain = false;

	/**
	 * Create a new SingleConnectionFactory102 for bean-style usage.
	 */
	public SingleConnectionFactory102() {
		super();
	}

	/**
	 * Create a new SingleConnectionFactory102 that always returns a single
	 * Connection that it will lazily create via the given target
	 * ConnectionFactory.
	 * @param connectionFactory the target ConnectionFactory
	 * @param pubSubDomain whether the Publish/Subscribe domain (Topics) or
	 * Point-to-Point domain (Queues) should be used
	 */
	public SingleConnectionFactory102(ConnectionFactory connectionFactory, boolean pubSubDomain) {
		setTargetConnectionFactory(connectionFactory);
		this.pubSubDomain = pubSubDomain;
		afterPropertiesSet();
	}

	/**
	 * Configure the factory with knowledge of the JMS domain used.
	 * This tells the JMS 1.0.2 provider which class hierarchy to use
	 * for creating Connections. Default is Point-to-Point (Queues).
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
	 * In addition to checking if the connection factory is set, make sure
	 * that the supplied connection factory is of the appropriate type for
	 * the specified destination type: QueueConnectionFactory for queues,
	 * and TopicConnectionFactory for topics.
	 */
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		// Make sure that the ConnectionFactory passed is consistent.
		// Some provider implementations of the ConnectionFactory interface
		// implement both domain interfaces under the cover, so just check if
		// the selected domain is consistent with the type of connection factory.
		if (isPubSubDomain()) {
			if (!(getTargetConnectionFactory() instanceof TopicConnectionFactory)) {
				throw new IllegalArgumentException(
						"Specified a Spring JMS 1.0.2 SingleConnectionFactory for topics " +
						"but did not supply an instance of TopicConnectionFactory");
			}
		}
		else {
			if (!(getTargetConnectionFactory() instanceof QueueConnectionFactory)) {
				throw new IllegalArgumentException(
						"Specified a Spring JMS 1.0.2 SingleConnectionFactory for queues " +
						"but did not supply an instance of QueueConnectionFactory");
			}
		}
	}

	/**
	 * This implementation overrides the superclass method to use JMS 1.0.2 API.
	 */
	protected Connection doCreateConnection() throws JMSException {
		if (isPubSubDomain()) {
			return ((TopicConnectionFactory) getTargetConnectionFactory()).createTopicConnection();
		}
		else {
			return ((QueueConnectionFactory) getTargetConnectionFactory()).createQueueConnection();
		}
	}

}
