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

package org.springframework.jms.remoting;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * A JMS MessageListener that exports the specified service bean as a JMS service
 * endpoint, accessible via a JMS proxy.
 *
 * <p>Note: JMS services exported with this class can be accessed by
 * any JMS client, as there isn't any special handling involved.
 *
 * @author James Strachan
 * @see org.springframework.jms.remoting.JmsInvokerProxyFactoryBean
 */
public class JmsInvokerServiceExporter extends RemoteInvocationBasedExporter
		implements MessageListener, InitializingBean {

	private Object proxy;

	private JmsTemplate jmsTemplate;

	private boolean ignoreFailures;

	private boolean ignoreInvalidMessages;


	/**
	 * Sets the JMS template used to send replies back for the request
	 * @param jmsTemplate the JMS template to use
	 */
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	/**
	 * Set whether or not failures should be ignored (and just logged) or thrown as
	 * runtime exceptions into the JMS provider
	 */
	public void setIgnoreFailures(boolean ignoreFailures) {
		this.ignoreFailures = ignoreFailures;
	}

	public boolean isIgnoreFailures() {
		return ignoreFailures;
	}

	/**
	 * Set whether invalidly formatted messages should be silently ignored or not
	 */
	public void setIgnoreInvalidMessages(boolean ignoreInvalidMessages) {
		this.ignoreInvalidMessages = ignoreInvalidMessages;
	}

	public boolean isIgnoreInvalidMessages() {
		return ignoreInvalidMessages;
	}

	public void afterPropertiesSet() {
		if (this.jmsTemplate == null) {
			throw new IllegalArgumentException("template is required");
		}
		this.proxy = getProxyForService();
	}


	public void onMessage(Message message) {
		try {
			RemoteInvocation invocation = readRemoteInvocation(message);
			if (invocation != null) {
				RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
				writeRemoteInvocationResult(message, result);
			}
		}
		catch (JMSException ex) {
			onException(message, ex);
		}
	}

	/**
	 * Read a RemoteInvocation from the given JMS message.
	 * @param message current JMS message
	 * @return the RemoteInvocation object
	 */
	protected RemoteInvocation readRemoteInvocation(Message message) throws JMSException {
		if (message instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage) message;
			Object body = objectMessage.getObject();
			if (body instanceof RemoteInvocation) {
				return (RemoteInvocation) body;
			}
		}
		return onInvalidMessage(message);
	}


	/**
	 * Send the given RemoteInvocationResult as a JMS message to the originator.
	 * @param message current HTTP message
	 * @param result  the RemoteInvocationResult object
	 * @throws javax.jms.JMSException if thrown by trying to send the message
	 */
	protected void writeRemoteInvocationResult(final Message message, final RemoteInvocationResult result)
			throws JMSException {
		jmsTemplate.send(message.getJMSReplyTo(), new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return createResponseMessage(session, message, result);
			}
		});
	}

	/**
	 * Create the invocation result response message.
	 * @param session the JMS session to use
	 * @param message the original request message, in case we want to attach any properties etc.
	 * @param result  the invocation result
	 * @return the message response to send
	 * @throws javax.jms.JMSException if creating the messsage failed
	 */
	protected Message createResponseMessage(Session session, Message message, RemoteInvocationResult result)
			throws JMSException {
		// an alternative strategy could be to use XStream and text messages
		// though some JMS providers, like ActiveMQ, might do this kind of thing for us under the covers
		ObjectMessage answer = session.createObjectMessage(result);

		// lets preserve the correlation ID
		answer.setJMSCorrelationID(message.getJMSCorrelationID());
		return answer;
	}

	/**
	 * Handle invalid messages by just logging, though a different implementation
	 * may wish to throw exceptions.
	 */
	protected RemoteInvocation onInvalidMessage(Message message) throws JmsInvokerProcessingException {
		String text = "Invalid message will be discarded: " + message;
		logger.warn(text);
		if (!this.ignoreInvalidMessages) {
			throw new JmsInvokerProcessingException(text);
		}
		return null;
	}

	/**
	 * Handle the processing of an exception when processing an inbound message.
	 */
	protected void onException(Message message, JMSException ex) throws JmsInvokerProcessingException {
		String text = "Failed to process inbound message due to: " + ex.getMessage() +
				". Message will be discarded: " + message;
		logger.warn(text, ex);
		if (!this.ignoreFailures) {
			throw new JmsInvokerProcessingException(text, ex);
		}
	}
}
