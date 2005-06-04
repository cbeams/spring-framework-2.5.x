/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.execution;

import org.springframework.web.flow.Flow;

/**
 * Strategy interface that determines if a flow execution listener
 * should be subscribed to the lifecycle of flow executions of specific
 * flow definition.
 * <p>
 * This selection strategy is used by the flow execution manager.
 * 
 * @see org.springframework.web.flow.execution.FlowExecution
 * @see org.springframework.web.flow.execution.FlowExecutionListener
 * @see org.springframework.web.flow.execution.FlowExecutionManager
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionListenerCriteria {
	
	/**
	 * Is this flow eligible for listening?
	 * @param flow the flow
	 * @return true if yes, false if no
	 */
	public boolean matches(Flow flow);
}