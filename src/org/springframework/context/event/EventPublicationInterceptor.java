package org.springframework.context.event;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ApplicationObjectSupport;


/**
 * Interceptor that knows how to publish {@link org.springframework.context.ApplicationEvent}s to all
 * <code>ApplicationListener</code>s registered with <code>ApplicationContext</code> 
 * @author Dmitriy Kopylenko
 * @version $Id: EventPublicationInterceptor.java,v 1.1 2003-10-04 15:58:29 jhoeller Exp $
 */
public class EventPublicationInterceptor extends ApplicationObjectSupport  implements MethodInterceptor, InitializingBean {

	private Class applicationEventClass;

	/**
	 * Set the application event class to publish.
	 */
	public void setApplicationEventClass(Class applicationEventClass) {
		this.applicationEventClass = applicationEventClass;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();
		ApplicationEvent applicationEvent = null;
		Constructor constructor = this.applicationEventClass.getConstructor(new Class[] {Object.class});
		applicationEvent = (ApplicationEvent)constructor.newInstance(new Object[] {invocation.getThis()});
		
		getApplicationContext().publishEvent(applicationEvent);
		
		return retVal;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.applicationEventClass == null || !ApplicationEvent.class.isAssignableFrom(this.applicationEventClass)) {
			throw new IllegalArgumentException("applicationEventClass needs to implement ApplicationEvent");
		}
	}

}
