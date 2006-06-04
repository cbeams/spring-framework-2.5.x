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

package org.springframework.jms.listener.adapter;

import java.lang.reflect.InvocationTargetException;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;

/**
 * Message listener adapter that delegates the handling of messages to target
 * listener methods via reflection, with flexible message type conversion.
 * Allows listener methods to operate on message content types, completely
 * independent from the JMS API.
 *
 * <p>By default, the content of incoming JMS messages gets extracted before
 * being passed into the target listener method, to let the target method
 * operate on message content types such as String or byte array instead of
 * the raw <code>javax.jms.Message</code>.
 *
 * <p>Message type conversion is delegated to a Spring JMS MessageConverter.
 * By default, a SimpleMessageConverter(102) will be used.
 *
 * <p>If a target listener method returns a non-null object (typically of a
 * message content type such as String or byte array), it will get wrapped
 * in a JMS Message and sent to the response destination (either the JMS
 * "reply-to" destination or a
 * {@link #setDefaultResponseDestination(javax.jms.Destination) specified default destination}).
 *
 * <p><b>Note:</b> The sending of response messages is only available when
 * using the SessionAwareMessageListener entry point (typically through a
 * Spring message listener container). Usage as standard JMS MessageListener
 * does <i>not</i> support the generation of response messages.
 *
 * <p>This class requires a JMS 1.1+ provider, because it builds on the
 * domain-independent API. <b>Use the {@link MessageListenerAdapter102
 * MessageListenerAdapter102} subclass for JMS 1.0.2 providers.</b>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setDelegate
 * @see #setDefaultListenerMethod
 * @see #setDefaultResponseDestination
 * @see #setMessageConverter
 * @see org.springframework.jms.support.converter.SimpleMessageConverter
 * @see org.springframework.jms.listener.SessionAwareMessageListener
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer#setMessageListener
 */
public class MessageListenerAdapter implements MessageListener, SessionAwareMessageListener {

	/**
	 * Out-of-the-box value for the default listener method: "handleMessage".
	 */
	public static final String ORIGINAL_DEFAULT_LISTENER_METHOD = "handleMessage";


    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

	private Object delegate;

	private String defaultListenerMethod = ORIGINAL_DEFAULT_LISTENER_METHOD;

	private Destination defaultResponseDestination;

	private MessageConverter messageConverter;


	/**
	 * Create a new MessageListenerAdapter with default settings.
	 */
	public MessageListenerAdapter() {
		initDefaultStrategies();
		this.delegate = this;
	}

	/**
	 * Create a new MessageListenerAdapter for the given delegate.
	 */
	public MessageListenerAdapter(Object delegate) {
		initDefaultStrategies();
		setDelegate(delegate);
	}


	/**
	 * Set a target object to delegate message listening to.
	 * Specified listener methods have to be present on this target object.
	 * <p>If no explicit delegate object has been specified, listener
	 * methods are expected to present on this adapter instance, that is,
	 * on a custom subclass of this adapter, defining listener methods.
	 */
	public void setDelegate(Object delegate) {
		Assert.notNull("Delegate must not be null");
		this.delegate = delegate;
	}

	/**
	 * Return the target object to delegate message listening to.
	 */
	protected Object getDelegate() {
		return delegate;
	}

	/**
	 * Specify the name of the default listener method to delegate to,
	 * for the case where no specific listener method has been determined.
	 * Out-of-the-box value is "handleMessage".
	 * @see #getListenerMethodName
	 */
	public void setDefaultListenerMethod(String defaultListenerMethod) {
		this.defaultListenerMethod = defaultListenerMethod;
	}

	/**
	 * Return the name of the default listener method to delegate to.
	 */
	protected String getDefaultListenerMethod() {
		return defaultListenerMethod;
	}

	/**
	 * Set the default destination to send response messages to.
	 * This will be applied in case of a request message that does not
	 * carry a "JMSReplyTo" field.
	 * <p>Response destinations are only relevant for listener methods
	 * that return result objects, which will be wrapped in a response
	 * message and sent to a response destination.
	 * @see #getResponseDestination
	 */
	public void setDefaultResponseDestination(Destination defaultResponseDestination) {
		this.defaultResponseDestination = defaultResponseDestination;
	}

	/**
	 * Return the default destination to send response messages to.
	 */
	protected Destination getDefaultResponseDestination() {
		return defaultResponseDestination;
	}

	/**
	 * Set the converter that will convert incoming JMS messages to
	 * listener method arguments, and objects returned from listener
	 * methods back to JMS messages.
	 * <p>The default converter is a SimpleMessageConverter, which is able
	 * to handle BytesMessages, TextMessages and ObjectMessages.
	 */
	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	/**
	 * Return the converter that will convert incoming JMS messages to
	 * listener method arguments, and objects returned from listener
	 * methods back to JMS messages.
	 */
	protected MessageConverter getMessageConverter() {
		return messageConverter;
	}


	/**
	 * Standard JMS MessageListener entry point.
	 * <p>Delegates the message to the target listener method, with appropriate
	 * conversion of the message argument. In case of an exception, the
	 * <code>handleListenerException</code> method will be invoked.
	 * <p><b>Note:</b> Does not support sending response messages based on
	 * result objects returned from listener methods. Use the
	 * SessionAwareMessageListener entry point (typically through a Spring
	 * message listener container) for handling result objects as well.
	 * @param message the incoming JMS message
	 * @see #handleListenerException
	 * @see #onMessage(javax.jms.Message, javax.jms.Session)
	 */
	public void onMessage(Message message) {
		try {
			onMessage(message, null);
		}
		catch (Throwable ex) {
			handleListenerException(ex);
		}
	}

	/**
	 * Spring SessionAwareMessageListener entry point.
	 * <p>Delegates the message to the target listener method, with appropriate
	 * conversion of the message argument. If the target method returns a
	 * non-null object, wrap in a JMS message and send it back.
	 * @param message the incoming JMS message
	 * @param session the JMS session to operate on
	 * @throws JMSException if thrown by JMS API methods
	 */
	public void onMessage(Message message, Session session) throws JMSException {
		Object convertedMessage = extractMessage(message);
		String methodName = getListenerMethodName(message, convertedMessage);
		if (methodName == null) {
			throw new IllegalStateException(
					"No listener method for message [" + convertedMessage +
					"] - specify a 'defaultListenerMethod' or override 'getListenerMethodName'");
		}
		Object result = invokeListenerMethod(methodName, convertedMessage);
		if (result != null) {
			handleResult(result, message, session);
		}
		else {
			logger.debug("No result object given - no result to handle");
		}
	}


    /**
	 * Initialize the default implementations for the adapter's strategies:
	 * SimpleMessageConverter.
	 * @see #setMessageConverter
	 * @see org.springframework.jms.support.converter.SimpleMessageConverter
	 */
	protected void initDefaultStrategies() {
		setMessageConverter(new SimpleMessageConverter());
	}

    /**
	 * Handle the given exception that arose during listener execution.
	 * The default implementation logs the exception at error level.
	 * <p>This method only applies when used as standard JMS MessageListener.
	 * In case of the Spring SessionAwareMessageListener mechanism,
	 * exceptions get handled by the caller instead.
	 * @param ex the exception to handle
	 * @see #onMessage(javax.jms.Message)
	 */
	protected void handleListenerException(Throwable ex) {
		logger.error("Listener execution failed", ex);
	}

    /**
	 * Extract the message body from the given JMS message.
	 * @param message the JMS Message
	 * @return the content of the message, to be passed into the
	 * listener method as argument
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Object extractMessage(Message message) throws JMSException {
		MessageConverter converter = getMessageConverter();
		if (converter != null) {
			return converter.fromMessage(message);
		}
		return message;
	}

	/**
	 * Determine the name of the listener method that is supposed to
	 * handle the given message.
	 * @param originalMessage the JMS request message
	 * @param extractedMessage the converted JMS request message,
	 * to be passed into the listener method as argument
	 * @return the name of the listener method (never <code>null</code>)
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected String getListenerMethodName(Message originalMessage, Object extractedMessage) throws JMSException {
		String listenerMethod = getDefaultListenerMethod();
		if (listenerMethod == null) {
			throw new javax.jms.IllegalStateException("No default listener method specified - " +
					"either specify a non-null value for the 'defaultListenerMethod' property or " +
					"override the 'getListenerMethodName' method");
		}
		return listenerMethod;
	}

	/**
	 * Invoke the specified listener method.
	 * @param methodName the name of the listener method
	 * @param message the message to pass in as argument
	 * @return the result returned from the listener method
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Object invokeListenerMethod(String methodName, Object message) throws JMSException {
		try {
			MethodInvoker methodInvoker = new MethodInvoker();
			methodInvoker.setTargetObject(getDelegate());
			methodInvoker.setTargetMethod(methodName);
			methodInvoker.setArguments(new Object[] {message});
			methodInvoker.prepare();
			return methodInvoker.invoke();
		}
		catch (InvocationTargetException ex) {
			throw new ListenerExecutionFailedException(
					"Listener method '" + methodName + "' threw exception", ex.getTargetException());
		}
		catch (Throwable ex) {
			throw new ListenerExecutionFailedException(
					"Failed to invoke target method '" + methodName + "' with message [" + message + "]", ex);
		}
	}


	/**
	 * Handle the given result object returned from the listener method,
	 * sending a response message back.
	 * @param result the result object to handle (never <code>null</code>)
	 * @param request the original request message
	 * @param session the JMS Session to operate on (may be <code>null</code>)
	 * @throws JMSException if thrown by JMS API methods
	 * @see #buildMessage
	 * @see #postProcessResponse
	 * @see #getResponseDestination
	 * @see #sendResponse
	 */
	protected void handleResult(Object result, Message request, Session session) throws JMSException {
		if (session != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Listener method returned result [" + result +
						"] - generating response message for it");
			}
			Message response = buildMessage(session, result);
			postProcessResponse(request, response);
			Destination destination = getResponseDestination(request, response);
			sendResponse(session, destination,  response);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Listener method returned result [" + result +
						"] - not generating response message for it because of no JMS Session given");
			}
		}
	}

	/**
	 * Build a JMS message to be sent as response based on the given result object.
	 * @param session the JMS session to operate on
	 * @param result the content of the message, as returned from the listener method
	 * @return the JMS Message (never <code>null</code>)
	 * @throws JMSException if thrown by JMS API methods
	 * @see #setMessageConverter
	 */
	protected Message buildMessage(Session session, Object result) throws JMSException {
		MessageConverter converter = getMessageConverter();
		if (converter != null) {
			return converter.toMessage(result, session);
		}
		else {
			if (!(result instanceof Message)) {
				throw new MessageConversionException(
						"No MessageConverter specified - cannot handle message [" + result + "]");
			}
			return (Message) result;
		}
	}

	/**
	 * Post-process the given response message before it will be sent.
	 * <p>The default implementation sets the response's correlation id
	 * to the request message's correlation id.
	 * @param request the original incoming JMS message
	 * @param response the outgoing JMS message about to be sent
	 * @throws JMSException if thrown by JMS API methods
	 * @see javax.jms.Message#setJMSCorrelationID
	 */
	protected void postProcessResponse(Message request, Message response) throws JMSException {
		response.setJMSCorrelationID(request.getJMSCorrelationID());
	}

	/**
	 * Determine a response destination for the given message.
     * <p>The default implementation first checks the JMS Reply-To
     * {@link Destination} of the supplied request; if that is not <code>null</code>
     * it is returned; if it is <code>null</code>, then the configured
     * {@link #getDefaultResponseDestination() default response destination}
     * is returned; if this too is <code>null</code>, then an
     * {@link InvalidDestinationException} is thrown. 
	 * @param request the original incoming JMS message
	 * @param response the outgoing JMS message about to be sent
	 * @return the response destination (never <code>null</code>)
	 * @throws JMSException if thrown by JMS API methods
     * @throws InvalidDestinationException if no {@link Destination} can be determined
	 * @see #setDefaultResponseDestination
	 * @see javax.jms.Message#getJMSReplyTo()
	 */
	protected Destination getResponseDestination(Message request, Message response) throws JMSException {
		Destination replyTo = request.getJMSReplyTo();
		if (replyTo == null) {
			replyTo = getDefaultResponseDestination();
			if (replyTo == null) {
				throw new InvalidDestinationException("Cannot determine response destination - " +
						"request message does not contain reply-to destination, and no default response destination set");
			}
		}
		return replyTo;
	}

	/**
	 * Send the given response message to the given destination.
	 * @param response the JMS message to send
	 * @param destination the JMS destination to send to
	 * @param session the JMS session to operate on
	 * @throws JMSException if thrown by JMS API methods
	 * @see #postProcessProducer
	 * @see javax.jms.Session#createProducer
	 * @see javax.jms.MessageProducer#send
	 */
	protected void sendResponse(Session session, Destination destination, Message response) throws JMSException {
		MessageProducer producer = session.createProducer(destination);
		try {
			postProcessProducer(producer, response);
			producer.send(response);
		}
		finally {
			JmsUtils.closeMessageProducer(producer);
		}
	}

	/**
	 * Post-process the given message producer before using it to send the response.
	 * <p>The default implementation is empty.
	 * @param producer the JMS message producer that will be used to send the message
	 * @param response the outgoing JMS message about to be sent
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected void postProcessProducer(MessageProducer producer, Message response) throws JMSException {
	}

}
