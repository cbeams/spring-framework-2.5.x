package org.springframework.web.flow.execution;

import org.springframework.web.flow.Flow;

public interface FlowExecutionListenerCriteria {
	public static final FlowExecutionListenerCriteria ALL_FLOWS = new FlowExecutionListenerCriteria() {
		public boolean shouldListenTo(Flow flow) {
			return true;
		}
	};

	public boolean shouldListenTo(Flow flow);
}
