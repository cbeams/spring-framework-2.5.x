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

import java.lang.reflect.Method;

import org.springframework.binding.AttributeSource;
import org.springframework.util.DispatchMethodInvoker;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;

/**
 * Action implementation that bundles many action execution methods into a
 * single action implementation class. Action execution methods defined by
 * subclasses should follow the following signature:
 * 
 * <pre>
 *     public Event ${method}(RequestContext context) throws Exception
 * </pre>
 * 
 * By default, the ${method} will be the value of the "method" action
 * property associated with the action in the current state, or the name of the
 * current state of the flow if there is no such property defined.
 * So the following state definition
 * 
 * <pre>
 *     &lt;action-state id=&quot;search&quot;&gt;
 *          &lt;action bean=&quot;my.search.action&quot;/&gt;
 *          &lt;transition on=&quot;success&quot; to=&quot;results&quot;/&gt;
 *     &lt;/action-state&gt;
 * </pre>
 * 
 * ... will execute the method:
 * 
 * <pre>
 *     public Event search(RequestContext context) throws Exception
 * </pre>
 * 
 * Alternatively you could have explictly specified the method name:
 * 
 * <pre>
 *     &lt;action-state id=&quot;searchState&quot;&gt;
 *         &lt;action bean=&quot;my.search.action&quot method=&quot;search&quot;/&gt;
 *         &lt;transition on=&quot;success&quot; to=&quot;results&quot;/&gt;
 *     &lt;/action-state&gt;
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
 * <td><i>{@link MultiAction.DefaultActionExecuteMethodNameResolver default}</i></td>
 * <td>Set the strategy used to resolve the name of an action execution method.</td>
 * </tr>
 * </table>
 * 
 * @see MultiAction.ActionExecuteMethodNameResolver
 * @see MultiAction.DefaultActionExecuteMethodNameResolver
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MultiAction extends AbstractAction {

	/**
	 * A cache for dispatched action execute methods.
	 */
	private DispatchMethodInvoker executeMethodDispatcher = new DispatchMethodInvoker(this,
			new Class[] { RequestContext.class }, Event.class, "action");

	/**
	 * The action execute method name resolver strategy.
	 */
	private ActionExecuteMethodNameResolver executeMethodNameResolver = new DefaultActionExecuteMethodNameResolver();

	/**
	 * Returns the delegate object holding the action execution methods.
	 * Defaults to this object.
	 */
	public Object getDelegate() {
		return this.executeMethodDispatcher.getTarget();
	}

	/**
	 * Set the delegate object holding the action execution methods.
	 * @param delegate the delegate to set
	 */
	public void setDelegate(Object delegate) {
		this.executeMethodDispatcher.setTarget(delegate);
	}

	/**
	 * Get the strategy used to resolve action execution method names. Defaults
	 * to current state id based resolution.
	 */
	public ActionExecuteMethodNameResolver getExecuteMethodNameResolver() {
		return executeMethodNameResolver;
	}

	/**
	 * Set the strategy used to resolve action execution method names.
	 */
	public void setExecuteMethodNameResolver(ActionExecuteMethodNameResolver methodNameResolver) {
		this.executeMethodNameResolver = methodNameResolver;
	}

	protected Event doExecuteAction(RequestContext context) throws Exception {
		String actionExecuteMethodName = this.executeMethodNameResolver.getMethodName(context, this);
		return (Event)this.executeMethodDispatcher.dispatch(actionExecuteMethodName, new Object[] { context });
	}

	/**
	 * Find the action execution method with given name on the delegate object
	 * using reflection.
	 */
	protected Method getActionExecuteMethod(String actionExecuteMethodName) {
		return (Method)this.executeMethodDispatcher.getDispatchMethod(actionExecuteMethodName);
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
		 * @param action the multi-action requesting method name resolution
		 * @return the name of the method that should handle action execution
		 */
		public String getMethodName(RequestContext context, MultiAction action);
	}

	/**
	 * Action execution method name resolver that uses the following algorithm
	 * to calculate a method name:
	 * <ol>
	 * <li>If the currently executing action has a "executeMethodName" property
	 * defined, use the value as method name.</li>
	 * <li>Else, use the name of the current state of the flow execution as a
	 * method name.</li>
	 * </ol>
	 * This is the default method name resolver used by the MultiAction class.
	 * 
	 * @author Erwin Vervaet
	 */
	public static class DefaultActionExecuteMethodNameResolver implements ActionExecuteMethodNameResolver {
		
		public static final String METHOD = "method";

		public String getMethodName(RequestContext context, MultiAction action) {
			AttributeSource attributes = context.getActionExecutionAttributes();
			if (attributes.containsAttribute(METHOD)) {
				// use specified execute method name
				return (String)attributes.getAttribute(METHOD);
			}
			else {
				// use current state name as method name
				return context.getCurrentState().getId();
			}
		}
	}
}