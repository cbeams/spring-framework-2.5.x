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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;

import org.springframework.jms.JmsException;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationFactory;

/**
 * Simple implementation of the DestinationResolver interface,
 * resolving destination names as dynamic destinations.
 *
 * <p>Caches created topics and queues.
 *
 * @author Juergen Hoeller
 * @since 20.07.2004
 * @see org.springframework.jms.support.destination.DynamicDestinationFactory#createQueue
 * @see org.springframework.jms.support.destination.DynamicDestinationFactory#createTopic
 */
public class DynamicDestinationResolver implements DestinationResolver {

	private final Map queueMap = Collections.synchronizedMap(new HashMap());

	private final Map topicMap = Collections.synchronizedMap(new HashMap());

	public Destination resolveDestinationName(String destinationName, boolean isPubSubDomain,
	                                          DynamicDestinationFactory factory) throws JmsException {
		Destination dest = null;
		if (isPubSubDomain) {
			dest = (Destination) this.topicMap.get(destinationName);
		}
		else {
			dest = (Destination) this.queueMap.get(destinationName);
		}
		if (dest == null) {
			if (isPubSubDomain) {
				dest = factory.createTopic(destinationName);
				this.topicMap.put(destinationName, dest);
			}
			else {
				dest = factory.createQueue(destinationName);
				this.queueMap.put(destinationName, dest);
			}
		}
		return dest;
	}

}
