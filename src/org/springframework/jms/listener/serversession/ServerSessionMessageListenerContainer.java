/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jms.listener.serversession;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;

/**
 * Message listener container that builds on the JMS ServerSessionPool SPI,
 * creating JMS ServerSessions through a pluggable ServerSessionFactory.
 *
 * <p>This is the most sophisticated message listener container possible
 * with standard JMS. It allows for dynamic management of JMS Sessions,
 * potentially using a pool of Sessions that receive messages in parallel.
 *
 * <p>Note that the default ServerSessionFactory is a SimpleServerSessionFactory,
 * which will create a new ServerSession for each listener execution.
 * Consider specifying a CommonsPoolServerSessionFactory to reuse JMS Sessions
 * and/or to limit the number of concurrent ServerSession executions
 *
 * <p>See the {@link AbstractMessageListenerContainer AbstractMessageListenerContainer}
 * javadoc for details on acknowledge modes and transaction options.
 *
 * <p>For a simpler message listener container, in particular if using
 * a JMS provider without ServerSessionPool support, consider using
 * SimpleMessageListenerContainer.
 *
 * <p>This class requires a JMS 1.1+ provider, because it builds on the
 * domain-independent API. <b>Use the {@link ServerSessionMessageListenerContainer102
 * ServerSessionMessageListenerContainer102} subclass for JMS 1.0.2 providers.</b>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.jms.listener.SimpleMessageListenerContainer
 * @see ServerSessionMessageListenerContainer102
 */
public class ServerSessionMessageListenerContainer extends AbstractMessageListenerContainer
		implements ListenerSessionManager {

	private ServerSessionFactory serverSessionFactory = new SimpleServerSessionFactory();

	private int maxMessagesPerTask = 1;

	private ConnectionConsumer consumer;


	/**
	 * Set the Spring ServerSessionFactory to use.
	 * <p>Default is a plain SimpleServerSessionFactory.
	 * Consider using a CommonsPoolServerSessionFactory to reuse JMS Sessions
	 * and/or to limit the number of concurrent ServerSession executions.
	 * @see SimpleServerSessionFactory
	 * @see org.springframework.jms.listener.serversession.CommonsPoolServerSessionFactory
	 */
	public void setServerSessionFactory(ServerSessionFactory serverSessionFactory) {
		this.serverSessionFactory =
				(serverSessionFactory != null ? serverSessionFactory : new SimpleServerSessionFactory());
	}

	/**
	 * Return the Spring ServerSessionFactory to use.
	 */
	protected ServerSessionFactory getServerSessionFactory() {
		return serverSessionFactory;
	}

	/**
	 * Set the maximum number of messages to load into a JMS Session.
	 * Default is 1.
	 * <p>See the corresponding JMS <code>createConnectionConsumer</code>
	 * argument for details.
	 * @see javax.jms.Connection#createConnectionConsumer
	 */
	public void setMaxMessagesPerTask(int maxMessagesPerTask) {
		this.maxMessagesPerTask = maxMessagesPerTask;
	}

	/**
	 * Return the maximum number of messages to load into a JMS Session.
	 */
	protected int getMaxMessagesPerTask() {
		return maxMessagesPerTask;
	}


	//-------------------------------------------------------------------------
	// Implementation of AbstractMessageListenerContainer's template methods
	//-------------------------------------------------------------------------

	/**
	 * Always use a shared JMS Connection.
	 */
	protected final boolean sharedConnectionEnabled() {
		return true;
	}

	/**
	 * Creates a JMS ServerSessionPool for the specified listener and registers
	 * it with a JMS ConnectionConsumer for the specified destination.
	 * @see #createServerSessionPool
	 * @see #createConsumer
	 */
	protected void registerListener() throws JMSException {
		Connection con = getSharedConnection();
		Destination destination = getDestination();
		if (destination == null) {
			Session session = createSession(con);
			try {
				destination = resolveDestinationName(session, getDestinationName());
			}
			finally {
				JmsUtils.closeSession(session);
			}
		}
		ServerSessionPool pool = createServerSessionPool();
		this.consumer = createConsumer(con, destination, pool);
	}

	/**
	 * Create a JMS ServerSessionPool for the specified message listener,
	 * via this container's ServerSessionFactory.
	 * <p>This message listener container implements the ListenerSessionManager
	 * interface, hence can be passed to the ServerSessionFactory itself.
	 * @see #setServerSessionFactory
	 * @see ServerSessionFactory#getServerSession(ListenerSessionManager)
	 */
	protected ServerSessionPool createServerSessionPool() throws JMSException {
		return new ServerSessionPool() {
			public ServerSession getServerSession() throws JMSException {
				logger.debug("JMS ConnectionConsumer requests ServerSession");
				return getServerSessionFactory().getServerSession(ServerSessionMessageListenerContainer.this);
			}
		};
	}

	/**
	 * Return the JMS ConnectionConsumer used by this message listener container.
	 * Available after initialization.
	 */
	protected final ConnectionConsumer getConsumer() {
		return consumer;
	}

	/**
	 * Close the JMS ServerSessionPool for the specified message listener,
	 * via this container's ServerSessionFactory, and subsequently also
	 * this container's JMS ConnectionConsumer.
	 * <p>This message listener container implements the ListenerSessionManager
	 * interface, hence can be passed to the ServerSessionFactory itself.
	 * @see #setServerSessionFactory
	 * @see ServerSessionFactory#getServerSession(ListenerSessionManager)
	 */
	protected void destroyListener() throws JMSException {
		logger.debug("Closing ServerSessionFactory");
		getServerSessionFactory().close(this);
		logger.debug("Closing JMS ConnectionConsumer");
		this.consumer.close();
	}


	//-------------------------------------------------------------------------
	// Implementation of the ListenerSessionManager interface
	//-------------------------------------------------------------------------

	/**
	 * Create a JMS Session with the specified listener registered.
	 * Listener execution is delegated to the <code>executeListener</code> method.
	 * <p>Default implementation simply calls <code>setMessageListener</code>
	 * on a newly created JMS Session, according to the JMS specification's
	 * ServerSessionPool section.
	 * @return the JMS Session
	 * @throws JMSException if thrown by JMS API methods
	 * @see #executeListener
	 */
	public Session createListenerSession() throws JMSException {
		final Session session = createSession(getSharedConnection());

		session.setMessageListener(new MessageListener() {
			public void onMessage(Message message) {
				executeListener(session, message);
			}
		});

		return session;
	}

	/**
	 * Execute the given JMS Session, triggering invocation
	 * of its listener.
	 * <p>Default implementation simply calls <code>run()</code>
	 * on the JMS Session, according to the JMS specification's
	 * ServerSessionPool section.
	 * @param session the JMS Session to execute
	 */
	public void executeListenerSession(Session session) {
		session.run();
	}


	//-------------------------------------------------------------------------
	// JMS 1.1 factory methods, potentially overridden for JMS 1.0.2
	//-------------------------------------------------------------------------

	/**
	 * Create a JMS ConnectionConsumer for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected ConnectionConsumer createConsumer(Connection con, Destination destination, ServerSessionPool pool)
			throws JMSException {

		if (isSubscriptionDurable() && destination instanceof Topic) {
			return con.createDurableConnectionConsumer(
					(Topic) destination, getDurableSubscriptionName(), getMessageSelector(), pool, getMaxMessagesPerTask());
		}
		else {
			return con.createConnectionConsumer(destination, getMessageSelector(), pool, getMaxMessagesPerTask());
		}
	}

}
