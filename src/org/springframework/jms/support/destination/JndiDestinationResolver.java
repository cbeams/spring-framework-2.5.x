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
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.NamingException;

import org.springframework.jndi.JndiLocatorSupport;

/**
 * Implementation of the DestinationResolver interface which interprets
 * destination names as JNDI locations, falling back to dynamic destinations else.
 *
 * <p>Allows for customizing the JNDI environment if necessary,
 * for example specifying appropriate JNDI environment properties.
 *
 * <p>Dynamic queues and topics get cached by destination name.
 * Thus, use unique destination names across both queues and topics.
 * Caching can be turned off by specifying "cache" as "false", if desired.
 *
 * <b>Automatic creation of dynamic destinations is turned off by default.
 * Specify "fallbackToDynamicDestination" as "true" to enable this functionality.
 *
 * @author Mark Pollack
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 * @see #setCache
 * @see #setFallbackToDynamicDestination
 */
public class JndiDestinationResolver extends JndiLocatorSupport implements DestinationResolver {

	private boolean cache = true;

	private boolean fallbackToDynamicDestination = false;

	private DestinationResolver dynamicDestinationResolver = new DynamicDestinationResolver();

	private final Map destinationCache = Collections.synchronizedMap(new HashMap());


	/**
	 * Set whether to cache resolved destinations. Default is true.
	 * <p>Can be turned off to re-lookup a destination for each operation,
	 * which allows for hot restarting of destinations. This is mainly
	 * useful during development.
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	/**
	 * Set the ability of JmsTemplate to create dynamic destinations
	 * if the destination name is not found in JNDI. Default is false.
	 */
	public void setFallbackToDynamicDestination(boolean fallbackToDynamicDestination) {
		this.fallbackToDynamicDestination = fallbackToDynamicDestination;
	}

	/**
	 * Set the DestinationResolver to use when falling back to dynamic destinations.
	 * Default is a DynamicDestinationResolver.
	 * @see #setFallbackToDynamicDestination
	 * @see DynamicDestinationResolver
	 */
	public void setDynamicDestinationResolver(DestinationResolver dynamicDestinationResolver) {
		this.dynamicDestinationResolver = dynamicDestinationResolver;
	}


	public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain)
			throws JMSException {
		Destination dest = (Destination) this.destinationCache.get(destinationName);
		if (dest == null) {
			try {
				dest = (Destination) lookup(destinationName);
			}
			catch (NamingException ex) {
				logger.debug("Destination [" + destinationName + "] not found in JNDI", ex);
				if (this.fallbackToDynamicDestination) {
					dest = this.dynamicDestinationResolver.resolveDestinationName(session, destinationName, pubSubDomain);
				}
				else {
					throw new DestinationResolutionException(
							"Destination [" + destinationName + "] not found in JNDI", ex);
				}
			}
			if (this.cache) {
				this.destinationCache.put(destinationName, dest);
			}
		}
		return dest;
	}

}