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

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.converter.JmsConverter;
import org.springframework.jms.support.DefaultJmsAdmin;
import org.springframework.jms.support.JmsAdmin;
import org.springframework.util.ClassUtils;

/**
 * Base class for JmsSenders defining commons operations like
 * setting/getting the connection factory, session parameters,
 * and checking that all required bean properites have been set.
 * 
 * Default settings for JMS sessions are transacted and
 * auto acknowledge.  As per section 17.3.5 of the EJB specification,
 * the transaction and acknowledgement parameters are ignored 
 * when a JMS Session is created inside the container environment.
 * 
 * Default setting for isEnabledDynamicDestinations is false.
 * 
 * Default setting for isPubSubDomain is false.  Point-to-Point (Queues)
 * is the default domain.
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 * 
 */
public abstract class AbstractJmsSender implements JmsSender, InitializingBean {

	/**
	 * Used to obtain JMS connections.
	 */
	private ConnectionFactory _cf;
	
	/**
	 * The JMS Converter to use for send(object) methods.
	 */
	private JmsConverter _jmsConverter;
	
    /**
     * Default transaction mode for a JMS Session. 
     */
	private boolean _sessionTransacted = true;
	
    /**
     * Default ack mode for a JMS Session.
     */
	private int _sessionAcknowledgeMode =  Session.AUTO_ACKNOWLEDGE;
	
    
    /**
     * The default destination to use on send operations that do not specify an explicit destination.
     */
    private Destination _defaultDestination;
    
    /**
     * Delegate mangement of JNDI lookups and dynamic destination creation to a JmsAdmin implementation.
     */
	private JmsAdmin _jmsAdmin;
    
    /**
     * The delivery mode to use when sending a message.  Only used if isExplicitQosEnabled = true.
     * 
     */
    private int _deliveryMode;
    
    /**
     * The priority of the message.  Only used if isExplicitQosEnabled = true.
     */
    private int _priority;
	
    /**
     * The message's lifetime in milliseconds.  Only used if isExplicitQosEnabled = true.
     */
    private long _timeToLive;
    
    /**
     * Use the default or explicit QOS parameters.
     */
    private boolean _explicitQosEnabled;
    
	/**
	 * Enable creation of dynamic destinations.
	 */
	private boolean _enabledDynamicDestinations = false;
    
	/**
	 * By default usee the Point-to-Point domain.
	 */
	private boolean _isPubSubDomain = false;
	    
    protected final Log logger = LogFactory.getLog(getClass());
    
    
	/**
     * Return the connection factory used sending messages.
	 * @return the connection factory.
	 */
	public ConnectionFactory getConnectionFactory() {
		return _cf;
	}

	/**
     * Set the connection factory used for sending messages.
	 * @param cf the connection factory.
	 */
	public void setConnectionFactory(ConnectionFactory cf) {
		_cf = cf;
	}


	/**
	 * Make sure the connection factory has been set.
	 *
	 */
	public void afterPropertiesSet() {
		if (_cf == null) {
			throw new IllegalArgumentException("ConnectionFactory is required");
		}
		if (_jmsAdmin == null)
		{
			logger.info("Using DefaultJmsAdmin implementation");
			//TODO This should be a singleton......since it has a cache of
			//dynamic jms destinations.  Maybe place all lookup of destinations
			//here and have it configured to use a JNDILookup template?
			DefaultJmsAdmin admin = new DefaultJmsAdmin();
			//TODO bad smell....
			admin.setJmsSender(this);
			setJmsAdmin(admin);
		}
	}

    /**
     * Determine if acknowledgement mode of the JMS session used for sending a message. 
     * @return The ack mode used for sending a message.
     */
	public int getSessionAcknowledgeMode() {
		return _sessionAcknowledgeMode;
	}
    
    /**
     * Set the JMS acknowledgement mode that is used when creating a JMS session to send
     * a message.  Vendor extensions to the acknowledgment mode can be set here as well.
     * 
     * Note that that inside an ejb the parameters to 
     * create<Queue|Topic>Session(boolean transacted, int acknowledgeMode) method are not 
     * taken into account.  Depending on the tx context in the ejb, the container makes its own
     * decisions on these values.  See section 17.3.5 of the EJB Spec.
     *  
     * @param ackMode The acknowledgement mode.
     */
    public void setSessionAcknowledgeMode(int ackMode) {
        _sessionAcknowledgeMode = ackMode;
    }

	/**
     * Determine if the JMS session used for sending a message is transacted.
	 * @return Return true if using a transacted JMS session, false otherwise.
	 */
	public boolean isSessionTransacted() {
		return _sessionTransacted;
	}

    /**
     * Set the transaction mode that is used when creating a JMS session to send a message.
     * 
     * Note that that inside an ejb the parameters to 
     * create<Queue|Topic>Session(boolean transacted, int acknowledgeMode) method are not 
     * taken into account.  Depending on the tx context in the ejb, the container makes its own
     * decisions on these values.  See section 17.3.5 of the EJB Spec. 
     * @param txMode The transaction mode.
     */
	 public void setSessionTransacted(boolean txMode) {
		_sessionTransacted = txMode;
	 }

	/**
	 * {@inheritDoc}
	 */
	public JmsAdmin getJmsAdmin() {
		return _jmsAdmin;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setJmsAdmin(JmsAdmin admin) {
		_jmsAdmin = admin;
	}

	/**
	 * If a destination name is not found in JNDI, then it will
	 * be created dynamically.
	 * @return true if enabled.
	 */
	public boolean isEnabledDynamicDestinations() {
		return _enabledDynamicDestinations;
	}

	/**
	 * Set the ability of JmsSender to create dynamic destinations
	 * if the destination name is not found in JNDI.
	 * @param b true to enable.
	 */
	public void setEnabledDynamicDestinations(boolean b) {
		_enabledDynamicDestinations = b;
	}

	/**
	 * Configure the JmsSender with knowledge of the JMS Domain used.
	 * For the JMS 1.0.2 based senders this tells the JMS 1.0.2 which
	 * class hierarchy to use in the implementation of the various
	 * send and execute methods.  For the JMS 1.1 based senders it
	 * tells what type of destination to create if dynamic destinations
	 * are enabled.
	 * @return true if the Publish/Subscribe domain (Topics) are used.
	 * otherwise the Point-to-Point domain (Queues) are used.
	 */
	public boolean isPubSubDomain() {
		return _isPubSubDomain;
	}

	/**
	 * Set the type of domain the sender is configured for.  See 
	 * {@link #isPubSubDomain() isPubSubDomain} for more information.
	 * @param b true for Publish/Subscribe domain (Topics) false for
	 * Point-to-Point domain (Queues)
	 */
	public void setPubSubDomain(boolean b) {
		_isPubSubDomain = b;
	}

    public int getDeliveryMode()
    {
        return _deliveryMode;
    }

    public boolean isExplicitQosEnabled()
    {
        return _explicitQosEnabled;
    }

    public int getPriority()
    {
        return _priority;
    }

    public long getTimeToLive()
    {
        return _timeToLive;
    }

    /**
     * Set the delivery mode to use.
     * @param i the delivery mode.
     */
    public void setDeliveryMode(int i)
    {
        _deliveryMode = i;
    }

    /**
     * Set if the QOS values (deliveryMode, priority, timeToLive) should be used for
     * sending a message.
     * @param b true to use the values, false not to use.
     */
    public void setExplicitQosEnabled(boolean b)
    {
        _explicitQosEnabled = b;
    }

    /**
     * Set the priority of the message to be send.
     * @param priority of the message.
     */
    public void setPriority(int priority)
    {
        _priority = priority;
    }

    /**
     * Set the message's lifetime in milliseconds.
     * @param timeToLive message's lifetime.
     */
    public void setTimeToLive(long timeToLive)
    {
        _timeToLive = timeToLive;
    }

    /**
     * Return the default destination to send message sto when using send methods that do no 
     * specifiy the destionation.
     * @return the default destination
     */
    public Destination getDefaultDestination()
    {
        return _defaultDestination;
    }

    public void setDefaultDestination(Destination destination)
    {
        _defaultDestination = destination;
    }
    
    /**
     * Converts the specified checked {@link javax.jms.JMSException JMSException} to
     * a Spring runtime {@link org.springframework.jms.JmsException JmsException}
     * equivalent.
     * @param task readable text describing the task being attempted
     * @param orig The original checked JMSException to wrap
     * @return the Spring runtime JmsException wrapping <code>orig</code>.
     */
    public final JmsException convertJMSException(String task, JMSException orig ) {

        if (logger.isInfoEnabled()) {
            logger.info("Translating JMSException with errorCode '" + orig.getErrorCode() + "' and message [" +
                                    orig.getMessage() + "]; for task [" + task + "]");
        }
        
        if ( orig instanceof JMSSecurityException ) {
            return new JmsSecurityException( orig );
        }
        

        // all other exceptions in our Jms runtime exception hierarchy have the
        // same unqualified names as their javax.jms counterparts, so just
        // construct the converted exception dynamically based on name:
        String shortName = ClassUtils.getShortName( orig.getClass().getName() );

        //all JmsException subclasses are in the same package:
        String longName = JmsException.class.getPackage().getName() + "." + shortName;

        try {
            Class clazz = Class.forName( longName );
            Constructor ctor =
                clazz.getConstructor( new Class[]{Throwable.class} );
            Object counterpart = ctor.newInstance( new Object[]{orig} );
            return (JmsException)counterpart;
        } catch ( Exception e ) {
			throw new IllegalStateException( "Unable to instantiate class [" +
                                             longName + "]", e );
        }
    }

    public JmsConverter getJmsConverter() {
        return _jmsConverter;
    }

    /**
     * Set the converter to use
     * @param converter The JMS converter
     */
    public void setJmsConverter(JmsConverter converter) {
        _jmsConverter = converter;
    }

}
