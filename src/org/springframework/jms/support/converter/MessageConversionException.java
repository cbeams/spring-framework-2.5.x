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

import org.springframework.jms.JmsException;

/**
 * Thrown by MessageConverter when it can not convert an object to/from a JMS message.
 * @author Mark Pollack
 * @since 1.1
 * @see MessageConverter
 */
public class MessageConversionException extends JmsException {

	public MessageConversionException(String msg) {
		super(msg);
	}

	public MessageConversionException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
