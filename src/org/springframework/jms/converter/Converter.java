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

package org.springframework.jms.converter;

import javax.jms.Message;
import javax.jms.Session;

/**
 * Converter between Java Objects and JMS Messages.
 * @author Mark Pollack
 *
 */
public interface Converter {

	/**
	 * Convert a Java object to a JMS Message using the supplied session to create
	 * the mesage object.
	 * @param object The object to convert
	 * @param session The session to use for creating a JMS Message
	 * @return
	 */
	Message toMessage(Object object, Session session);

	/**
	 * Convert from a JMS Message to a Java object.
	 * @param message The message to convert
	 * @return the converter Java object.
	 */
	Object fromMessage(Message message);


}
