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

package org.springframework.jms.support.destination;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicSession;

/**
 * Simple implementation of the DestinationResolver interface,
 * resolving destination names as dynamic destinations.
 *
 * <p>This implementation will work on both JMS 1.1 and JMS 1.0.2,
 * because it uses the QueueSession respectively TopicSession methods
 * if possible, falling back to JMS 1.1's generic Session methods.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see javax.jms.QueueSession#createQueue
 * @see javax.jms.TopicSession#createTopic
 * @see javax.jms.Session#createQueue
 * @see javax.jms.Session#createTopic
 */
public class DynamicDestinationResolver implements DestinationResolver {

	public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain)
			throws JMSException {
		if (pubSubDomain) {
			if (session instanceof TopicSession) {
				// cast to TopicSession: will work on both JMS 1.1 and 1.0.2
				return ((TopicSession) session).createTopic(destinationName);
			}
			else {
				// fallback to generic JMS Session: will only work on JMS 1.1
				return session.createTopic(destinationName);
			}
		}
		else {
			if (session instanceof QueueSession) {
				// cast to QueueSession: will work on both JMS 1.1 and 1.0.2
				return ((QueueSession) session).createQueue(destinationName);
			}
			else {
				// fallback to generic JMS Session: will only work on JMS 1.1
				return session.createQueue(destinationName);
			}
		}
	}

}
