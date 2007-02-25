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

import java.util.Arrays;

import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.xa.XAResource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * @author Juergen Hoeller
 * @since 2.1
 */
public class GenericMessageEndpointFactory extends AbstractMessageEndpointFactory {

	private Object messageListener;


	public void setMessageListener(Object messageListener) {
		this.messageListener = messageListener;
	}

	public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
		GenericMessageEndpoint endpoint = (GenericMessageEndpoint) super.createEndpoint(xaResource);
		ProxyFactory proxyFactory = new ProxyFactory(this.messageListener);
		DelegatingIntroductionInterceptor introduction = new DelegatingIntroductionInterceptor(endpoint);
		introduction.suppressInterface(MethodInterceptor.class);
		proxyFactory.addAdvice(introduction);
		return (MessageEndpoint) proxyFactory.getProxy();
	}

	protected AbstractMessageEndpoint createEndpointInternal() throws UnavailableException {
		return new GenericMessageEndpoint();
	}


	private class GenericMessageEndpoint extends AbstractMessageEndpoint implements MethodInterceptor {

		public Object invoke(MethodInvocation methodInvocation) throws Throwable {
			boolean applyDeliveryCalls = !hasBeforeDeliveryBeenCalled();
			if (applyDeliveryCalls) {
				try {
					beforeDelivery(null);
				}
				catch (ResourceException ex) {
					if (Arrays.asList(methodInvocation.getMethod().getExceptionTypes()).contains(ResourceException.class)) {
						throw ex;
					}
					else {
						throw new InternalResourceException(ex);
					}
				}
			}
			try {
				return methodInvocation.proceed();
			}
			catch (Throwable ex) {
				onEndpointException(ex);
				throw ex;
			}
			finally {
				if (applyDeliveryCalls) {
					try {
						afterDelivery();
					}
					catch (ResourceException ex) {
						if (Arrays.asList(methodInvocation.getMethod().getExceptionTypes()).contains(ResourceException.class)) {
							throw ex;
						}
						else {
							throw new InternalResourceException(ex);
						}
					}
				}
			}
		}

		protected ClassLoader getEndpointClassLoader() {
			return messageListener.getClass().getClassLoader();
		}
	}


	public static class InternalResourceException extends RuntimeException {

		public InternalResourceException(ResourceException cause) {
			super(cause);
		}
	}

}
