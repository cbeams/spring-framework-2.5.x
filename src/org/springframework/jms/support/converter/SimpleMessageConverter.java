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

package org.springframework.jms.support.converter;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * A simple message converter that can handle BytesMessages, TextMessages,
 * and ObjectMessages.
 *
 * <p>Converts a byte array to a JMS BytesMessage, a String to a JMS TextMessage,
 * and a Serializable object to a JMS ObjectMessage (and vice versa).
 *
 * @author Juergen Hoeller
 * @since 25.07.2004
 */
public class SimpleMessageConverter implements MessageConverter {

	/**
	 * This implementation creates a ByteMessage for a byte array, a TextMessage
	 * for a String, and an ObjectMessage for a Serializable object.
	 * @see javax.jms.Session#createBytesMessage
	 * @see javax.jms.Session#createTextMessage
	 * @see javax.jms.Session#createObjectMessage
	 */
	public Message toMessage(Object object, Session session) throws JMSException {
		if (object instanceof byte[]) {
			BytesMessage message = session.createBytesMessage();
			message.writeBytes((byte[]) object);
			return message;
		}
		else if (object instanceof String) {
			return session.createTextMessage((String) object);
		}
		else if (object instanceof Serializable) {
			return session.createObjectMessage((Serializable) object);
		}
		else {
			throw new MessageConversionException("Cannot convert object [" + object + "] to JMS message");
		}
	}

	/**
	 * This implementation converts a ByteMessage back to a byte array,
	 * a TextMessage back to a String, and an ObjectMessage back to a
	 * Serializable object.
	 */
	public Object fromMessage(Message message) throws JMSException {
		if (message instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) message;
			byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
			bytesMessage.readBytes(bytes);
			return bytes;
		}
		else if (message instanceof TextMessage) {
			return ((TextMessage) message).getText();
		}
		else if (message instanceof ObjectMessage) {
			return ((ObjectMessage) message).getObject();
		}
		else {
			throw new MessageConversionException("Cannot convert JMS message [" + message + "] to object");
		}
	}

}
