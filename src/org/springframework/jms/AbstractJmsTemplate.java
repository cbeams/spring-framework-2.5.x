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

package org.springframework.jms;

import java.lang.reflect.Constructor;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.converter.Converter;
import org.springframework.util.ClassUtils;

/**
 * Base class for JmsSenders defining commons operations like
 * setting/getting the connection factory, session parameters,
 * and checking that all required bean properites have been set.
 *
 * <p>Default settings for JMS sessions are transacted and
 * auto acknowledge.  As per section 17.3.5 of the EJB specification,
 * the transaction and acknowledgement parameters are ignored
 * when a JMS Session is created inside the container environment.
 *
 * <p>Default setting for isEnabledDynamicDestinations is false.
 *
 * <p>Default setting for isSessionTransacted is false.
 *
 * <p>Default setting for pubSubDomain is false.
 * Point-to-Point (Queues) is the default domain.
 *
 * @author Mark Pollack
 */
public abstract class AbstractJmsTemplate implements JmsTemplate, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());


	/**
	 * Used to obtain JMS connections.
	 */
	private ConnectionFactory connectionFactory;

	/**
	 * Delegate mangement of JNDI lookups and dynamic destination creation to a JmsAdmin implementation.
	 */
	private JmsAdmin jmsAdmin;

	/**
	 * By default usee the Point-to-Point domain.
	 */
	private boolean pubSubDomain = false;

	/**
	 * The default destination to use on send operations that do not specify an explicit destination.
	 */
	private Destination defaultDestination;

	/**
	 * The converter to use for send(object) methods.
	 */
	private Converter converter;


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
	 * Enable creation of dynamic destinations.
	 */
	private boolean dynamicDestinationEnabled = false;

	/**
	 * Default ack mode for a JMS Session.
	 */
	private int sessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	/**
	 * Default transaction mode for a JMS Session.
	 */
	private boolean sessionTransacted = false;


	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setJndiEnvironment(Properties jndiEnvironment) {
		getJmsAdmin().setJndiEnvironment(jndiEnvironment);
	}

	public void setJmsAdmin(JmsAdmin jmsAdmin) {
		this.jmsAdmin = jmsAdmin;
	}

	public JmsAdmin getJmsAdmin() {
		return jmsAdmin;
	}

	public void setPubSubDomain(boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	public boolean isPubSubDomain() {
		return pubSubDomain;
	}

	public void setDefaultDestination(Destination destination) {
		this.defaultDestination = destination;
	}

	public Destination getDefaultDestination() {
		return defaultDestination;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public Converter getConverter() {
		return converter;
	}


	/**
	 * Set if the QOS values (deliveryMode, priority, timeToLive)
	 * should be used for sending a message.
	 */
	public void setExplicitQosEnabled(boolean explicitQosEnabled) {
		this.explicitQosEnabled = explicitQosEnabled;
	}

	public boolean isExplicitQosEnabled() {
		return explicitQosEnabled;
	}

	public void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	public int getDeliveryMode() {
		return deliveryMode;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public long getTimeToLive() {
		return timeToLive;
	}


	/**
	 * Set the ability of JmsTemplate to create dynamic destinations
	 * if the destination name is not found in JNDI.
	 */
	public void setDynamicDestinationsEnabled(boolean dynamicDestinationEnabled) {
		this.dynamicDestinationEnabled = dynamicDestinationEnabled;
	}

	/**
	 * If a destination name is not found in JNDI, then it will
	 * be created dynamically.
	 */
	public boolean isDynamicDestinationEnabled() {
		return dynamicDestinationEnabled;
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
	 * Set the transaction mode that is used when creating a JMS session to send a message.
	 * <p>Note that that inside an EJB the parameters to
	 * create<Queue|Topic>Session(boolean transacted, int acknowledgeMode) method are not
	 * taken into account. Depending on the transaction context in the EJB, the container
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
	 * Make sure the connection factory has been set.
	 */
	public void afterPropertiesSet() {
		if (this.connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}
	}


	protected void createDefaultJmsAdmin() {
		DefaultJmsAdmin admin = new DefaultJmsAdmin();
		admin.setJmsTemplate(this);
		setJmsAdmin(admin);
	}

	/**
	 * Converts the specified checked {@link javax.jms.JMSException JMSException} to
	 * a Spring runtime {@link org.springframework.jms.JmsException JmsException}
	 * equivalent.
	 * @param task readable text describing the task being attempted
	 * @param orig The original checked JMSException to wrap
	 * @return the Spring runtime JmsException wrapping <code>orig</code>.
	 */
	public final JmsException convertJMSException(String task, JMSException orig) {
		if (logger.isInfoEnabled()) {
			logger.info("Translating JMSException with errorCode '" + orig.getErrorCode() +
			             "' and message [" + orig.getMessage() + "]; for task [" + task + "]");
		}

		if (orig instanceof JMSSecurityException) {
			return new JmsSecurityException((JMSSecurityException) orig);
		}

		// All other exceptions in our Jms runtime exception hierarchy have the
		// same unqualified names as their javax.jms counterparts, so just
		// construct the converted exception dynamically based on name.
		String shortName = ClassUtils.getShortName(orig.getClass().getName());

		// all JmsException subclasses are in the same package:
		String longName = JmsException.class.getPackage().getName() + "." + shortName;

		try {
			Class clazz = Class.forName(longName);
			Constructor ctor = clazz.getConstructor(new Class[]{Throwable.class});
			Object counterpart = ctor.newInstance(new Object[]{orig});
			return (JmsException) counterpart;
		}
		catch (Exception ex) {
			logger.info("No direct translation to runtime equivalent - rethrowing as JmsException", ex);
			return new JmsException("No translation to runtime equivalent", orig);
		}
	}

}
