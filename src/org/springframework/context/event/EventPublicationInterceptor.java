package org.springframework.context.event;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ApplicationObjectSupport;

/**
 * Interceptor that knows how to publish {@link org.springframework.context.ApplicationEvent}s to all
 * <code>ApplicationListener</code>s registered with <code>ApplicationContext</code> 
 * @author Dmitriy Kopylenko
 * @version $Id: EventPublicationInterceptor.java,v 1.2 2003-10-23 18:45:26 uid112313 Exp $
 */
public class EventPublicationInterceptor extends ApplicationObjectSupport implements MethodInterceptor {

	private Class applicationEventClass;

	/**
	 * Set the application event class to publish.
	 * <p>The event class must have a constructor with a single Object argument
	 * for the event source. The interceptor will pass in the invoked object.
	 */
	public void setApplicationEventClass(Class applicationEventClass) {
		if (applicationEventClass == null || !ApplicationEvent.class.isAssignableFrom(applicationEventClass)) {
			throw new IllegalArgumentException("applicationEventClass needs to implement ApplicationEvent");
		}
		this.applicationEventClass = applicationEventClass;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();
		Constructor constructor = this.applicationEventClass.getConstructor(new Class[] {Object.class});
		ApplicationEvent applicationEvent = (ApplicationEvent) constructor.newInstance(new Object[] {invocation.getThis()});
		getApplicationContext().publishEvent(applicationEvent);
		return retVal;
	}

}
