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
package org.springframework.web.flow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeSetter;

/**
 * A context for a currently executing flow.
 * @author Keith Donald
 */
public interface FlowExecutionContext extends AttributeAccessor {

	/**
	 * Returns this flow execution's root flow definition.
	 * @return The root flow definition.
	 */
	public Flow getRootFlow();

	/**
	 * Returns this flow execution's active flow definition.
	 * @return The active flow definition
	 * @throws IllegalStateException the flow execution is not active
	 */
	public Flow getActiveFlow() throws IllegalStateException;

	/**
	 * Returns the last event signaled within this flow context. The event may
	 * or may not have caused a state transition to happen.
	 * @return The last signaled event
	 */
	public Event getEvent();

	/**
	 * Returns a mutable list of listeners attached to this flow execution.
	 * @return The flow execution listener list
	 */
	public FlowExecutionListenerList getListenerList();

	/**
	 * Is the flow execution managed in this context active?
	 * @return true if yes, false otherwise
	 */
	public boolean isFlowExecutionActive();

	/**
	 * Is given request participating in the active transaction of the model?
	 * @param request the current HTTP request
	 * @param reset indicates whether or not the transaction should end after
	 *        checking it
	 * @return True when the request is participating in the active transaction
	 *         of the model, false otherwise
	 */
	public boolean inTransaction(boolean reset);

	/**
	 * Assert that given request is participating in the active transaction of
	 * the model.
	 * @param request the current HTTP request
	 * @param reset indicates whether or not the transaction should end after
	 *        checking it
	 * @throws IllegalStateException The request is not participating in the
	 *         active transaction of the model or there is no transaction active
	 *         in the model
	 */
	public void assertInTransaction(boolean reset) throws IllegalStateException;

	/**
	 * Start a new transaction on this context.
	 */
	public void beginTransaction();

	/**
	 * End the active transaction on this context.
	 */
	public void endTransaction();

	/**
	 * @return
	 */
	public AttributeSetter getRequestAttributeAccessor();

	/**
	 * @param attributeName
	 * @return
	 */
	public Object getRequestAttribute(String attributeName);

	/**
	 * @param attributeName
	 * @param requiredType
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getRequestAttribute(String attributeName, Class requiredType) throws IllegalStateException;

	/**
	 * @param attributeName
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getRequiredRequestAttribute(String attributeName) throws IllegalStateException;

	/**
	 * @param attributeName
	 * @param requiredType
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getRequiredRequestAttribute(String attributeName, Class requiredType) throws IllegalStateException;

	/**
	 * @param attributeName
	 * @param attributeValue
	 */
	public void setRequestAttribute(String attributeName, Object attributeValue);

	/**
	 * @param attributes
	 */
	public void setRequestAttributes(Map attributes);

	/**
	 * @param attributeName
	 * @return
	 */
	public Object removeRequestAttribute(String attributeName);

	/**
	 * @return
	 */
	public AttributeSetter getFlowAttributeAccessor();

	/**
	 * @param attributeName
	 * @return
	 */
	public Object getFlowAttribute(String attributeName);

	/**
	 * @param attributeName
	 * @param requiredType
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getFlowAttribute(String attributeName, Class requiredType) throws IllegalStateException;

	/**
	 * @param attributeName
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getRequiredFlowAttribute(String attributeName) throws IllegalStateException;

	/**
	 * @param attributeName
	 * @param requiredType
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getRequiredFlowAttribute(String attributeName, Class requiredType) throws IllegalStateException;

	/**
	 * @param attributeName
	 * @param attributeValue
	 */
	public void setFlowAttribute(String attributeName, Object attributeValue);

	/**
	 * @param attributes
	 */
	public void setFlowAttributes(Map attributes);
	
	/**
	 * @param attributeName
	 * @return
	 */
	public Object removeFlowAttribute(String attributeName);
	
	/**
	 * Returns the data model for this flow model, suitable for exposing to web
	 * views.
	 * @return Map of model attributes for this flow model.
	 */
	public Map getModel();
}