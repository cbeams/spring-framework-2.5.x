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
package org.springframework.web.flow.config;

import org.springframework.web.flow.Flow;

/**
 * Builder interface used to build flows.
 * <p>
 * Implementations should encapsulate flow construction logic, either for a
 * specific kind of flow, for example, <code>a EditUsersMasterFlowBuilder</code>
 * built in java code, or a generic flow builder strategy, like the
 * XmlFlowBuilder, for building flows from a xml-definition.
 * <p>
 * Flow builders are executed by the FlowFactoryBean, which acts as an assembler
 * (director). This is the classic GoF Builder pattern.
 * 
 * @see org.springframework.web.flow.config.AbstractFlowBuilder
 * @see org.springframework.web.flow.config.XmlFlowBuilder
 * @see org.springframework.web.flow.config.FlowFactoryBean
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowBuilder {

	/**
	 * Initialize this builder and return a handle to the flow under construction.
	 * This handle is needed to avoid infinite loops in the build process.
	 * The returned flow object is still under construction and not yet ready for
	 * use. The only property that is guaranteed to be filled is the id of the flow.
	 */
	public Flow init() throws FlowBuilderException;
	
	/**
	 * Creates and adds all states to the flow built by this builder.
	 */
	public void buildStates() throws FlowBuilderException;

	/**
	 * Creates and/or links applicable flow execution listeners up to the flow
	 * built by this builder. This set of listeners will be treated as the
	 * default set associated with each execution created for the flow built by
	 * this builder.
	 */
	public void buildExecutionListeners() throws FlowBuilderException;

	/**
	 * Get the fully constructed and configured Flow object - called by the
	 * builder's assembler (director) after assembly. Note that this method
	 * will return the same Flow object as that returned from the
	 * <code>init()</code> method. However, when this method is called by the
	 * assembler, flow construction will have completed and the returned
	 * flow is ready for use. 
	 */
	public Flow getResult();

}