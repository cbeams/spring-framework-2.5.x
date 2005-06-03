package org.springframework.web.flow.execution;

import org.springframework.web.flow.Flow;

public interface FlowExecutionListenerCriteria {
	public boolean shouldListenTo(Flow flow);
	
	public static class Factory {
		private static final FlowExecutionListenerCriteria ALL_FLOWS = new FlowExecutionListenerCriteria() {
			public boolean shouldListenTo(Flow flow) {
				return true;
			}
		};

		public static FlowExecutionListenerCriteria allFlows() {
			return ALL_FLOWS;
		}
		
		public static FlowExecutionListenerCriteria flow(String flowId) {
			return new FlowIdFlowExecutionListenerCriteria(flowId);
		}
		
		public static class FlowIdFlowExecutionListenerCriteria implements FlowExecutionListenerCriteria {
			private String flowId;

			public FlowIdFlowExecutionListenerCriteria(String flowId) {
				this.flowId = flowId;
			}
			
			public boolean shouldListenTo(Flow flow) {
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