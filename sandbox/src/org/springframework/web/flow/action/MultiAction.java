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
import org.springframework.web.flow.ActionResult;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.MutableAttributesAccessor;
import org.springframework.web.flow.support.FlowUtils;

/**
 * Action that allows multiple event types types to be processed by
 * a single action.
 * 
 * @see org.springframework.web.servlet.mvc.multiaction.MultiActionController
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MultiAction extends AbstractAction {

	/**
	 * @author Keith Donald
	 */
	public interface EventHandlerMethodNameResolver {

		/**
		 * @param eventId
		 * @return
		 */
		String getHandlerMethodName(String eventId);

	}

	private static class DefaultEventHandlerMethodNameResolver implements EventHandlerMethodNameResolver {
		public String getHandlerMethodName(String eventId) {
			return "handle" + StringUtils.capitalize(eventId) + "Event";
		}
	}

	private EventHandlerMethodNameResolver methodNameResolver = new DefaultEventHandlerMethodNameResolver();
	
	public void setMethodNameResolver(EventHandlerMethodNameResolver methodNameResolver) {
		this.methodNameResolver = methodNameResolver;
	}

	protected ActionResult doExecuteAction(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws Exception {
		FlowExecution flowExecution = FlowUtils.getFlowExecution(model);
		String eventId = flowExecution.getLastEventId();
		String handlerMethodName = methodNameResolver.getHandlerMethodName(eventId);
		try {
			Method handlerMethod = getHandlerMethod(handlerMethodName);
			Object result=handlerMethod.invoke(getDelegate(), new Object[] { request, response, model });
			Assert.isInstanceOf(ActionResult.class, result,
					"Event handler methods should return an object of type ActionResult");
			return (ActionResult)result;
		}
		catch (InvocationTargetException e) {
			Throwable t=e.getTargetException();
			if (t instanceof Exception) {
				throw (Exception)e.getTargetException();
			}
			else {
				throw (Error)e.getTargetException();
			}
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