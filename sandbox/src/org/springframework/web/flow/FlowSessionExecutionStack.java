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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.Constraint;
import org.springframework.web.util.SessionKeyUtils;

/**
 * A stack tracking the execution of a flow session.
 * 
 * @author Keith Donald
 */
public class FlowSessionExecutionStack implements MutableAttributesAccessor, Serializable, FlowSessionExecutionInfo {
	private static final Log logger = LogFactory.getLog(FlowSessionExecutionStack.class);

	private String id;

	private FlowSession NO_SESSION = new FlowSession(null, null);

	private Stack executingFlowSessions = new Stack();

	private String lastEventId;

	private long lastEventTimestamp;

	public FlowSessionExecutionStack() {
		this.id = SessionKeyUtils.generateMD5SessionKey(String.valueOf(this), true);
	}

	public String getId() {
		return id;
	}

	public String getCaption() {
		return "[sessionId=" + getId() + ", " + getQualifiedActiveFlowId() + "]";
	}

	public boolean isActive() {
		return !isEmpty();
	}

	public boolean isEmpty() {
		return executingFlowSessions.isEmpty();
	}

	public String getActiveFlowId() {
		return getActiveFlowSession().getFlowId();
	}

	public String[] getFlowIdStack() {
		if (isEmpty()) {
			return new String[0];
		}
		else {
			Iterator it = executingFlowSessions.iterator();
			List stack = new ArrayList(executingFlowSessions.size());
			while (it.hasNext()) {
				FlowSession session = (FlowSession)it.next();
				stack.add(session.getFlowId());
			}
			return (String[])stack.toArray(new String[0]);
		}
	}

	public String getQualifiedActiveFlowId() {
		Iterator it = executingFlowSessions.iterator();
		StringBuffer qualifiedName = new StringBuffer(128);
		while (it.hasNext()) {
			FlowSession session = (FlowSession)it.next();
			qualifiedName.append(session.getFlowId());
			if (it.hasNext()) {
				qualifiedName.append('.');
			}
		}
		return qualifiedName.toString();
	}

	public Map getAttributes() {
		return getActiveFlowSession().getAttributes();
	}

	/**
	 * Are we currently in the root flow? There can be any depth of nested
	 * subflows below this, but sometimes the first subflow below the root may
	 * require special treatment.
	 * @return whether we're in the root flow
	 */
	public boolean isRootFlow() {
		return executingFlowSessions.size() == 1;
	}

	public String getCurrentStateId() {
		return getActiveFlowSession().getCurrentStateId();
	}

	public Object getAttribute(String attributeName) {
		if (attributeName.equals(FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME)) {
			return this;
		}
		else {
			return getActiveFlowSession().getAttribute(attributeName);
		}
	}

	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		if (attributeName.equals(FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME)) {
			Assert.isInstanceOf(requiredType, this);
			return this;
		}
		else {
			return getActiveFlowSession().getAttribute(attributeName, requiredType);
		}
	}

	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		if (attributeName.equals(FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME)) {
			return this;
		}
		else {
			return getActiveFlowSession().getRequiredAttribute(attributeName);
		}
	}

	public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		if (attributeName.equals(FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME)) {
			Assert.isInstanceOf(requiredType, this);
			return this;
		}
		else {
			return getActiveFlowSession().getRequiredAttribute(attributeName, requiredType);
		}
	}

	public boolean containsAttribute(String attributeName) {
		return getActiveFlowSession().containsAttribute(attributeName);
	}

	public boolean containsAttribute(String attributeName, Class requiredType) {
		return getActiveFlowSession().containsAttribute(attributeName, requiredType);
	}

	public void assertAttributePresent(String attributeName) {
		getActiveFlowSession().assertAttributePresent(attributeName);
	}

	public void assertAttributePresent(String attributeName, Class requiredType) {
		getActiveFlowSession().assertAttributePresent(attributeName, requiredType);
	}

	public Collection attributeNames() {
		return getActiveFlowSession().attributeNames();
	}

	public Collection attributeValues() {
		return getActiveFlowSession().attributeValues();
	}

	public Collection attributeEntries() {
		return getActiveFlowSession().attributeEntries();
	}

	public Collection findAttributes(Constraint criteria) {
		return getActiveFlowSession().findAttributes(criteria);
	}

	/**
	 * @return
	 */
	public String getLastEventId() {
		return lastEventId;
	}

	public long getLastEventTimestamp() {
		return lastEventTimestamp;
	}

	public void setLastEventId(String eventId) {
		Assert.notNull(eventId, "The eventId is required");
		this.lastEventId = eventId;
		this.lastEventTimestamp = new Date().getTime();
		if (logger.isDebugEnabled()) {
			logger.debug("Set last event id to '" + eventId + "' and updated timestamp to " + this.lastEventTimestamp);
		}
	}

	protected void setCurrentStateId(String id) {
		getActiveFlowSession().setCurrentStateId(id);
	}

	public void setAttribute(String attributeName, Object attributeValue) {
		if (attributeName.equals(FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME)) {
			throw new IllegalArgumentException("Attribute name '" + FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME
					+ "' is reserved for internal use only");
		}
		getActiveFlowSession().setAttribute(attributeName, attributeValue);
	}

	public void setAttributes(Map attributes) {
		getActiveFlowSession().setAttributes(attributes);
	}

	public void removeAttribute(String attributeName) {
		getActiveFlowSession().removeAttribute(attributeName);
	}

	public FlowSession getActiveFlowSession() {
		if (executingFlowSessions.isEmpty()) {
			throw new IllegalStateException("No flow session is executing in this execution stack");
		}
		return (FlowSession)executingFlowSessions.peek();
	}

	protected void push(FlowSession subFlowSession) {
		executingFlowSessions.push(subFlowSession);
		if (logger.isDebugEnabled()) {
			logger.debug("After push of new Flow Session '" + subFlowSession.getFlowId()
					+ "' - excutingFlowSessionsCount=" + executingFlowSessions.size() + ", sessionStack="
					+ executingFlowSessions);
		}
	}

	protected FlowSession pop() {
		FlowSession s = (FlowSession)executingFlowSessions.pop();
		if (logger.isDebugEnabled()) {
			logger.debug("After pop of ended Flow Session '" + s.getFlowId() + "' - excutingFlowSessionsCount="
					+ executingFlowSessions.size() + ", sessionStack=" + executingFlowSessions);
		}
		return s;
	}

	public String toString() {
		return executingFlowSessions.isEmpty() ? "[Empty FlowSessionExecutionStack " + getId()
				+ "; no flows are active]" : new ToStringCreator(this).append("id", getId()).append("activeFlowId",
				getActiveFlowId()).append("currentStateId", getCurrentStateId()).append("rootFlow", isRootFlow())
				.append("executingFlowSessions", executingFlowSessions).toString();
	}
}