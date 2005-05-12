package org.springframework.web.flow.execution;

import junit.framework.TestCase;

import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.SubflowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.ViewState;
import org.springframework.web.flow.StateTests.ExecutionCounterAction;
import org.springframework.web.flow.StateTests.InputOutputMapper;
import org.springframework.web.flow.action.AbstractAction;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;
import org.springframework.web.flow.config.FlowFactoryBean;
import org.springframework.web.flow.config.SimpleTransitionCriteriaCreator;
import org.springframework.web.flow.config.TransitionCriteriaCreator;

/**
 * General flow execution tests.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionTests extends TestCase {

	private static TransitionCriteriaCreator factory = new SimpleTransitionCriteriaCreator();

	public static TransitionCriteria on(String event) {
		return factory.create(event);
	}
	
	public void testFlowExecutionListener() {
		Flow subFlow = new Flow("mySubFlow");
		new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition(on("submit"), "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition(on("success"), "viewState"));
		new ViewState(flow, "viewState", "myView", new Transition(on("submit"), "subFlowState"));
		new SubflowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition(on("finish"), "finish"));
		new EndState(flow, "finish");

		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		MockFlowExecutionListener flowExecutionListener = new MockFlowExecutionListener();
		flowExecution.getListeners().add(flowExecutionListener);
		flowExecution.start(new Event(this));
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(2, flowExecutionListener.getTransitionCount());
		flowExecution.signalEvent(new Event(this, "submit"));
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(1, flowExecutionListener.getFlowNestingLevel());
		assertEquals(4, flowExecutionListener.getTransitionCount());
		flowExecution.signalEvent(new Event(this, "submit"));
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(6, flowExecutionListener.getTransitionCount());
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
		FlowExecution flowExecution = new FlowExecutionImpl(new FlowFactoryBean(builder).getFlow());
		ViewDescriptor vd = flowExecution.start(new Event(this, "start"));
		assertNotNull(vd);
		assertEquals("viewName", vd.getViewName());
		for (int i = 0; i < 10; i++) {
			vd = flowExecution.signalEvent(new Event(this, "submit"));
			assertNotNull(vd);
			assertEquals("viewName", vd.getViewName());
		}
		assertTrue(flowExecution.getContext().isActive());
		vd = flowExecution.signalEvent(new Event(this, "finish"));
		assertNull(vd);
		assertFalse(flowExecution.getContext().isActive());
	}

	public void testLoopInFlowWithSubFlow() {
		AbstractFlowBuilder childBuilder = new AbstractFlowBuilder() {
			protected String flowId() {
				return "childFlow";
			}

			public void buildStates() throws FlowBuilderException {
				addActionState("doOtherStuff", new AbstractAction() {
					private int executionCount = 0;

					protected Event doExecute(RequestContext context) throws Exception {
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
					protected Event doExecute(RequestContext context) throws Exception {
						return success();
					}
				}, on(success(), "startSubFlow"));
				addSubFlowState("startSubFlow", childFlow, new Transition[] { on(finish(), "startSubFlow"),
						on("stopTest", "stopTest") });
				addEndState("stopTest");
			}
		};
		Flow parentFlow = new FlowFactoryBean(parentBuilder).getFlow();

		FlowExecution flowExecution = new FlowExecutionImpl(parentFlow);
		flowExecution.start(new Event(this, "start"));
		assertFalse(flowExecution.getContext().isActive());
	}
}