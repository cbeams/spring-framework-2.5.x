/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.flow.support.AttributeSetterSupport;

public class Scope extends AttributeSetterSupport implements Serializable {

	private Map attributes = new HashMap();

	public boolean containsAttribute(String attributeName) {
		return this.attributes.containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return this.attributes.get(attributeName);
	}

	public Map getAttributeMap() {
		return Collections.unmodifiableMap(this.attributes);
	}

	public Object setAttribute(String attributeName, Object attributeValue) {
		return this.attributes.put(attributeName, attributeValue);
	}
	
	public Object removeAttribute(String attributeName) {
		return this.attributes.remove(attributeName);
	}
	
	public int size() {
		return this.attributes.size();
	}
}