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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Create a JMS Consumer from a Session.  It is creates non-singleton/prototype bean.
 * Requires a JMS 1.1 provider.
 *
 * @author Mark Pollack
 */
public class JmsConsumerFactoryBean implements FactoryBean, InitializingBean {
    protected final Log logger = LogFactory.getLog(getClass());
    private Destination destination;
    private MessageConsumer messageConsumer;
    private MessageListener messageListener;
    private Session session;
    private String messageSelector;
    private Boolean noLocal;
    private boolean isDurableSubscriber = false;
    private String durableIdentity;

    /**
     * Set the JMS destination to consume messages from.
     * @param destination JMS destination.
     */
    public void setDestination(Destination d) {
        destination = d;
    }

    public Object getObject() throws Exception {
        return messageConsumer;
    }

    public Class getObjectType() {
        return MessageConsumer.class;
    }

    public boolean isSingleton() {
        return false;
    }

    public void afterPropertiesSet() throws Exception {
        if (session == null) {
            throw new IllegalArgumentException("Did not set required JMS session property");
        }

        if (destination == null) {
            throw new IllegalArgumentException("Did not set required JMS destination property");
        }

        logger.info("Creating JMS Consumer");

        if (isDurableSubscriber) {
            if ( ! (destination instanceof Topic))
            {
                throw new IllegalArgumentException("Destination must be a topic when creating a durable subscriber");
            }
            if (durableIdentity == null)
            {
                throw new IllegalArgumentException("DurableIdentity is requred when creating a durable subscriber");
            }
            if (noLocal == null) {
                messageConsumer = 
                    session.createDurableSubscriber((Topic)destination, durableIdentity);
            } else {
                messageConsumer = 
                    session.createDurableSubscriber((Topic)destination, durableIdentity, messageSelector, noLocal.booleanValue());
            }
        } else {

            if (noLocal == null) {
                messageConsumer =
                    session.createConsumer(destination, messageSelector);
            } else {
                messageConsumer =
                    session.createConsumer(
                        destination,
                        messageSelector,
                        noLocal.booleanValue());
            }
        }

        if (messageListener != null) {
            messageConsumer.setMessageListener(messageListener);
        }

    }
    /**
     * The message listener to use for asynchronous receiving of messages
     * @param listener The messgae listener.
     */
    public void setMessageListener(MessageListener listener) {
        messageListener = listener;
    }

    /**
     * The message selector to apply to the consumer.
     * @param s message selector
     */
    public void setMessageSelector(String s) {
        messageSelector = s;
    }

    /**
     * Set the nolocal property, used when creating a consumer.
     * @param b nolocal property.
     */
    public void setNoLocal(boolean b) {
        noLocal = new Boolean(b);
    }

    /**
     * Set the JMS session used to create the Consumer
     * @param session JMS Session object
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * @param string
     */
    public void setDurableIdentity(String string) {
        durableIdentity = string;
    }

}
