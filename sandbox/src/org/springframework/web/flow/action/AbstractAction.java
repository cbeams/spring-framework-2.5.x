/*
 * Copyright 2002-2004 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.LocalEvent;
import org.springframework.web.flow.TransactionSynchronizer;

/**
 * Base action implementation that provides a number of helper methods generally
 * useful to any controller/command action. These include:
 * <ul>
 * <li>Creating common result outcome identifiers
 * <li>Accessing request parameters and session attributes
 * <li>Accessing and exposing form objects
 * <li>Inserting action pre and post execution logic (may also be done with an
 * interceptor)
 * </ul>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractAction implements Action, InitializingBean {

	/**
	 * The form object instance is aliased under this attribute name in the flow
	 * model by the default form setup and bind and validate actions.
	 */
	public static final String FORM_OBJECT_ATTRIBUTE = "formObject";

	/**
	 * The form object errors instance is aliased under this attribute name in
	 * the flow model by the default form setup and bind and validate actions.
	 */
	public static final String FORM_OBJECT_ERRORS_ATTRIBUTE = "formObjectErrors";

	protected final Log logger = LogFactory.getLog(getClass());

	public void afterPropertiesSet() {
		initAction();
	}

	/**
	 * Action initializing callback, may be overriden by subclasses to perform
	 * custom initialization.
	 */
	protected void initAction() {
	}

	// creating common events

	/**
	 * Returns the default error event ("error").
	 */
	protected Event error() {
		return new LocalEvent(FlowConstants.ERROR);
	}

	/**
	 * Returns the default success event ("success").
	 */
	protected Event success() {
		return new LocalEvent(FlowConstants.SUCCESS);
	}

	// action pre and post execution logic

	public final Event execute(FlowExecutionContext context) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Action '" + getClass().getName() + "' beginning execution");
		}
		Event result = onPreExecute(context);
		if (result == null) {
			result = doExecuteAction(context);
			if (logger.isDebugEnabled()) {
				logger.debug("Action '" + getClass().getName() + "' completed execution; event result is " + result);
			}
			onPostExecute(context);
			if (logger.isInfoEnabled()) {
				if (result == null) {
					logger.info("Retured action event is [null]; that's ok so long as another action associated "
							+ "with the currently executing flow state returns a valid event");
				}
			}
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("Action execution disallowed; event is " + result);
			}
		}
		return result;
	}

	/**
	 * Pre-action-execution hook, subclasses may override. If this method
	 * returns a non-<code>null</code> value, the
	 * <code>doExecuteAction()</code> method will <b>not</b> be called and
	 * the returned value will be used to select a transition to trigger in the
	 * calling action state. If this method returns <code>null</code>,
	 * <code>doExecuteAction()</code> will be called to obtain an action
	 * result.
	 * 
	 * <p>
	 * This implementation just returns <code>null</code>.
	 * 
	 * @param request The http request
	 * @param response The http response
	 * @param model The flow data model
	 * @return The non-<code>null</code> action result, in which case the
	 *         <code>doExecuteAction()</code> will not be called. Or
	 *         <code>null</code> if the <code>doExecuteAction()</code>
	 *         method should be called to obtain the action result.
	 * @throws Exception An <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	protected Event onPreExecute(FlowExecutionContext context) throws Exception {
		return null;
	}

	/**
	 * Template hook method subclasses should override to encapsulate their
	 * specific action execution logic.
	 * 
	 * @param request The http request
	 * @param response The http response
	 * @param model The flow data model
	 * @return The action result
	 * @throws Exception An <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	protected abstract Event doExecuteAction(FlowExecutionContext context) throws Exception;

	/**
	 * Post-action execution hook, subclasses may override.
	 * 
	 * <p>
	 * This implementation does nothing.
	 * 
	 * @param request The http request
	 * @param response The http response
	 * @param model The flow data model
	 * @throws Exception An <b>unrecoverable </b> exception occured, either
	 *         checked or unchecked
	 */
	protected void onPostExecute(FlowExecutionContext context) throws Exception {
	}
}