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
package org.springframework.web.flow.execution.portlet;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.RandomGuid;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.execution.FlowExecution;
import org.springframework.web.flow.execution.FlowExecutionStorage;
import org.springframework.web.flow.execution.FlowExecutionStorageException;
import org.springframework.web.flow.execution.NoSuchFlowExecutionException;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * Flow execution storage implementation that stores the flow execution in the
 * portlet session.
 * <p>
 * This storage strategy requires a <code>PortletRequestEvent</code>.
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 */
public class PortletSessionFlowExecutionStorage implements FlowExecutionStorage {

	protected final Log logger = LogFactory.getLog(PortletSessionFlowExecutionStorage.class);

	private boolean createSession = true;

	/**
	 * Returns whether or not a portlet session should be created if non exists.
	 * Defaults to true.
	 */
	public boolean isCreateSession() {
		return createSession;
	}

	/**
	 * Set whether or not a portlet session should be created if non exists.
	 */
	public void setCreateSession(boolean createSession) {
		this.createSession = createSession;
	}

	public FlowExecution load(String id, Event requestingEvent)
			throws NoSuchFlowExecutionException, FlowExecutionStorageException {
		try {
			return (FlowExecution) PortletUtils.getRequiredSessionAttribute(getPortletRequest(requestingEvent), id);
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(id, e);
		}
	}

	public String save(String id, FlowExecution flowExecution,
			Event requestingEvent) throws FlowExecutionStorageException {
		if (id == null) {
			id = createId();
			if (logger.isDebugEnabled()) {
				logger.debug("Saving flow execution in portlet session using id '" + id + "'");
			}
		}
		// always update session attribute, even if just overwriting
		// an existing one to make sure the portlet engine knows that this
		// attribute has changed!
		getPortletSession(requestingEvent).setAttribute(id, flowExecution);
		return id;
	}

	public void remove(String id, Event requestingEvent) throws FlowExecutionStorageException {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution with id '" + id + "' from portlet session");
		}
		getPortletSession(requestingEvent).removeAttribute(id);
	}

	/**
	 * Helper to generate a unique id for a flow execution in the storage.
	 */
	protected String createId() {
		return new RandomGuid().toString();
	}

	/**
	 * Return (cast) given event as a PortletRequestEvent.
	 */
	protected PortletEvent getPortletRequestEvent(Event event) {
		Assert.isInstanceOf(PortletEvent.class, event, "Wrong event type: ");
		return (PortletEvent) event;
	}

	/**
	 * Helper to get the portlet request from given event.
	 */
	protected PortletRequest getPortletRequest(Event event) {
		return getPortletRequestEvent(event).getRequest();
	}

	/**
	 * Helper to get the portlet session associated with the portlet request
	 * embedded in given event.
	 */
	protected PortletSession getPortletSession(Event event) {
		return getPortletRequest(event).getPortletSession(isCreateSession());
	}
}