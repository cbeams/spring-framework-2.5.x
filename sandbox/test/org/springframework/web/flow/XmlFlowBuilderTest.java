package org.springframework.web.flow;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.flow.config.FlowFactoryBean;
import org.springframework.web.flow.config.FlowServiceLocator;
import org.springframework.web.flow.config.FlowServiceLookupException;
import org.springframework.web.flow.config.NoSuchActionException;
import org.springframework.web.flow.config.NoSuchFlowAttributesMapperException;
import org.springframework.web.flow.config.NoSuchFlowDefinitionException;
import org.springframework.web.flow.config.XmlFlowBuilder;
import org.springframework.web.flow.support.FlowExecutionListenerAdapter;

import junit.framework.TestCase;

/**
 * Test case for XML flow builder.
 * 
 * @see org.springframework.web.flow.config.XmlFlowBuilder
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilderTest extends TestCase {
	
	private Flow flow;
	
	protected void setUp() throws Exception {
		XmlFlowBuilder builder=new XmlFlowBuilder(new ClassPathResource("testFlow.xml", XmlFlowBuilderTest.class));
		
		builder.setFlowServiceLocator(new FlowServiceLocator() {
			public Action getAction(Class actionImplementationClass) throws FlowServiceLookupException {
				throw new UnsupportedOperationException();
			}
			
			public Action getAction(String actionId) throws FlowServiceLookupException {
				if ("action1".equals(actionId) || "action2".equals(actionId)) {
					return new Action() {
						public ActionResult execute(HttpServletRequest request,	HttpServletResponse response,
								MutableAttributesAccessor model) throws RuntimeException {
							return new ActionResult("testOk");
						}
					};
				}
				throw new NoSuchActionException(actionId);
			}
			
			public Flow getFlow(Class flowDefinitionImplementationClass) throws FlowServiceLookupException {
				throw new UnsupportedOperationException();
			}
			
			public Flow getFlow(String flowDefinitionId) throws FlowServiceLookupException {
				if ("subFlow1".equals(flowDefinitionId) || "subFlow2".equals(flowDefinitionId)) {
					return new Flow(flowDefinitionId);
				}
				throw new NoSuchFlowDefinitionException(flowDefinitionId);
			}
			
			public Flow getFlow(String flowDefinitionId, Class requiredFlowBuilderImplementationClass)
					throws FlowServiceLookupException {
				throw new UnsupportedOperationException();
			}
			
			public FlowAttributesMapper getFlowAttributesMapper(Class flowAttributesMapperImplementationClass)
					throws FlowServiceLookupException {
				throw new UnsupportedOperationException();
			}
			
			public FlowAttributesMapper getFlowAttributesMapper(String flowAttributesMapperId)
					throws FlowServiceLookupException {
				if ("attribMapper1".equals(flowAttributesMapperId)) {
					return new FlowAttributesMapper() {
						public Map createSpawnedSubFlowAttributesMap(AttributesAccessor parentFlowModel) {
							return new HashMap();
						}
	
						public void mapToResumingParentFlow(AttributesAccessor endingSubFlowModel,
								MutableAttributesAccessor resumingParentFlowModel) {
						}
					};
				}
				throw new NoSuchFlowAttributesMapperException(flowAttributesMapperId);
			}
		});
		builder.setFlowExecutionListener(new FlowExecutionListenerAdapter() {});
		
		flow=new FlowFactoryBean(builder).getFlow();
	}
	
	public void testBuildResult() {
		assertNotNull(flow);
		assertEquals("testFlow", flow.getId());
		assertEquals("actionState1", flow.getStartState().getId());
		assertEquals(9, flow.getStateIds().length);
		
		ActionState actionState1=(ActionState)flow.getState("actionState1");
		assertNotNull(actionState1);
		assertEquals(2, actionState1.getActions().length);
		assertEquals(2, actionState1.getTransitions().length);
		assertNotNull(actionState1.getTransition("event1"));
		assertEquals("viewState1", actionState1.getTransition("event1").getTargetStateId());
		assertNotNull(actionState1.getTransition("event2"));
		assertEquals("viewState2", actionState1.getTransition("event2").getTargetStateId());
		
		ViewState viewState1=(ViewState)flow.getState("viewState1");
		assertNotNull(viewState1);
		assertFalse(viewState1.isMarker());
		assertEquals("view1", viewState1.getViewName());
		assertEquals(1, viewState1.getTransitions().length);
		assertNotNull(viewState1.getTransition("event1"));
		assertEquals("subFlowState1", viewState1.getTransition("event1").getTargetStateId());

		ViewState viewState2=(ViewState)flow.getState("viewState2");
		assertNotNull(viewState2);
		assertTrue(viewState2.isMarker());
		assertNull(viewState2.getViewName());
		assertEquals(1, viewState2.getTransitions().length);
		assertNotNull(viewState2.getTransition("event2"));
		assertEquals("subFlowState2", viewState2.getTransition("event2").getTargetStateId());
		
		ViewState viewState3=(ViewState)flow.getState("viewState3");
		assertNotNull(viewState3);
		assertFalse(viewState3.isMarker());
		assertEquals("viewState3", viewState3.getViewName());
		assertEquals(1, viewState3.getTransitions().length);
		assertNotNull(viewState3.getTransition("event3"));
		assertEquals("endState3", viewState3.getTransition("event3").getTargetStateId());
		
		SubFlowState subFlowState1=(SubFlowState)flow.getState("subFlowState1");
		assertNotNull(subFlowState1);
		assertNotNull(subFlowState1.getSubFlow());
		assertEquals("subFlow1", subFlowState1.getSubFlow().getId());
		assertNotNull(subFlowState1.getFlowAttributesMapper());
		assertEquals(1, subFlowState1.getTransitions().length);
		assertNotNull(subFlowState1.getTransition("event1"));
		assertEquals("endState1", subFlowState1.getTransition("event1").getTargetStateId());
		
		SubFlowState subFlowState2=(SubFlowState)flow.getState("subFlowState2");
		assertNotNull(subFlowState2);
		assertNotNull(subFlowState2.getSubFlow());
		assertEquals("subFlow2", subFlowState2.getSubFlow().getId());
		assertNull(subFlowState2.getFlowAttributesMapper());
		assertEquals(1, subFlowState2.getTransitions().length);
		assertNotNull(subFlowState2.getTransition("event2"));
		assertEquals("endState2", subFlowState2.getTransition("event2").getTargetStateId());
		
		EndState endState1=(EndState)flow.getState("endState1");
		assertNotNull(endState1);
		assertFalse(endState1.isMarker());
		assertEquals("endView1", endState1.getViewName());
		
		EndState endState2=(EndState)flow.getState("endState2");
		assertNotNull(endState2);
		assertTrue(endState2.isMarker());
		assertNull(endState2.getViewName());
		
		EndState endState3=(EndState)flow.getState("endState3");
		assertNotNull(endState3);
		assertFalse(endState3.isMarker());
		assertEquals("endState3", endState3.getViewName());
	}

}
