/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms.support;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.Destination;
import javax.naming.NamingException;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.QueueSessionCallback;
import org.springframework.jms.JmsSender;
import org.springframework.jms.TopicSessionCallback;
import org.springframework.jndi.JndiTemplate;

/**
 * Simple implementation of the JmsAdmin interface.
 * 
 * @author Mark Pollack
 */
public class DefaultJmsAdmin implements JmsAdmin {

	protected final Log logger = LogFactory.getLog(getClass());

	protected Map _queueMap;

	protected Map _topicMap;

	public DefaultJmsAdmin() {
		_queueMap = Collections.synchronizedMap(new HashMap());
		_topicMap = Collections.synchronizedMap(new HashMap());
	}

	//TODO this should maybe be done better..see AbstractJndiLocator
	private final JndiTemplate _jndiTemplate = new JndiTemplate();

	protected JmsSender _jmsSender;

	/**
	 * Create a dynamic queue.
	 * @see org.springframework.jms.support.JmsAdmin#createQueue(org.springframework.jms.support.QueueInfo)
	 */
	public QueueInfo createQueue(final QueueInfo queueInfo)
		throws JmsException {
		getJmsSender().execute(new QueueSessionCallback() {
			public void doInJms(QueueSession s) throws JMSException {
				//TODO look into side effects of calling twice
				Queue q = s.createQueue(queueInfo.getName());
				logger.info(
					"Created dynamic queue with name = " + queueInfo.getName());
				_queueMap.put(queueInfo.getName(), q);
			}
		});
		return queueInfo;
	}

	/**
	 * 
	 * @see org.springframework.jms.support.JmsAdmin#destroyQueue(java.lang.String)
	 */
	public void destroyQueue(String queueName) {
		//TODO very vendor dependent....
	}

	/**
	 * Create a dynamic topic
	 * @see org.springframework.jms.support.JmsAdmin#createTopic(org.springframework.jms.support.TopicInfo)
	 */
	public TopicInfo createTopic(final TopicInfo topicInfo) {
		getJmsSender().execute(new TopicSessionCallback() {
			public void doInJms(TopicSession s) throws JMSException {
				//TODO look into side effects of calling twice
				Topic t = s.createTopic(topicInfo.getName());
				logger.info(
					"Created dynamic topic with name = " + topicInfo.getName());
				_topicMap.put(topicInfo.getName(), t);
			}
		});
		return topicInfo;
	}

	/**
	 * 
	 * @see org.springframework.jms.support.JmsAdmin#destroyTopic(java.lang.String)
	 */
	public void destroyTopic(String topicName) {
		// TODO very vendor specific....

	}

	/**
	 * Return either a dynamic topic or queue destination.
	 * @param destinationName name of the dynamic destination.
	 * @return The JMS destination. Null if not found.
	 * @see org.springframework.jms.support.JmsAdmin#lookup(java.lang.String)
	 */
	public Destination lookup(String destinationName) {
		if (_queueMap.containsKey(destinationName)) {
			return (Destination) _queueMap.get(destinationName);
		} else if (_topicMap.containsKey(destinationName)) {
			return (Destination) _topicMap.get(destinationName);
		} else {
			return null;
		}
	}

	/**
	 * @{inheritDoc}
	 */
	public Destination lookup(
		String destName,
		boolean createDynamic,
		boolean isPubSubDomain) {

		//TODO code to deal with JNDI prefix.
		Destination dest = null;
		if (createDynamic) {
			if (isPubSubDomain) {
				dest = lookupDynamicTopic(destName);
			} else {
				dest = lookupDynamicQueue(destName);
			}
		}
		if (dest == null) {
			try {
				dest = (Destination) _jndiTemplate.lookup(destName);
				if (logger.isInfoEnabled()) {
					logger.info(
						"Looked up destination with name ["
							+ destName
							+ "]"
							+ " in JNDI");
				}
			} catch (NamingException e) {
				if (createDynamic) {
					if (isPubSubDomain)
					{
						createTopic(new TopicInfo(destName));
					} else
					{
						createQueue(new QueueInfo(destName));
					}
				} else {
					throw new JmsException(
						"Couldn't get destination name [" + destName + "] from JNDI",
						e);
				}
			}
		}
		return dest;
	}

	/**
	 * @{inheritDoc}
	 */
	public Topic lookupDynamicTopic(String topicName) {
		return (Topic) _topicMap.get(topicName);
	}

	/**
	 * @{inheritDoc}
	 */
	public Queue lookupDynamicQueue(String queueName) {
		return (Queue) _queueMap.get(queueName);
	}

	/**
	 * @return
	 */
	public JmsSender getJmsSender() {
		return _jmsSender;
	}

	/**
	 * @param sender
	 */
	public void setJmsSender(JmsSender sender) {
		_jmsSender = sender;
	}

	/**
	 * Set the JNDI environment to use for the JNDI lookup.
	 * Creates a JndiTemplate with the given environment settings.
	 * @see #setJndiTemplate
	 */
	public final void setJndiEnvironment(Properties jndiEnvironment) {
		_jndiTemplate.setEnvironment(jndiEnvironment);
	}

	/**
	 * Return the JNDI enviromment to use for the JNDI lookup.
	 */
	public final Properties getJndiEnvironment() {
		return _jndiTemplate.getEnvironment();
	}

}
