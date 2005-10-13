/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jms.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Variant of the standard JMS MessageListener interface,
 * offering not only the received Message but also the underlying
 * JMS Session object. The latter can be used to send reply messages,
 * without the need to access an external Connection/Session,
 * i.e. without the need to access the underlying ConnectionFactory.
 *
 * <p>Supported by Spring's SimpleMessageListenerContainer and
 * ServerMessageListenerContainer, as direct alternative to the standard
 * JMS MessageListener interface. Typically <i>not</i> supported by
 * JCA-based listener containers: For maximum compatibility, implement
 * a standard JMS MessageListener instead.
 *
 * @author Juergen Hoeller
 * @since 1.3
 * @see AbstractMessageListenerContainer#setMessageListener
 * @see SimpleMessageListenerContainer
 * @see org.springframework.jms.listener.server.ServerMessageListenerContainer
 * @see javax.jms.MessageListener
 */
public interface SessionAwareMessageListener {

	/**
	 * Callback for a received JMS message.
	 * <p>Implementors are supposed to process the given Message,
	 * typically sending reply messages through the given Session.
	 * @param message the received JMS message
	 * @param session the underlying JMS Session
	 * @throws JMSException if thrown by JMS methods
	 */
	void onMessage(Message message, Session session) throws JMSException;

}
