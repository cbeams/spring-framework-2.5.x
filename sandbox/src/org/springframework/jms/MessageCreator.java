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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * The callback interface used by JmsSender.
 * This interface creates a JMS message given a session, provided
 * by the JmsSender.  
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * checked JMSException (from javax.jms) that may be thrown from 
 * operations they attempt.  The JmsSender will catch and handle 
 * these JMSExceptions appropriately.
 *
 * If extra parameters need to be set, such as the delivery mode, priority or time
 * to live, override any of the getters with your own implementation.
 */
public interface MessageCreator {

	/**
	 * Implement this method to return a message to be sent.
	 *
	 * @param session The JMS session
	 * @return The message to be sent.
	 * @throws The JMS Checked Exception.  Do not catch it, it will be
	 * handled correctly by the JmsSender.
	 */
	public Message createMessage(Session session) throws JMSException;


}
