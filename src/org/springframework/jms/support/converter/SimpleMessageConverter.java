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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * A simple message converter that can handle TextMessages, BytesMessages,
 * and MapMessages.
 *
 * <p>Converts a String to a JMS TextMessage, a byte array to a JMS BytesMessage,
 * and a Map to a JMS MapMessage (and vice versa).
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see javax.jms.TextMessage
 * @see javax.jms.BytesMessage
 * @see javax.jms.MapMessage
 */
public class SimpleMessageConverter implements MessageConverter {

	/**
	 * This implementation creates a TextMessage for a String, a
	 * BytesMessage for a byte array, and a MapMessage for a Map.
	 * @see javax.jms.Session#createBytesMessage
	 * @see javax.jms.Session#createTextMessage
	 * @see javax.jms.Session#createObjectMessage
	 */
	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
		if (object instanceof String) {
			return session.createTextMessage((String) object);
		}
		else if (object instanceof byte[]) {
			BytesMessage message = session.createBytesMessage();
			message.writeBytes((byte[]) object);
			return message;
		}
		else if (object instanceof Map) {
			MapMessage message = session.createMapMessage();
			Map map = (Map) object;
			for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				if (!(entry.getKey() instanceof String)) {
					throw new MessageConversionException(
							"Cannot convert non-String key of type [" +
							(entry.getKey() != null ? entry.getKey().getClass().getName() : null) +
							"] to MapMessage entry");
				}
				message.setObject((String) entry.getKey(), entry.getValue());
			}
			return message;
		}
		else {
			throw new MessageConversionException("Cannot convert object [" + object + "] to JMS message");
		}
	}

	/**
	 * This implementation converts a TextMessage back to a String, a
	 * ByteMessage back to a byte array, and a MapMessage back to a Map.
	 */
	public Object fromMessage(Message message) throws JMSException, MessageConversionException {
		if (message instanceof TextMessage) {
			return ((TextMessage) message).getText();
		}
		else if (message instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) message;
			byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
			bytesMessage.readBytes(bytes);
			return bytes;
		}
		else if (message instanceof MapMessage) {
			MapMessage mapMessage = (MapMessage) message;
			Map map = new HashMap();
			Enumeration enum = mapMessage.getMapNames();
			while (enum.hasMoreElements()) {
				String key = (String) enum.nextElement();
				map.put(key, mapMessage.getObject(key));
			}
			return map;
		}
		else {
			throw new MessageConversionException("Cannot convert JMS message [" + message + "] to object");
		}
	}

}
