/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package org.springframework.context.event;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Interceptor that knows how to publish ApplicationEvents to all ApplicationListeners
 * registered with an ApplicationEventPublisher (typically an ApplicationContext).
 *
 * <p>Note that this interceptor is only capable of publishing <i>stateless</i>
 * events configured statically via the "applicationEventClass" property.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @see #setApplicationEventClass
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.context.ApplicationContext
 */
public class EventPublicationInterceptor
		implements MethodInterceptor, ApplicationEventPublisherAware, InitializingBean {

	private Class applicationEventClass;

	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Set the application event class to publish.
	 * <p>
	 * The event class must have a constructor with a single Object argument for the event source. The interceptor will
	 * pass in the invoked object.
	 */
	public void setApplicationEventClass(Class applicationEventClass) {
		this.applicationEventClass = applicationEventClass;
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.applicationEventClass == null || !ApplicationEvent.class.isAssignableFrom(this.applicationEventClass)) {
			throw new IllegalStateException("applicationEventClass is required and needs to extend ApplicationEvent");
		}
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();
		Constructor constructor = this.applicationEventClass.getConstructor(new Class[]{Object.class});
		ApplicationEvent event = (ApplicationEvent) constructor.newInstance(new Object[]{invocation.getThis()});
		this.applicationEventPublisher.publishEvent(event);
		return retVal;
	}

}
