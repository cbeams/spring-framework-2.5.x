/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import org.springframework.web.flow.Flow;

/**
 * Factory method that encapsulates the creation of a <code>Flow</code>
 * instance. The instance created may be Flow (in the default case) or a
 * specific specialization.
 * 
 * This interface is useful when you require specific <code>Flow</code>
 * specializations that are shared between different <code>FlowBuilder</code>
 * implementations.
 * @author Keith Donald
 */
public interface FlowCreator {

	/**
	 * Create the <code>Flow</code> instance with the specified id.
	 * @param id The flow identifier
	 * @return The <code>Flow</code> (or a custom specialization of Flow)
	 */
	public Flow createFlow(String id);
}
