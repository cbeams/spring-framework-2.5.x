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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;

/**
 * Interceptor for accessing a JMS based service.
 *
 * @author James Strachan
 * @see #setServiceInterface
 * @see #setServiceUrl
 * @see org.springframework.remoting.jms.JmsServiceExporter
 * @see org.springframework.remoting.jms.JmsProxyFactoryBean
 */
public class JmsClientInterceptor extends RemoteInvocationBasedAccessor
        implements MethodInterceptor, InitializingBean {

    private QueueRequestor queueRequestor;
    private QueueSession session;
    private Queue queue;

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
            return "JMS invoker proxy for service URL [" + getServiceUrl() + "]";
        }

        RemoteInvocation invocation = createRemoteInvocation(methodInvocation);
        RemoteInvocationResult result = null;
        try {
            result = executeRequest(invocation);
        }
        catch (Exception ex) {
            throw new RemoteAccessException("Cannot access JMS invoker remote service at [" + getServiceUrl() + "]", ex);
        }
        return recreateRemoteInvocationResult(result);
    }

    public void afterPropertiesSet() throws JMSException {
        if (session == null) {
            throw new IllegalArgumentException("session is required");
        }
        if (queue == null) {
            throw new IllegalArgumentException("queue is required");
        }
        queueRequestor = new QueueRequestor(session, queue);
    }

    public QueueSession getSession() {
        return session;
    }

    public void setSession(QueueSession session) {
        this.session = session;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    /**
     * Execute the given remote invocation
     *
     * @param invocation the RemoteInvocation to execute
     * @return the RemoteInvocationResult object
     * @throws JMSException if some kind of JMS I/O failure occurs
     * @see HttpInvokerClientConfiguration
     */
    protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws JMSException {
        Message response = queueRequestor.request(createRequestMessage(invocation));
        return extractInvocationResult(response);
    }

    /**
     * Creates the request message
     *
     * @param invocation the remote invocation to send
     * @throws JMSException if the message could not be created
     */
    protected Message createRequestMessage(RemoteInvocation invocation) throws JMSException {
        return session.createObjectMessage(invocation);
    }

    /**
     * Extracts the invocation result from the response message
     *
     * @param message the response message
     * @return the invocation result
     * @throws JMSException is thrown if a JMS exception occurs
     */
    protected RemoteInvocationResult extractInvocationResult(Message message) throws JMSException {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Object body = objectMessage.getObject();
            if (body instanceof RemoteInvocationResult) {
                return (RemoteInvocationResult) body;
            }
        }
        return onInvalidMessage(message);
    }

    protected RemoteInvocationResult onInvalidMessage(Message message) throws JMSException {
        throw new JMSException("Invalid response message: " + message);
    }

    /**
     * Recreate the invocation result contained in the given RemoteInvocationResult
     * object. The default implementation calls the default recreate method.
     * <p>Can be overridden in subclass to provide custom recreation, potentially
     * processing the returned result object.
     *
     * @param result the RemoteInvocationResult to recreate
     * @return a return value if the invocation result is a successful return
     * @throws Throwable if the invocation result is an exception
     * @see org.springframework.remoting.support.RemoteInvocationResult#recreate
     */
    protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
        return result.recreate();
    }

}
