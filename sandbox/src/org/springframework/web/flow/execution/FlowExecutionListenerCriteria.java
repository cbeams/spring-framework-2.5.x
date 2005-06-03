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
 * Strategy interface that determines if a set of listeners should be subscribed to
 * the lifecycle of executions of specific flow definition.
 * @author Keith Donald
 */
public interface FlowExecutionListenerCriteria {
	
	/**
	 * Is this flow eligible for listening?
	 * @param flow the flow
	 * @return true if yes, false if no.
	 */
	public boolean matches(Flow flow);
	
	/**
	 * Static factory for producing common flow execution listener criteria.
	 * @author Keith Donald
	 */
	public static class Factory {
		private static final FlowExecutionListenerCriteria ALL_FLOWS = new FlowExecutionListenerCriteria() {
			public boolean matches(Flow flow) {
				return true;
			}
		};

		/**
		 * A wild card criteria that matches all flows.
		 * @return all flows
		 */
		public static FlowExecutionListenerCriteria allFlows() {
			return ALL_FLOWS;
		}
		
		/**
		 * A criteria that just matches a flow with the specified id.
		 * @param flowId the flow id
		 * @return the criteria
		 */
		public static FlowExecutionListenerCriteria flow(String flowId) {
			return new FlowIdFlowExecutionListenerCriteria(flowId);
		}
		
		public static class FlowIdFlowExecutionListenerCriteria implements FlowExecutionListenerCriteria {
			private String flowId;

			public FlowIdFlowExecutionListenerCriteria(String flowId) {
				this.flowId = flowId;
			}
			
			public boolean matches(Flow flow) {
				return flowId.equals(flow.getId());
			}
		
			public boolean equals(Object o) {
				if (!(o instanceof FlowIdFlowExecutionListenerCriteria)) {
					return false;
				}
				FlowIdFlowExecutionListenerCriteria c = (FlowIdFlowExecutionListenerCriteria)o;
				return flowId.equals(c.flowId);
			}
			
			public int hashCode() {
				return flowId.hashCode();
			}
			
			public String toString() {
				return flowId;
			}
		}
	}
}