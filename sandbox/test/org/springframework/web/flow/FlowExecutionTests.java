package org.springframework.web.flow;

import org.springframework.web.flow.action.AbstractAction;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;
import org.springframework.web.flow.config.FlowFactoryBean;

import junit.framework.TestCase;

/**
 * General flow execution tests.
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionTests extends TestCase {
	
	public void testInfrastructureAttributePrecense() {
		AbstractFlowBuilder builder=new AbstractFlowBuilder() {
			protected String flowId() {
				return "flow";
			}
			
			public void buildStates() throws FlowBuilderException {
				addViewState("view", "viewName", on(submit(), "end"));
				addEndState("end", "endViewName");
			}
		};
		FlowExecution flowExecution=new FlowFactoryBean(builder).getFlow().createExecution();
		ViewDescriptor vd=flowExecution.start(new SimpleEvent(this, "start"));
		assertNotNull(vd);
		assertEquals("viewName", vd.getViewName());
		assertEquals(flowExecution, vd.getModel().get(FlowConstants.FLOW_EXECUTION_ATTRIBUTE));
		assertEquals(flowExecution.getId(), vd.getModel().get(FlowConstants.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertEquals(flowExecution.getCurrentStateId(), vd.getModel().get(FlowConstants.CURRENT_STATE_ID_ATTRIBUTE));
		assertTrue(flowExecution.isActive());
		vd=flowExecution.signalEvent(new SimpleEvent(this, "submit"));
		assertNotNull(vd);
		assertEquals("endViewName", vd.getViewName());
		assertFalse(vd.getModel().containsKey(FlowConstants.FLOW_EXECUTION_ATTRIBUTE));
		assertFalse(vd.getModel().containsKey(FlowConstants.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertFalse(vd.getModel().containsKey(FlowConstants.CURRENT_STATE_ID_ATTRIBUTE));
		assertFalse(flowExecution.isActive());
	}
	
	public void testLoopInFlow() {
		AbstractFlowBuilder builder=new AbstractFlowBuilder() {
			protected String flowId() {
				return "flow";
			}
			
			public void buildStates() throws FlowBuilderException {
				addViewState(
						"viewState",
						"viewName",
						new Transition[] { on(submit(), "viewState"), on(finish(), "endState") });
				addEndState("endState");
			}
		};
		FlowExecution flowExecution=new FlowFactoryBean(builder).getFlow().createExecution();
		ViewDescriptor vd=flowExecution.start(new SimpleEvent(this, "start"));
		assertNotNull(vd);
		assertEquals("viewName", vd.getViewName());
		for (int i=0; i<10; i++) {
			vd=flowExecution.signalEvent(new SimpleEvent(this, "submit"));
			assertNotNull(vd);
			assertEquals("viewName", vd.getViewName());
		}
		assertTrue(flowExecution.isActive());
		vd=flowExecution.signalEvent(new SimpleEvent(this, "finish"));
		assertNull(vd);
		assertFalse(flowExecution.isActive());
	}
	
	public void testLoopInFlowWithSubFlow() {
		AbstractFlowBuilder childBuilder=new AbstractFlowBuilder() {
			protected String flowId() {
				return "childFlow";
			}
			public void buildStates() throws FlowBuilderException {
				addActionState(
						"doOtherStuff",
						new AbstractAction() {
							private int executionCount=0;
							
							protected Event doExecuteAction(RequestContext context)	throws Exception {
								executionCount++;
								if (executionCount<2) {
									return success();
								}
								return error();
							}
						},
						new Transition[] { on(success(), finish()), on(error(), "stopTest")});
				addEndState(finish());
				addEndState("stopTest");
			}
		};
		final Flow childFlow=new FlowFactoryBean(childBuilder).getFlow();
		AbstractFlowBuilder parentBuilder=new AbstractFlowBuilder() {
			protected String flowId() {
				return "parentFlow";
			}
			
			public void buildStates() throws FlowBuilderException {
				addActionState(
						"doStuff",
						new AbstractAction() {
							protected Event doExecuteAction(RequestContext context) throws Exception {
								return success();
							}
						},
						on(success(), "startSubFlow"));
				addSubFlowState(
						"startSubFlow",
						childFlow,
						new Transition[] { on(finish(), "startSubFlow"), on("stopTest", "stopTest") });
				addEndState("stopTest");
			}
		};
		Flow parentFlow=new FlowFactoryBean(parentBuilder).getFlow();
		
		FlowExecution flowExecution=parentFlow.createExecution();
		flowExecution.start(new SimpleEvent(this, "start"));
		assertFalse(flowExecution.isActive());
	}
	
	
}
