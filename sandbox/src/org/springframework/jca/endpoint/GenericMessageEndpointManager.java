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

package org.springframework.jca.endpoint;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

/**
 * @author Juergen Hoeller
 * @since 2.1
 */
public class GenericMessageEndpointManager implements InitializingBean, Lifecycle, DisposableBean {

	private ResourceAdapter resourceAdapter;

	private MessageEndpointFactory messageEndpointFactory;

	private ActivationSpec activationSpec;

	private boolean autoStartup = true;

	private boolean running = false;

	private final Object lifecycleMonitor = new Object();


	public void setResourceAdapter(ResourceAdapter resourceAdapter) {
		this.resourceAdapter = resourceAdapter;
	}

	public void setMessageEndpointFactory(MessageEndpointFactory messageEndpointFactory) {
		this.messageEndpointFactory = messageEndpointFactory;
	}

	public void setActivationSpec(ActivationSpec activationSpec) {
		this.activationSpec = activationSpec;
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}


	public void afterPropertiesSet() throws ResourceException {
		if (this.resourceAdapter == null) {
			throw new IllegalArgumentException("Property 'resourceAdapter' is required");
		}
		if (this.messageEndpointFactory == null) {
			throw new IllegalArgumentException("Property 'messageEndpointFactory' is required");
		}
		if (this.activationSpec == null) {
			throw new IllegalArgumentException("Property 'activationSpec' is required");
		}

		if (this.activationSpec.getResourceAdapter() == null) {
			this.activationSpec.setResourceAdapter(this.resourceAdapter);
		}
		else if (this.activationSpec.getResourceAdapter() != this.resourceAdapter) {
			throw new IllegalArgumentException("ActivationSpec [" + this.activationSpec +
					"] is associated with a different ResourceAdapter: " + this.resourceAdapter);
		}

		if (this.autoStartup) {
			start();
		}
	}

	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (!this.running) {
				try {
					this.resourceAdapter.endpointActivation(this.messageEndpointFactory, this.activationSpec);
				}
				catch (ResourceException ex) {
					throw new IllegalStateException("Could not activate message endpoint", ex);
				}
				this.running = true;
			}
		}
	}

	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (this.running) {
				this.resourceAdapter.endpointDeactivation(this.messageEndpointFactory, this.activationSpec);
				this.running = false;
			}
		}
	}

	public boolean isRunning() {
		synchronized (this.lifecycleMonitor) {
			return this.running;
		}
	}

	public void destroy() {
		stop();
	}

}
