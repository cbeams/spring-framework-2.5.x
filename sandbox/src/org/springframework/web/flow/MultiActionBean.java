/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.action.AbstractActionBean;

/**
 * @author Keith Donald
 */
public class MultiActionBean extends AbstractActionBean {

	private EventHandlerMethodNameResolver methodNameResolver = new DefaultEventHandlerMethodNameResolver();

	private static class DefaultEventHandlerMethodNameResolver implements EventHandlerMethodNameResolver {
		public String getMethodName(String eventId) {
			return "handle" + StringUtils.capitalize(eventId) + "Event";
		}
	}

	protected ActionBeanEvent doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws RuntimeException, ServletRequestBindingException {
		FlowSessionExecutionInfo sessionExecution = FlowUtils.getFlowSessionExecutionInfo(model);
		String eventId = sessionExecution.getLastEventId();
		String handlerMethodName = methodNameResolver.getMethodName(eventId);
		try {
			Method handlerMethod = getHandlerMethod(handlerMethodName);
			return (ActionBeanEvent)handlerMethod.invoke(getDelegate(), new Object[] { request, response, model });
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException("Exception occured dispatching action by reflection on event '" + eventId + "'",
					e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("Exception occured dispatching action by reflection on event '" + eventId + "'",
					e);
		}
		catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			Assert.isTrue(target instanceof RuntimeException,
					"Action bean handlers should only throw runtime exceptions - programmer error");
			throw (RuntimeException)target;
		}
	}

	protected Method getHandlerMethod(String eventHandlerMethodName) throws NoSuchMethodException,
			IllegalAccessException {
		return getDelegate().getClass().getMethod(eventHandlerMethodName,
				new Class[] { HttpServletRequest.class, HttpServletResponse.class, MutableAttributesAccessor.class });
	}

	protected Object getDelegate() {
		return this;
	}

}