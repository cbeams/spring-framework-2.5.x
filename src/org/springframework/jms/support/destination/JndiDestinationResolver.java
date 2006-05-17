/*
 * Copyright 2002-2006 the original author or authors.
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
 * {@link DestinationResolver} implementation which interprets destination names
 * as JNDI locations (with a configurable fallback strategy).
 *
 * <p>Allows for customizing the JNDI environment if necessary,
 * for example specifying appropriate JNDI environment properties.
 *
 * <p>Dynamic queues and topics get cached by destination name.
 * Thus, use unique destination names across both queues and topics.
 * Caching can be turned off by specifying {@link #cache} as <code>false</code>.
 *
 * <b>Please note that the fallback to resolution of dynamic destinations is turned
 * off by default. Specify
 * {@link #fallbackToDynamicDestination fallbackToDynamicDestination} as
 * <code>true</code> to enable this functionality.
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
	 * Set whether to cache resolved destinations. Default is <code>true</code> .
	 * <p>Can be turned off to re-lookup a destination for each operation,
	 * which allows for hot restarting of destinations. This is mainly
	 * useful during development.
     * <p>Please note that dynamic queues and topics get cached by destination name.
     * Thus, use unique destination names across both queues and topics.
     * @param cache <code>true</code> if resolved destinations are to be cached 
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	/**
	 * Set the ability of {@link JndiDestinationResolver} to create dynamic destinations
	 * if the destination name is not found in JNDI. Default is <code>false</code>.
     * @param fallbackToDynamicDestination <code>true</code> if this {@link JndiDestinationResolver} instance is to
     * fallback to resolving destinations dynamically
     * @see #setDynamicDestinationResolver(DestinationResolver)  
	 */
	public void setFallbackToDynamicDestination(boolean fallbackToDynamicDestination) {
		this.fallbackToDynamicDestination = fallbackToDynamicDestination;
	}

	/**
	 * Set the {@link DestinationResolver} to use when falling back to dynamic
     * destinations.
	 * <p>The default is a {@link DynamicDestinationResolver}.
     * @param dynamicDestinationResolver the {@link DestinationResolver} to use when falling back to dynamic destinations
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
				dest = (Destination) lookup(destinationName, Destination.class);
			}
			catch (NamingException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Destination [" + destinationName + "] not found in JNDI", ex);
				}
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
