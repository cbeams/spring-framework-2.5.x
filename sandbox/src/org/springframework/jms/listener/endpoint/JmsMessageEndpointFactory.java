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

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;

import org.springframework.jca.endpoint.AbstractMessageEndpointFactory;

/**
 * @author Juergen Hoeller
 * @since 2.1
 */
public class JmsMessageEndpointFactory extends AbstractMessageEndpointFactory  {

	private MessageListener messageListener;


	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	protected AbstractMessageEndpoint createEndpointInternal() throws UnavailableException {
		return new JmsMessageEndpoint();
	}


	private class JmsMessageEndpoint extends AbstractMessageEndpoint implements MessageListener {

		public void onMessage(Message message) {
			boolean applyDeliveryCalls = !hasBeforeDeliveryBeenCalled();
			if (applyDeliveryCalls) {
				try {
					beforeDelivery(null);
				}
				catch (ResourceException ex) {
					throw new JmsResourceException(ex);
				}
			}
			try {
				messageListener.onMessage(message);
			}
			catch (RuntimeException ex) {
				onEndpointException(ex);
				throw ex;
			}
			catch (Error err) {
				onEndpointException(err);
				throw err;
			}
			finally {
				if (applyDeliveryCalls) {
					try {
						afterDelivery();
					}
					catch (ResourceException ex) {
						throw new JmsResourceException(ex);
					}
				}
			}
		}

		protected ClassLoader getEndpointClassLoader() {
			return messageListener.getClass().getClassLoader();
		}
	}


	public static class JmsResourceException extends RuntimeException {

		public JmsResourceException(ResourceException cause) {
			super(cause);
		}
	}

}
