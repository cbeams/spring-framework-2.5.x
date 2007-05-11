/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jms.listener.endpoint;

import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Standard implementation of the {@link JmsActivationSpecFactory} interface.
 * Supports the standard JMS properties as defined by the JMS 1.5 specification
 * (Appendix B); ignores Spring's "maxConcurrency" and "prefetchSize" settings.
 *
 * <p>The 'activationSpecClass' property is required, explicitly defining
 * the fully-qualified class name of the provider's ActivationSpec class
 * (e.g. "org.apache.activemq.ra.ActiveMQActivationSpec").
 *
 * <p>Check out {@link DefaultJmsActivationSpecFactory} for an extended variant
 * of this class, supporting some further default conventions beyond the plain
 * JMS 1.5 specification.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see #setActivationSpecClass
 * @see DefaultJmsActivationSpecFactory
 */
public class StandardJmsActivationSpecFactory implements JmsActivationSpecFactory {

	private Class activationSpecClass;


	/**
	 * Specify the fully-qualified ActivationSpec class name for the target
	 * provider (e.g. "org.apache.activemq.ra.ActiveMQActivationSpec").
	 */
	public void setActivationSpecClass(Class activationSpecClass) {
		this.activationSpecClass = activationSpecClass;
	}


	public ActivationSpec createActivationSpec(ResourceAdapter adapter, JmsActivationSpecConfig config) {
		Class activationSpecClassToUse = this.activationSpecClass;
		if (activationSpecClassToUse == null) {
			activationSpecClassToUse = determineActivationSpecClass(adapter);
			if (activationSpecClassToUse == null) {
				throw new IllegalStateException("Property 'activationSpecClass' is required");
			}
		}
		BeanWrapper bw = new BeanWrapperImpl(activationSpecClassToUse);
		populateActivationSpecProperties(bw, config);
		return (ActivationSpec) bw.getWrappedInstance();
	}

	/**
	 * Determine the ActivationSpec class for the given ResourceAdapter,
	 * if possible. Called if no 'activationSpecClass' has been set explicitly
	 * @param adapter the ResourceAdapter to check
	 * @return the corresponding ActivationSpec class, or <code>null</code>
	 * if not determinable
	 * @see #setActivationSpecClass
	 */
	protected Class determineActivationSpecClass(ResourceAdapter adapter) {
		return null;
	}

	/**
	 * Populate the given ApplicationSpec object with the settings
	 * defined in the given configuration object.
	 * <p>This implementation applies all standard JMS settings, but ignores
	 * "maxConcurrency" and "prefetchSize" - not supported in standard JCA 1.5.
	 * @param bw the BeanWrapper wrapping the ActivationSpec object
	 * @param config the configured object holding common JMS settings
	 */
	protected void populateActivationSpecProperties(BeanWrapper bw, JmsActivationSpecConfig config) {
		bw.setPropertyValue("destination", config.getDestinationName());
		bw.setPropertyValue("destinationType", config.isPubSubDomain() ? Topic.class.getName() : Queue.class.getName());
		bw.setPropertyValue("subscriptionDurability", config.isSubscriptionDurable() ? "Durable" : "NonDurable");
		bw.setPropertyValue("subscriptionName", config.getDurableSubscriptionName());
		bw.setPropertyValue("clientId", config.getClientId());
		bw.setPropertyValue("messageSelector", config.getMessageSelector());

		applyAcknowledgeMode(bw, config.getAcknowledgeMode());
	}

	/**
	 * Apply the specified acknowledge mode to the ActivationSpec object.
	 * <p>This implementation applies the standard JCA 1.5 acknowledge modes
	 * "Auto-acknowledge" and "Dups-ok-acknowledge". It throws an exception in
	 * case of <code>CLIENT_ACKNOWLEDGE</code> or <code>SESSION_TRANSACTED</code>
	 * having been requested.
	 * @param bw the BeanWrapper wrapping the ActivationSpec object
	 * @param ackMode the configured acknowledge mode
	 * (according to the constants in {@link javax.jms.Session}
	 * @see javax.jms.Session#AUTO_ACKNOWLEDGE
	 * @see javax.jms.Session#DUPS_OK_ACKNOWLEDGE
	 * @see javax.jms.Session#CLIENT_ACKNOWLEDGE
	 * @see javax.jms.Session#SESSION_TRANSACTED
	 */
	protected void applyAcknowledgeMode(BeanWrapper bw, int ackMode) {
		if (ackMode == Session.SESSION_TRANSACTED) {
			throw new IllegalArgumentException("No support for SESSION_TRANSACTED: Only \"Auto-acknowledge\" " +
					"and \"Dups-ok-acknowledge\" supported in standard JCA 1.5");
		}
		else if (ackMode == Session.CLIENT_ACKNOWLEDGE) {
			throw new IllegalArgumentException("No support for CLIENT_ACKNOWLEDGE: Only \"Auto-acknowledge\" " +
					"and \"Dups-ok-acknowledge\" supported in standard JCA 1.5");
		}
		else {
			bw.setPropertyValue("acknowledgeMode",
					ackMode == Session.DUPS_OK_ACKNOWLEDGE ? "Dups-ok-acknowledge" : "Auto-acknowledge");
		}
	}

}
