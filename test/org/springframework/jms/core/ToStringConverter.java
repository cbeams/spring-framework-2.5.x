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

package org.springframework.jms.core;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConverter;

/**
 * A simple message converter that uses the toString method.
 * @author Mark Pollack
 * @see java.lang.Object#toString
 */
public class ToStringConverter implements MessageConverter {

	/**
	 * This implementations creates a JMS text message with
	 * the toString result of the given object.
	 * @see javax.jms.Session#createTextMessage
	 */
	public Message toMessage(Object object, Session session) throws JMSException {
		return session.createTextMessage(object.toString());
	}

	public Object fromMessage(Message message) {
		return null;
	}

}
