/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public interface EventHandlerMethodNameResolver {
	public String getMethodName(String eventId);
}