/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;
import org.springframework.util.ToStringCreator;

/**
 * A user session instance of a flow.
 * 
 * @author Keith Donald
 */
public class FlowSession implements MutableAttributesAccessor, Serializable {
    private static final Log logger = LogFactory.getLog(FlowSession.class);

    private String flowId;

    private String currentStateId;

    private Map attributes = new HashMap();

    public FlowSession(String flowId, String startingStateId) {
        this.flowId = flowId;
        this.currentStateId = startingStateId;
    }

    public FlowSession(String flowId, String startingStateId, Map input) {
        this.flowId = flowId;
        this.currentStateId = startingStateId;
        if (input != null) {
            setAttributes(input);
        }
    }

    /**
     * @return Returns the id.
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * @return Returns the stateId.
     */
    public String getCurrentStateId() {
        return currentStateId;
    }

    public void setCurrentStateId(String stateId) {
        this.currentStateId = stateId;
    }

    /**
     * @return Returns the attributes.
     */
    public Map getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Object getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
        Object value = getAttribute(attributeName);
        if (requiredType != null && value != null) {
            Assert.isInstanceOf(requiredType, value);
        }
        return value;
    }

    public Object getRequiredAttribute(String key) throws IllegalStateException {
        Object value = getAttribute(key);
        if (value == null) {
            throw new IllegalStateException("Required attribute '" + key + "' is not present in flow scope for flow '"
                    + getFlowId() + "'; attributes currently in scope are = " + DefaultObjectStyler.call(attributes));

        }
        return value;
    }

    public Object getRequiredAttribute(String key, Class clazz) throws IllegalStateException {
        Object value = getRequiredAttribute(key);
        if (clazz != null) {
            Assert.isInstanceOf(clazz, value);
        }
        return value;
    }

    public boolean containsAttribute(String attributeName) {
        return attributes.containsKey(attributeName);
    }

    public Collection attributeNames() {
        return Collections.unmodifiableCollection(attributes.keySet());
    }

    public Collection attributeEntries() {
        return Collections.unmodifiableCollection(attributes.entrySet());
    }

    public Collection attributeValues() {
        return Collections.unmodifiableCollection(attributes.values());
    }

    public void setAttribute(String attributeName, Object attributeValue) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting flow '" + getFlowId() + "' attribute '" + attributeName + "' to '" + attributeValue
                    + "'");
        }
        this.attributes.put(attributeName, attributeValue);
    }

    public void setAttributes(Map attributes) {
        if (attributes == null) {
            return;
        }
        Iterator it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();
            Assert.isInstanceOf(String.class, e.getKey());
            setAttribute((String)e.getKey(), e.getValue());
        }
    }

    public String toString() {
        return new ToStringCreator(this).append("flowId", flowId).append("currentStateId", currentStateId).append(
                "attributesCount", (attributes != null ? attributes.size() : 0)).append("attributes", attributes)
                .toString();
    }
}