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

/**
 * @author Juergen Hoeller
 * @since 2.1
 */
public class JmsActivationSpecConfig {

	private String destinationName;

	private boolean pubSubDomain = false;

	private boolean subscriptionDurable = false;

	private String durableSubscriptionName;

	private String clientId;

	private String messageSelector;

	private int concurrentConsumers = 1;


	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getDestinationName() {
		return this.destinationName;
	}

	public void setPubSubDomain(boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	public boolean isPubSubDomain() {
		return this.pubSubDomain;
	}

	public void setSubscriptionDurable(boolean subscriptionDurable) {
		this.subscriptionDurable = subscriptionDurable;
	}

	public boolean isSubscriptionDurable() {
		return this.subscriptionDurable;
	}

	public void setDurableSubscriptionName(String durableSubscriptionName) {
		this.durableSubscriptionName = durableSubscriptionName;
	}

	public String getDurableSubscriptionName() {
		return this.durableSubscriptionName;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return this.clientId;
	}

	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}

	public String getMessageSelector() {
		return this.messageSelector;
	}

	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}

	public int getConcurrentConsumers() {
		return this.concurrentConsumers;
	}

}
