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

package org.springframework.jms.remoting;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.Assert;

/**
 * Interceptor for accessing a JMS-based remote service.
 *
 * <p>To be configured with a QueueConnectionFactory and a target queue
 * (either as Queue reference or as queue name).
 *
 * @author Juergen Hoeller
 * @author James Strachan
 * @since 2.0
 * @see #setConnectionFactory
 * @see #setQueue
 * @see #setQueueName
 * @see org.springframework.jms.remoting.JmsInvokerServiceExporter
 * @see org.springframework.jms.remoting.JmsInvokerProxyFactoryBean
 */
public class JmsInvokerClientInterceptor implements MethodInterceptor, InitializingBean {

	private QueueConnectionFactory connectionFactory;

	private Object queue;

	private DestinationResolver destinationResolver = new DynamicDestinationResolver();

	private RemoteInvocationFactory remoteInvocationFactory = new DefaultRemoteInvocationFactory();


	/**
	 * Set the QueueConnectionFactory to use for obtaining JMS QueueConnections.
	 */
	public void setConnectionFactory(QueueConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Return the QueueConnectionFactory to use for obtaining JMS QueueConnections.
	 */
	protected QueueConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * Set the target Queue to send invoker requests to.
	 */
	public void setQueue(Object queue) {
		this.queue = queue;
	}

	/**
	 * Set the name of target queue to send invoker requests to.
	 */
	public void setQueueName(String queueName) {
		this.queue = queueName;
	}

	/**
	 * Set the DestinationResolver that is to be used to resolve Queue
	 * references for this accessor.
	 * <p>The default resolver is a DynamicDestinationResolver. Specify a
	 * JndiDestinationResolver for resolving destination names as JNDI locations.
	 * @param destinationResolver the DestinationResolver that is to be used
	 * @see org.springframework.jms.support.destination.DynamicDestinationResolver
	 * @see org.springframework.jms.support.destination.JndiDestinationResolver
	 */
	public void setDestinationResolver(DestinationResolver destinationResolver) {
		Assert.notNull(destinationResolver, "DestinationResolver must not be null");
		this.destinationResolver = destinationResolver;
	}

	/**
	 * Set the RemoteInvocationFactory to use for this accessor.
	 * Default is a DefaultRemoteInvocationFactory.
	 * <p>A custom invocation factory can add further context information
	 * to the invocation, for example user credentials.
	 */
	public void setRemoteInvocationFactory(RemoteInvocationFactory remoteInvocationFactory) {
		this.remoteInvocationFactory = remoteInvocationFactory;
	}


	public void afterPropertiesSet() {
		if (getConnectionFactory() == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}
		if (this.queue == null) {
			throw new IllegalArgumentException("'queue' or 'queueName' is required");
		}
	}


	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
			return "JMS invoker proxy for queue [" + this.queue + "]";
		}

		RemoteInvocation invocation = createRemoteInvocation(methodInvocation);
		RemoteInvocationResult result = null;
		try {
			result = executeRequest(invocation);
		}
		catch (JMSException ex) {
			throw new RemoteAccessException("Cannot access JMS invoker queue [" + this.queue + "]", ex);
		}
		return recreateRemoteInvocationResult(result);
	}

	/**
	 * Create a new RemoteInvocation object for the given AOP method invocation.
	 * The default implementation delegates to the RemoteInvocationFactory.
	 * <p>Can be overridden in subclasses to provide custom RemoteInvocation
	 * subclasses, containing additional invocation parameters like user credentials.
	 * Note that it is preferable to use a custom RemoteInvocationFactory which
	 * is a reusable strategy.
	 * @param methodInvocation the current AOP method invocation
	 * @return the RemoteInvocation object
	 * @see RemoteInvocationFactory#createRemoteInvocation
	 */
	protected RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
		return this.remoteInvocationFactory.createRemoteInvocation(methodInvocation);
	}

	/**
	 * Execute the given remote invocation, sending an invoker request message
	 * to this accessor's target queue and waiting for a corresponding response.
	 * <p>The default implementation is based on a JMS QueueRequestor,
	 * using a freshly obtained JMS Session.
	 * @param invocation the RemoteInvocation to execute
	 * @return the RemoteInvocationResult object
	 * @throws JMSException in case of JMS failure
	 */
	protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws JMSException {
		QueueConnection con = getConnectionFactory().createQueueConnection();
		QueueSession session = null;
		QueueRequestor requestor = null;
		try {
			session = con.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queueToUse = resolveQueue(session);
			Message requestMessage = createRequestMessage(session, invocation);
			requestor = new QueueRequestor(session, queueToUse);
			Message responseMessage = requestor.request(requestMessage);
			return extractInvocationResult(responseMessage);
		}
		finally {
			JmsUtils.closeQueueRequestor(requestor);
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(con);
		}
	}

	/**
	 * Resolve this accessor's target queue.
	 * @param session the current JMS Session
	 * @return the resolved target Queue
	 * @throws JMSException if resolution failed
	 */
	protected Queue resolveQueue(Session session) throws JMSException {
		if (this.queue instanceof Queue) {
			return (Queue) queue;
		}
		else if (this.queue instanceof String) {
			return resolveQueueName(session, (String) this.queue);
		}
		else {
			throw new javax.jms.IllegalStateException(
					"Queue object [" + this.queue + "] is neither a [javax.jms.Queue] nor a queue name String");
		}
	}

	/**
	 * Resolve the given queue name into a JMS {@link javax.jms.Queue},
	 * via this accessor's {@link DestinationResolver}.
	 * @param session the current JMS Session
	 * @param queueName the name of the queue
	 * @return the located Queue
	 * @throws JMSException if resolution failed
	 * @see #setDestinationResolver
	 */
	protected Queue resolveQueueName(Session session, String queueName) throws JMSException {
		return (Queue) this.destinationResolver.resolveDestinationName(session, queueName, false);
	}

	/**
	 * Create the invoker request message.
	 * <p>The default implementation creates a JMS ObjectMessage
	 * for the given RemoteInvocation object.
	 * @param session the current JMS Session
	 * @param invocation the remote invocation to send
	 * @throws JMSException if the message could not be created
	 */
	protected Message createRequestMessage(Session session, RemoteInvocation invocation) throws JMSException {
		return session.createObjectMessage(invocation);
	}

	/**
	 * Extract the invocation result from the response message.
	 * <p>The default implementation expects a JMS ObjectMessage carrying
	 * a RemoteInvocationResult object. If an invalid response message is
	 * encountered, the <code>onInvalidResponse</code> callback gets invoked.
	 * @param responseMessage the response message
	 * @return the invocation result
	 * @throws JMSException is thrown if a JMS exception occurs
	 * @see #onInvalidResponse
	 */
	protected RemoteInvocationResult extractInvocationResult(Message responseMessage) throws JMSException {
		if (responseMessage instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage) responseMessage;
			Object body = objectMessage.getObject();
			if (body instanceof RemoteInvocationResult) {
				return (RemoteInvocationResult) body;
			}
		}
		return onInvalidResponse(responseMessage);
	}

	/**
	 * Callback that is invoked by <code>extractInvocationResult</code>
	 * when it encounters an invalid response message.
	 * <p>The default implementation throws a MessageFormatException.
	 * @param responseMessage the invalid response message
	 * @return an alternative invocation result that should be
	 * returned to the caller (if desired)
	 * @throws JMSException if the invalid response should lead
	 * to an infrastructure exception propagated to the caller
	 * @see #extractInvocationResult
	 */
	protected RemoteInvocationResult onInvalidResponse(Message responseMessage) throws JMSException {
		throw new MessageFormatException("Invalid response message: " + responseMessage);
	}

	/**
	 * Recreate the invocation result contained in the given RemoteInvocationResult
	 * object. The default implementation calls the default recreate method.
	 * <p>Can be overridden in subclass to provide custom recreation, potentially
	 * processing the returned result object.
	 * @param result the RemoteInvocationResult to recreate
	 * @return a return value if the invocation result is a successful return
	 * @throws Throwable if the invocation result is an exception
	 * @see org.springframework.remoting.support.RemoteInvocationResult#recreate()
	 */
	protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
		return result.recreate();
	}

}
