/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.support.DefaultJmsAdmin;
import org.springframework.jms.support.JmsAdmin;

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
 */
public abstract class AbstractJmsSender implements JmsSender, InitializingBean {

	/**
	 * Used to obtain JMS connections.
	 */
	private ConnectionFactory _cf;
	
	private boolean _sessionTransacted = true;
	
	private int _sessionAcknowledgeMode =  Session.AUTO_ACKNOWLEDGE;
	
	private JmsAdmin _jmsAdmin;
	
	/**
	 * 
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

}
