/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.ActionBeanEvent;
import org.springframework.web.flow.EventHandlerMethodNameResolver;
import org.springframework.web.flow.FlowSessionExecutionInfo;
import org.springframework.web.flow.FlowUtils;
import org.springframework.web.flow.MutableAttributesAccessor;

/**
 * @author Keith Donald
 */
public class MultiActionBean extends AbstractActionBean {

	private EventHandlerMethodNameResolver methodNameResolver = new DefaultEventHandlerMethodNameResolver();

	private static class DefaultEventHandlerMethodNameResolver implements EventHandlerMethodNameResolver {
		public String getHandlerMethodName(String eventId) {
			return "handle" + StringUtils.capitalize(eventId) + "Event";
		}
	}

	protected ActionBeanEvent doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws RuntimeException, ServletRequestBindingException {
		FlowSessionExecutionInfo sessionExecution = FlowUtils.getFlowSessionExecutionInfo(model);
		String eventId = sessionExecution.getLastEventId();
		String handlerMethodName = methodNameResolver.getHandlerMethodName(eventId);
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