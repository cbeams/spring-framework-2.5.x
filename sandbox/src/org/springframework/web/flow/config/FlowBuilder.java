/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import org.springframework.web.flow.Flow;

/**
 * Builder interface used to build flows.
 * 
 * <p>Flow builders are <i>executed</i> by the FlowFactoryBean.
 * 
 * @see org.springframework.web.flow.config.FlowFactoryBean
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowBuilder {
	
	/**
	 * Initialize this builder; typically constructs the initial Flow definition.
	 */
	public void init() throws FlowBuilderException;
	
	/**
	 * Creates and adds all states for the flow built by this builder.
	 */
	public void buildStates() throws FlowBuilderException;
	
	/**
	 * Get the fully constructed and configured Flow object - called by the
	 * builder's assembler (director) after assembly.
	 */
	public Flow getResult();

}