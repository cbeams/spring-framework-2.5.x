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
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.CachingMapTemplate;
import org.springframework.web.flow.ActionExecutionException;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;

/**
 * Action implementation that bundles many action execution methods into a
 * single action implementation class. Action execution methods defined by
 * subclasses should follow the following signature:
 * 
 * <pre>
 *     public Event ${executeMethodName}(RequestContext context)
 * </pre>
 * 
 * By default, the ${executeMethodName} will be the name of the <b>current state</b>
 * of the flow, so the follow state definition
 * 
 * <pre>
 *      &lt;action-state id=&quot;search&quot;&gt;
 *          &lt;action bean=&quot;my.search.action&quot;/&gt;
 *          &lt;transition on=&quot;success&quot; to=&quot;results&quot;/&gt;
 *      &lt;/action-state&gt;
 * </pre>
 * 
 * ... will execute the method:
 * 
 * <pre>
 *      public Event search(RequestContext context)
 * </pre>
 * 
 * <p>
 * A typical use of the MultiAction is to combine all CRUD operations for a
 * certain domain object into a single controller. Another typical use is to
 * centralize form setup and submit logic into one place. This action also
 * allows you to reduce the number of action beans you have to define and
 * configure.
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>Name </b></td>
 * <td><b>Default </b></td>
 * <td><b>Description </b></td>
 * </tr>
 * <tr>
 * <td>delegate</td>
 * <td><i>this</i></td>
 * <td>Set the delegate object holding the action execution methods.</td>
 * </tr>
 * <tr>
 * <td>executeMethodNameResolver</td>
 * <td><i>{@link MultiAction.CurrentStateActionExecuteMethodNameResolver current state}</i></td>
 * <td>Set the strategy used to resolve the name of an action execution method.</td>
 * </tr>
 * </table>
 * 
 * @see MultiAction.ActionExecuteMethodNameResolver
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MultiAction extends AbstractAction {

	/**
	 * A configurable delegate that may handle execution requests -- useful for
	 * invoking legacy code.
	 */
	private Object delegate = this;

	/**
	 * The action execute method name resolver strategy.
	 */
	private ActionExecuteMethodNameResolver executeMethodNameResolver = new CurrentStateActionExecuteMethodNameResolver();

	/**
	 * The resolved action execute method cache.
	 */
	private Map executeMethodCache = new CachingMapTemplate() {
		public Object create(Object key) {
			String executeMethodName = (String)key;
			try {
				return getDelegate().getClass().getMethod(executeMethodName, new Class[] { RequestContext.class });
			}
			catch (NoSuchMethodException e) {
				throw new ActionExecutionException(
						"Unable to resolve action execute method with signature 'public Event " + executeMethodName
								+ "(RequestContext context)' - make sure the method name is correct "
								+ "and such a method is defined in this action implementation", e);
			}
		}
	};

	/**
	 * Set the delegate object holding the action execution methods.
	 * @param delegate the delegate to set
	 */
	public void setDelegate(Object delegate) {
		this.delegate = delegate;
	}

	/**
	 * Set the strategy used to resolve action execution method names.
	 */
	public void setExecuteMethodNameResolver(ActionExecuteMethodNameResolver methodNameResolver) {
		this.executeMethodNameResolver = methodNameResolver;
	}

	protected Event doExecuteAction(RequestContext context) throws Exception {
		String actionExecuteMethodName = this.executeMethodNameResolver.getMethodName(context);
		try {
			Method actionExecuteMethod = getActionExecuteMethod(actionExecuteMethodName);
			Object result = actionExecuteMethod.invoke(getDelegate(), new Object[] { context });
			if (result != null) {
				Assert.isInstanceOf(Event.class, result,
						"Event handler methods should return a result object of type Event");
			}
			return (Event)result;
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
	 * Returns the delegate object holding the action execution methods.
	 * Defaults to this object.
	 */
	public Object getDelegate() {
		return this;
	}

	/**
	 * Get the strategy used to resolve action execution method names. Defaults
	 * to current state id based resolution.
	 */
	public ActionExecuteMethodNameResolver getExecuteMethodNameResolver() {
		return executeMethodNameResolver;
	}

	/**
	 * Find the action execution method with given name on the delegate object
	 * using reflection.
	 */
	protected Method getActionExecuteMethod(String actionExecuteMethodName) {
		return (Method)this.executeMethodCache.get(actionExecuteMethodName);
	}

	/**
	 * Strategy interface used by the MultiAction to map a request context to
	 * the name of an action execution method.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	public interface ActionExecuteMethodNameResolver {

		/**
		 * Resolve a method name from given flow execution request context.
		 * @param context the flow execution request context
		 * @return the name of the method that should handle action execution
		 */
		public String getMethodName(RequestContext context);
	}

	/**
	 * Action execution method name resolver that uses the name of the current
	 * state of the flow execution as a method name. This is the default method
	 * name resolver used by the MultiAction class.
	 * 
	 * @author Erwin Vervaet
	 */
	public static class CurrentStateActionExecuteMethodNameResolver implements ActionExecuteMethodNameResolver {
		public String getMethodName(RequestContext context) {
			return context.getCurrentState().getId();
		}
	}
}