/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.naming.NamingException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiTemplate;

/**
 * Base class for JmsSenders defining commons operations like
 * setting/getting the connection factory, session parameters,
 * and checking that all required bean properites have been set.
 * Default settings for JMS sessions are not transacted and
 * auto acknowledge. 
 */
public abstract class AbstractJmsSender implements JmsSender, InitializingBean {

	/**
	 * Used to obtain JMS connections.
	 */
	private ConnectionFactory _cf;
	
	private boolean _sessionTransacted = true;
	
	private int _sessionAcknowledgeMode =  Session.AUTO_ACKNOWLEDGE;
	
	//TODO this should maybe be done better...see AbstractJndiLocator
	private final JndiTemplate _jndiTemplate = new JndiTemplate();
	
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
	
	/**
	 * Looks up any object in the Jndi context. The consumer of this method is responsible
	 * for the casting to the actual type expected.
	 *
	 * @param name Jndi key that will be used to query the context.
	 * @return Object that needs to be cast to the actual type.
	 * @throws NamingException if nothing is found under the name passed.
	 */
	protected final Object lookupJndiResource(final String name) throws NamingException {
		return _jndiTemplate.lookup(name);
	}
	
	/**
	 * @inheritDoc
	 */
	public ConnectionFactory getConnectionFactory() {
		return _cf;
	}

	/**
	 * @inheritDoc
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
	}
	/**
	 * @return
	 */
	public int getSessionAcknowledgeMode() {
		return _sessionAcknowledgeMode;
	}

	/**
	 * @return
	 */
	public boolean isSessionTransacted() {
		return _sessionTransacted;
	}

	/**
	 * @param i
	 */
	public void setSessionAcknowledgeMode(int i) {
		_sessionAcknowledgeMode = i;
	}

	/**
	 * @param b
	 */
	public void setSessionTransacted(boolean b) {
		_sessionTransacted = b;
	}

}
