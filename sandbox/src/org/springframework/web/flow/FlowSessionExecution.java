/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * Subinterface of info that exposes mutable operations - designed for use by
 * the lifecycle listenner, which is a bit more privleged.
 * @author Keith Donald
 */
public interface FlowSessionExecution extends FlowSessionExecutionInfo, MutableAttributesAccessor {

}