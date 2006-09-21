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
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.JmsUtils;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * JMS MessageListener that exports the specified service bean as a
 * JMS service endpoint, accessible via a JMS invoker proxy.
 *
 * @author Juergen Hoeller
 * @author James Strachan
 * @since 2.0
 * @see JmsInvokerClientInterceptor
 * @see JmsInvokerProxyFactoryBean
 */
public class JmsInvokerServiceExporter extends RemoteInvocationBasedExporter
		implements SessionAwareMessageListener, InitializingBean {

	private boolean ignoreInvalidRequests = true;

	private Object proxy;


	/**
	 * Set whether invalidly formatted messages should be discarded.
	 * Default is "true".
	 * <p>Switch this flag to "false" to throw an exception back to the
	 * listener container. This will typically lead to redelivery of
	 * the message, which is usually undesirable - since the message
	 * content will be the same (that is, still invalid).
	 */
	public void setIgnoreInvalidRequests(boolean ignoreInvalidRequests) {
		this.ignoreInvalidRequests = ignoreInvalidRequests;
	}

	public void afterPropertiesSet() {
		this.proxy = getProxyForService();
	}


	public void onMessage(Message requestMessage, Session session) throws JMSException {
		RemoteInvocation invocation = readRemoteInvocation(requestMessage);
		if (invocation != null) {
			RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
			writeRemoteInvocationResult(requestMessage, session, result);
		}
	}

	/**
	 * Read a RemoteInvocation from the given JMS message.
	 * @param requestMessage current request message
	 * @return the RemoteInvocation object
	 */
	protected RemoteInvocation readRemoteInvocation(Message requestMessage) throws JMSException {
		if (requestMessage instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage) requestMessage;
			Object body = objectMessage.getObject();
			if (body instanceof RemoteInvocation) {
				return (RemoteInvocation) body;
			}
		}
		return onInvalidRequest(requestMessage);
	}


	/**
	 * Send the given RemoteInvocationResult as a JMS message to the originator.
	 * @param requestMessage current request message
	 * @param session the JMS Session to use
	 * @param result the RemoteInvocationResult object
	 * @throws javax.jms.JMSException if thrown by trying to send the message
	 */
	protected void writeRemoteInvocationResult(
			Message requestMessage, Session session, RemoteInvocationResult result) throws JMSException {

		Message response = createResponseMessage(requestMessage, session, result);
		MessageProducer producer = session.createProducer(requestMessage.getJMSReplyTo());
		try {
			producer.send(response);
		}
		finally {
			JmsUtils.closeMessageProducer(producer);
		}
	}

	/**
	 * Create the invocation result response message.
	 * <p>The default implementation creates a JMS ObjectMessage
	 * for the given RemoteInvocationResult object.
	 * @param requestMessage the original request message
	 * @param session the JMS session to use
	 * @param result the invocation result
	 * @return the message response to send
	 * @throws javax.jms.JMSException if creating the messsage failed
	 */
	protected Message createResponseMessage(Message requestMessage, Session session, RemoteInvocationResult result)
			throws JMSException {

		// An alternative strategy could be to use XStream and text messages.
		// Though some JMS providers, like ActiveMQ, might do this kind of thing for us under the covers.
		ObjectMessage response = session.createObjectMessage(result);

		// Let's preserve the correlation ID.
		response.setJMSCorrelationID(requestMessage.getJMSCorrelationID());

		return response;
	}

	/**
	 * Callback that is invoked by <code>readRemoteInvocation</code>
	 * when it encounters an invalid request message.
	 * <p>The default implementation either discards the invalid message or
	 * throws a MessageFormatException - according to the "ignoreInvalidRequests"
	 * flag, which is set to "true" (that is, discard invalid messages) by default.
	 * @param requestMessage the invalid request message
	 * @see #readRemoteInvocation
	 * @see #setIgnoreInvalidRequests
	 */
	protected RemoteInvocation onInvalidRequest(Message requestMessage) throws JMSException {
		if (this.ignoreInvalidRequests) {
			if (logger.isWarnEnabled()) {
				logger.warn("Invalid request message will be discarded: " + requestMessage);
			}
			return null;
		}
		else {
			throw new MessageFormatException("Invalid request message: " + requestMessage);
		}
	}

}
