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
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.MutableFlowModel;
import org.springframework.web.flow.support.FlowUtils;

/**
 * Action that allows multiple event types to be processed by a single action.
 * The action will take the last event id executed by the containing flow and
 * will map it to an event handling method using an instance of
 * <code>EventHandlerMethodNameResolver</code>. The event handling method
 * will then be invoked to take care of the required processing.
 * 
 * <p>
 * By default, the event handling method for event <code>save</code> should
 * have the following signature, where the method parameters are similar to
 * those of the <code>doExecuteAction</code> method on the action itself.
 * 
 * <pre>
 * 
 *  public String handleSaveEvent(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model) {
 *     ...
 *  }
 *  
 * </pre>
 * 
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>Name </b></td>
 * <td><b>Default </b></td>
 * <td><b>Description </b></td>
 * </tr>
 * <tr>
 * <td>delegate</td>
 * <td><i>this </i></td>
 * <td>Set the delegate object holding the event handler methods.</td>
 * </tr>
 * <tr>
 * <td>methodNameResolver</td>
 * <td><i>{@link MultiAction.EventHandlerMethodNameResolver}</i></td>
 * <td>Set the strategy used to resolve event ids to event handling method
 * names.</td>
 * </tr>
 * </table>
 * 
 * @see MultiAction.EventHandlerMethodNameResolver
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MultiAction extends AbstractAction {

	/**
	 * Strategy interface used by the MultiAction to map an event to an event
	 * handling method to do appropriate processing.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	public interface EventHandlerMethodNameResolver {
		/**
		 * Resolve an event id to an event handling method name.
		 * @param eventId The event id to resolve
		 * @return The name of the method that should handle processing
		 */
		public String getHandlerMethodName(String eventId);
	}

	/**
	 * The default <code>EventHandlerMethodNameResolver</code> implementation.
	 * This resolver prefixes the event id with "handle" and appends the "Event"
	 * suffix. The event id itself is properly capitalized. So event id "save"
	 * would be resolved to event handler method name "handleSaveEvent".
	 * 
	 * @author Erwin Vervaet
	 */
	public static class DefaultEventHandlerMethodNameResolver implements EventHandlerMethodNameResolver {
		public String getHandlerMethodName(String eventId) {
			return "handle" + StringUtils.capitalize(eventId) + "Event";
		}
	}

	private Object delegate = this;

	private EventHandlerMethodNameResolver methodNameResolver = new DefaultEventHandlerMethodNameResolver();

	/**
	 * Returns the delegate object holding the event handler methods. Defaults
	 * to this object.
	 */
	protected Object getDelegate() {
		return this;
	}

	/**
	 * Set the delegate object holding the event handler methods.
	 * @param delegate The delegate to set.
	 */
	protected void setDelegate(Object delegate) {
		this.delegate = delegate;
	}

	/**
	 * Set the strategy used to resolve event ids to event handling method
	 * names.
	 */
	public void setMethodNameResolver(EventHandlerMethodNameResolver methodNameResolver) {
		this.methodNameResolver = methodNameResolver;
	}

	protected String doExecuteAction(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		FlowExecution flowExecution = FlowUtils.getFlowExecution(model);
		String eventId = flowExecution.getLastEventId();
		String handlerMethodName = methodNameResolver.getHandlerMethodName(eventId);
		try {
			Method handlerMethod = getHandlerMethod(handlerMethodName);
			Object result = handlerMethod.invoke(getDelegate(), new Object[] { request, response, model });
			Assert.isInstanceOf(String.class, result,
					"Event handler methods should return a result object of type String");
			return (String)result;
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof Exception) {
				throw (Exception)e.getTargetException();
			}
			else {
				throw (Error)e.getTargetException();
			}
		}
	}

	/**
	 * Find the event handler method with given name on the delegate object
	 * using reflection.
	 */
	protected Method getHandlerMethod(String eventHandlerMethodName) throws NoSuchMethodException,
			IllegalAccessException {
		return getDelegate().getClass().getMethod(eventHandlerMethodName,
				new Class[] { HttpServletRequest.class, HttpServletResponse.class, MutableFlowModel.class });
	}
}