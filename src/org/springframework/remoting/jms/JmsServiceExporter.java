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

package org.springframework.remoting.jms;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.JmsException;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.core.NestedRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.MessageNotReadableException;

/**
 * A JMS MessageListener that exports the specified service bean as a JMS service
 * endpoint, accessible via a JMS proxy.
 * <p/>
 * <p>Note: JMS services exported with this class can be accessed by
 * any JMS client, as there isn't any special handling involved.
 *
 * @author James Strachan
 * @see org.springframework.remoting.jms.JmsProxyFactoryBean
 */
public class JmsServiceExporter extends RemoteInvocationBasedExporter implements MessageListener, InitializingBean {
    private static final Log log = LogFactory.getLog(JmsServiceExporter.class);

    private Object proxy;
    private JmsTemplate template;
    private boolean ignoreFailures;
    private boolean ignoreInvalidMessages;

    public void afterPropertiesSet() {
        this.proxy = getProxyForService();
        if (template == null) {
            throw new IllegalArgumentException("template is required");
        }
    }

    public void onMessage(Message message) {
        try {
            RemoteInvocation invocation = readRemoteInvocation(message);
            if (invocation != null) {
                RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
                writeRemoteInvocationResult(message, result);
            }
        }
        catch (JMSException e) {
            onException(message, e);
        }
    }

    public JmsTemplate getTemplate() {
        return template;
    }

    /**
     * Sets the JMS template used to send replies back for the request
     * @param template the JMS template to use
     */
    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    public boolean isIgnoreFailures() {
        return ignoreFailures;
    }

    /**
     * Sets whether or not failures should be ignored (and just logged) or thrown as
     * runtime exceptions into the JMS provider
     */
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    public boolean isIgnoreInvalidMessages() {
        return ignoreInvalidMessages;
    }

    /**
     * Sets whether invalidly formatted messages should be silently ignored or not
     */
    public void setIgnoreInvalidMessages(boolean ignoreInvalidMessages) {
        this.ignoreInvalidMessages = ignoreInvalidMessages;
    }

    /**
     * Read a RemoteInvocation from the given JMS message
     *
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
     * Send the given RemoteInvocationResult as a JMS message to the originator
     *
     * @param message current HTTP message
     * @param result  the RemoteInvocationResult object
     * @throws JMSException if thrown by trying to send the message
     */
    protected void writeRemoteInvocationResult(final Message message, final RemoteInvocationResult result) throws JMSException {
        template.send(message.getJMSReplyTo(), new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return createResponseMessage(session, message, result);
            }
        });
    }

    /**
     * Creates the invocation result response message
     *
     * @param session the JMS session to use
     * @param message the original request message, in case we want to attach any properties etc.
     * @param result  the invocation result
     * @return the message response to send
     * @throws JMSException if creating the messsage failed
     */
    protected Message createResponseMessage(Session session, Message message, RemoteInvocationResult result) throws JMSException {
        // an alternative strategy could be to use XStream and text messages
        // though some JMS providers, like ActiveMQ, might do this kind of thing for us under the covers 
        ObjectMessage answer = session.createObjectMessage(result);

        // lets preserve the correlation ID
        answer.setJMSCorrelationID(message.getJMSCorrelationID());
        return answer;
    }

    /**
     * Handle invalid messages by just logging, though a different implementation
     * may wish to throw exceptions
     */
    protected RemoteInvocation onInvalidMessage(Message message) {
        String text = "Invalid message will be discarded: " + message;
        log.info(text);
        if (!ignoreInvalidMessages) {
            throw new RuntimeException(text);
        }
        return null;
    }

    /**
     * Handle the processing of an exception when processing an inbound messsage
     */
    protected void onException(Message message, JMSException e) {
        String text = "Failed to process inbound message due to: " + e + ". Message will be discarded: " + message;
        log.info(text, e);
        if (!ignoreFailures) {
            throw new RuntimeException(text, e);
        }
    }
}
