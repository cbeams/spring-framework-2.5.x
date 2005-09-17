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

package org.springframework.jms.support.destination;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.springframework.jms.support.JmsAccessor;

/**
 * Base class for JmsTemplate and other JMS-accessing gateway helpers,
 * adding destination-related properties to JmsAccessor's common properties.
 *
 * <p>Not intended to be used directly. See JmsTemplate.
 *
 * @author Juergen Hoeller
 * @since 1.2.5
 * @see org.springframework.jms.support.JmsAccessor
 * @see org.springframework.jms.core.JmsTemplate
 */
public class JmsDestinationAccessor extends JmsAccessor {

	private boolean pubSubDomain = false;

	private DestinationResolver destinationResolver;


	/**
	 * Configure the destination accessor with knowledge of the JMS domain used.
	 * Default is Point-to-Point (Queues).
	 * <p>For JMS 1.0.2 based accessors, this tells the JMS provider which class hierarchy
	 * to use in the implementation of its operations. For JMS 1.1 based accessors, this
	 * setting does usually not affect operations. However, for both JMS versions, this
	 * setting tells what type of destination to create if dynamic destinations are enabled.
	 * @param pubSubDomain "true" for Publish/Subscribe domain (Topics),
	 * "false" for Point-to-Point domain (Queues)
	 * @see #setDestinationResolver
	 */
	public void setPubSubDomain(boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	/**
	 * Return whether the Publish/Subscribe domain (Topics) is used.
	 * Otherwise, the Point-to-Point domain (Queues) is used.
	 */
	public boolean isPubSubDomain() {
		return pubSubDomain;
	}

	/**
	 * Set the destination resolver for this accessor. Used to resolve
	 * destination names and to support dynamic destination functionality.
	 * <p>The default resolver is a DynamicDestinationResolver. Specify a
	 * JndiDestinationResolver for resolving destination names as JNDI locations.
	 * @see org.springframework.jms.support.destination.DynamicDestinationResolver
	 * @see org.springframework.jms.support.destination.JndiDestinationResolver
	 */
	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	/**
	 * Return the destination resolver for this accessor.
	 */
	public DestinationResolver getDestinationResolver() {
		return destinationResolver;
	}


	/**
	 * Resolve the given destination name into a JMS Destination,
	 * via this accessor's DestinationResolver.
	 * @param session the current JMS Session
	 * @param destinationName the name of the destination
	 * @return the located Destination
	 * @throws javax.jms.JMSException if resolution failed
	 * @see #setDestinationResolver
	 */
	protected Destination resolveDestinationName(Session session, String destinationName) throws JMSException {
		return getDestinationResolver().resolveDestinationName(session, destinationName, isPubSubDomain());
	}

}
