/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ToStringCreator;

/**
 * A stack tracking the execution of a flow session.
 * 
 * @author Keith Donald
 */
public class FlowSessionExecutionStack implements MutableAttributesAccessor, Serializable {
    private static final Log logger = LogFactory.getLog(FlowSessionExecutionStack.class);

    private FlowSession NO_SESSION = new FlowSession(null, null);

    private Stack executingFlowSessions = new Stack();

    public boolean isActive() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return executingFlowSessions.isEmpty();
    }

    public String getActiveFlowId() {
        return getActiveFlowSession().getFlowId();
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

    public void setCurrentState(String id) {
        getActiveFlowSession().setCurrentStateId(id);
    }

    public Object getAttribute(String attributeName) {
        return getActiveFlowSession().getAttribute(attributeName);
    }

    public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
        return getActiveFlowSession().getAttribute(attributeName, requiredType);
    }

    public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
        return getActiveFlowSession().getRequiredAttribute(attributeName);
    }

    public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException {
        return getActiveFlowSession().getRequiredAttribute(attributeName, requiredType);
    }

    public boolean containsAttribute(String attributeName) {
        return getActiveFlowSession().containsAttribute(attributeName);
    }

    public Collection attributeNames() {
        return getActiveFlowSession().attributeNames();
    }

    public Collection attributeEntries() {
        return getActiveFlowSession().attributeEntries();
    }

    public Collection attributeValues() {
        return getActiveFlowSession().attributeValues();
    }

    public void setAttribute(String attributeName, Object attributeValue) {
        getActiveFlowSession().setAttribute(attributeName, attributeValue);
    }

    public void setAttributes(Map attributes) {
        getActiveFlowSession().setAttributes(attributes);
    }

    public FlowSession getActiveFlowSession() {
        if (executingFlowSessions.isEmpty()) {
            throw new IllegalStateException("No flow session is executing in this execution stack");
        }
        return (FlowSession)executingFlowSessions.peek();
    }

    public void push(FlowSession subFlowSession) {
        executingFlowSessions.push(subFlowSession);
        if (logger.isDebugEnabled()) {
            logger.debug("After push of new Flow Session '" + subFlowSession.getFlowId()
                    + "' - excutingFlowSessionsCount=" + executingFlowSessions.size() + ", sessionStack="
                    + executingFlowSessions);
        }
    }

    public FlowSession pop() {
        FlowSession s = (FlowSession)executingFlowSessions.pop();
        if (logger.isDebugEnabled()) {
            logger.debug("After pop of ended Flow Session '" + s.getFlowId() + "' - excutingFlowSessionsCount="
                    + executingFlowSessions.size() + ", sessionStack=" + executingFlowSessions);
        }
        return s;
    }

    public String toString() {
        return executingFlowSessions.isEmpty() ? "[Empty FlowSessionExecutionStack; no flows are active]"
                : new ToStringCreator(this).append("activeFlowId", getActiveFlowId()).append("currentStateId",
                        getCurrentStateId()).append("rootFlow", isRootFlow()).append("executingFlowSessions",
                        executingFlowSessions).toString();
    }

}