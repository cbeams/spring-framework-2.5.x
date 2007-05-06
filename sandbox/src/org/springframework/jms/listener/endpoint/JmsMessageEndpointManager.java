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

import javax.jms.MessageListener;
import javax.resource.ResourceException;

import org.springframework.jca.endpoint.GenericMessageEndpointManager;

/**
 * @author Juergen Hoeller
 * @since 2.1
 */
public class JmsMessageEndpointManager extends GenericMessageEndpointManager {

	private final JmsMessageEndpointFactory endpointFactory = new JmsMessageEndpointFactory();

	private boolean messageListenerSet = false;

	private JmsActivationSpecFactory activationSpecFactory;

	private JmsActivationSpecConfig activationSpecConfig;


	public void setMessageListener(MessageListener messageListener) {
		this.endpointFactory.setMessageListener(messageListener);
		this.messageListenerSet = true;
	}

	public void setTransactionManager(Object transactionManager) {
		this.endpointFactory.setTransactionManager(transactionManager);
	}

	public void setActivationSpecFactory(JmsActivationSpecFactory activationSpecFactory) {
		this.activationSpecFactory = activationSpecFactory;
	}

	public void setActivationSpecConfig(JmsActivationSpecConfig activationSpecConfig) {
		this.activationSpecConfig = activationSpecConfig;
	}


	public void afterPropertiesSet() throws ResourceException {
		if (this.messageListenerSet) {
			setMessageEndpointFactory(this.endpointFactory);
		}
		if (this.activationSpecConfig != null) {
			if (this.activationSpecFactory == null) {
				throw new IllegalStateException(
						"Property 'activationSpecConfig' requires property 'activationSpecFactory' to be set");
			}
			setActivationSpec(this.activationSpecFactory.createActivationSpec(this.activationSpecConfig));
		}
		super.afterPropertiesSet();
	}

}
