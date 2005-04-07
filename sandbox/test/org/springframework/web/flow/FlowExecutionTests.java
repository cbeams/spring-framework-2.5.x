package org.springframework.web.flow;

import junit.framework.TestCase;

import org.springframework.mock.web.flow.MockFlowExecutionListener;
import org.springframework.web.flow.StateTests.ExecutionCounterAction;
import org.springframework.web.flow.StateTests.InputOutputMapper;
import org.springframework.web.flow.action.AbstractAction;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;
import org.springframework.web.flow.config.FlowFactoryBean;

/**
 * General flow execution tests.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionTests extends TestCase {

	public void testFlowExecutionListener() {
		Flow subFlow = new Flow("mySubFlow");
		new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition("submit", "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition("success", "viewState"));
		new ViewState(flow, "viewState", "myView", new Transition("submit", "subFlowState"));
		new SubFlowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition("finish", "finish"));
		new EndState(flow, "finish");
		
		FlowExecution flowExecution = flow.createExecution();
		MockFlowExecutionListener flowExecutionListener = new MockFlowExecutionListener();
		flowExecution.getListenerList().add(flowExecutionListener);
		flowExecution.start(new SimpleEvent(this, "start"));
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(2, flowExecutionListener.countStateTransitions());
		flowExecution.signalEvent(new SimpleEvent(this, "submit"));
		assertEquals(1, flowExecutionListener.getFlowNestingLevel());
		assertEquals(4, flowExecutionListener.countStateTransitions());
		flowExecution.signalEvent(new SimpleEvent(this, "submit"));
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(6, flowExecutionListener.countStateTransitions());
	}
	
	public void testLoopInFlow() {
		AbstractFlowBuilder builder = new AbstractFlowBuilder() {
			protected String flowId() {
				return "flow";
			}

			public void buildStates() throws FlowBuilderException {
				addViewState("viewState", "viewName", new Transition[] { on(submit(), "viewState"),
						on(finish(), "endState") });
				addEndState("endState");
			}
		};
		FlowExecution flowExecution = new FlowFactoryBean(builder).getFlow().createExecution();
		ViewDescriptor vd = flowExecution.start(new SimpleEvent(this, "start"));
		assertNotNull(vd);
		assertEquals("viewName", vd.getViewName());
		for (int i = 0; i < 10; i++) {
			vd = flowExecution.signalEvent(new SimpleEvent(this, "submit"));
			assertNotNull(vd);
			assertEquals("viewName", vd.getViewName());
		}
		assertTrue(flowExecution.isActive());
		vd = flowExecution.signalEvent(new SimpleEvent(this, "finish"));
		assertNull(vd);
		assertFalse(flowExecution.isActive());
	}

	public void testLoopInFlowWithSubFlow() {
		AbstractFlowBuilder childBuilder = new AbstractFlowBuilder() {
			protected String flowId() {
				return "childFlow";
			}

			public void buildStates() throws FlowBuilderException {
				addActionState("doOtherStuff", new AbstractAction() {
					private int executionCount = 0;

					protected Event doExecuteAction(RequestContext context) throws Exception {
						executionCount++;
						if (executionCount < 2) {
							return success();
						}
						return error();
					}
				}, new Transition[] { on(success(), finish()), on(error(), "stopTest") });
				addEndState(finish());
				addEndState("stopTest");
			}
		};
		final Flow childFlow = new FlowFactoryBean(childBuilder).getFlow();
		AbstractFlowBuilder parentBuilder = new AbstractFlowBuilder() {
			protected String flowId() {
				return "parentFlow";
			}

			public void buildStates() throws FlowBuilderException {
				addActionState("doStuff", new AbstractAction() {
					protected Event doExecuteAction(RequestContext context) throws Exception {
						return success();
					}
				}, on(success(), "startSubFlow"));
				addSubFlowState("startSubFlow", childFlow, new Transition[] { on(finish(), "startSubFlow"),
						on("stopTest", "stopTest") });
				addEndState("stopTest");
			}
		};
		Flow parentFlow = new FlowFactoryBean(parentBuilder).getFlow();

		FlowExecution flowExecution = parentFlow.createExecution();
		flowExecution.start(new SimpleEvent(this, "start"));
		assertFalse(flowExecution.isActive());
	}
}