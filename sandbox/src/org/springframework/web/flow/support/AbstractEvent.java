/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import org.springframework.web.flow.Event;

public abstract class AbstractEvent implements Event {

	public boolean containsAttribute(String attributeName) {
		return getParameters().containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return getParameter(attributeName);
	}

}
