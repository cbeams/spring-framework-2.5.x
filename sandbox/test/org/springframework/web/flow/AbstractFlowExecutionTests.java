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
package org.springframework.web.flow;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.util.Assert;
import org.springframework.web.flow.config.FlowBuilder;
import org.springframework.web.flow.config.FlowFactoryBean;
import org.springframework.web.flow.support.LocalEvent;
import org.springframework.web.servlet.ModelAndView;

/**
 * Base class for tests that verify a flow executes as expected; that is, it
 * responds to all supported events correctly, transitioning to the correct
 * states.
 * <p>
 * Belongs in the spring-test.jar.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractFlowExecutionTests extends AbstractTransactionalSpringContextTests {

	/**
	 * The flow whose execution is being tested.
	 */
	private Flow flow;

	/**
	 * The flow execution running the flow when the test is active.
	 */
	private FlowExecution flowExecution;

	/**
	 * The flow service locator; providing access to lookup and retrieve
	 * configured flows. Used to resolve the Flow to be tested by
	 * <code>id</code>.
	 */
	private FlowLocator flowLocator;

	/**
	 * Returns the flow locator used to resolve the Flow to be tested by
	 * <code>id</code>.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator.
	 * @param flowLocator the locator
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Get the singleton flow definition whose execution is being tested.
	 * @return the singleton flow definition
	 * @throws NoSuchFlowDefinitionExeception if the flow identified by flowId()
	 *         could not be resolved (if this.flow was null)
	 */
	protected Flow getFlow() throws NoSuchFlowDefinitionException {
		if (this.flow == null) {
			setFlow(getFlowLocator().getFlow(flowId(), flowBuilderClass()));
		}
		return flow;
	}

	/**
	 * Set the flow definition whose execution is being tested.
	 * @param flow the singleton flow definition
	 */
	protected void setFlow(Flow flow) {
		Assert.notNull(flow, "The flow definition whose execution to test is required");
		this.flow = flow;
	}

	/**
	 * Subclasses should override to return the <code>flowId</code> whose
	 * execution should be tested.
	 * @return The flow id, whose execution is to be tested.
	 */
	protected abstract String flowId();

	/**
	 * Subclasses may override to return the FlowBuilder implementation that is
	 * expected to build the flow whose execution is to be tested.
	 * @return the flow builder implementation (optional)
	 */
	protected Class flowBuilderClass() {
		return null;
	}

	/**
	 * Set the flow definition to be tested to the Flow built by the specified
	 * builder.
	 * @param flowBuilder The flow builder.
	 */
	protected void setFlowBuilder(FlowBuilder flowBuilder) {
		setFlow(new FlowFactoryBean(flowBuilder).getFlow());
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * Uses a mock http request and response object.
	 * @param input any input attribtes to pass to the flow execution
	 * @return the model and view returned as a result of starting the flow
	 *         (returned when the first view state is entered)
	 */
	protected ViewDescriptor startFlow(Map input) {
		return startFlow(new LocalEvent("start", input));
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @param request the http request, typically a mock http request
	 * @param response the http response, typically a mock http response
	 * @param input any input attribtes to pass to the flow execution
	 * @return the model and view returned as a result of starting the flow
	 *         (returned when the first view state is entered)
	 */
	protected ViewDescriptor startFlow(Event event) {
		this.flowExecution = getFlow().createExecution();
		setupFlowExecution(flowExecution);
		return this.flowExecution.start(event);
	}

	/**
	 * Hook method where you can do additional setup of a flow execution before
	 * it is started, like register an execution listener.
	 * @param flowExecution the flow execution
	 */
	protected void setupFlowExecution(FlowExecution flowExecution) {
	}

	/**
	 * Signal an occurence of an event in the current state of the flow
	 * execution being tested.
	 * <p>
	 * Note: signaling an event will cause state transitions to occur in a chain
	 * UNTIL control is returned to the caller. Control will be returned once a
	 * view state is entered or an end state is entered and the flow terminates.
	 * Action states are executed without returning control, as their result
	 * always triggers another state transition, executed internally. Action
	 * states can also be executed in a chain like fashion (e.g. action state 1
	 * (result), action state 2 (result), action state 3 (result), view state
	 * <control returns so view can be rendered>).
	 * <p>
	 * If you wish to verify expected behavior on each state transition (and not
	 * just when the view state triggers return of control back to the client),
	 * you have a few options:
	 * <p>
	 * First, you can always write a standalone unit test for the
	 * <code>Action</code> implementation. There you can verify that the
	 * action executes its core logic and responds to any exceptions it must
	 * handle. When you do this, you may mock or stub out services the Action
	 * implementation needs that are expensive to initialize. You can also
	 * verify there that the action puts everything in the model or the request
	 * it was supposed to (to meet its contract with the view it is prepping for
	 * display, if it's a view setup action).
	 * <p>
	 * Second, you can attach a FlowExecutionListener to the ongoing flow
	 * execution at any time within your test code, which receives callbacks on
	 * each state transition (among other points). To add a listener, call
	 * <code>getFlowExecution().getListenerList().add(myListener)</code>,
	 * where myListener is a class that implements the FlowExecutionListener
	 * interface (It is recommended you extend
	 * {@link org.springframework.web.flow.support.FlowExecutionListenerAdapter}
	 * and only override what you need.
	 * 
	 * @param eventId The event to signal
	 * @param request The request
	 * @param response The response
	 * @return the model and view, returned once control is returned to the
	 *         client (occurs when the flow enters a view state, or an end
	 *         state)
	 */
	protected ViewDescriptor signalEvent(Event event) {
		return getFlowExecution().signalEvent(event);
	}

	/**
	 * Returns the ongoing flow execution for this test.
	 * @return the flow execution
	 * @throws IllegalStateException the execution has not been started
	 */
	protected FlowExecution getFlowExecution() {
		if (flowExecution == null) {
			throw new IllegalStateException("The flow execution has not been started; call startFlow first");
		}
		return flowExecution;
	}

	/**
	 * Assert that the active flow session is for the flow with the provided id.
	 * @param expectedActiveFlowId The flow id that should have a session active
	 *        in the tested flow execution.
	 */
	protected void assertActiveFlowEquals(String expectedActiveFlowId) {
		assertEquals("The active flow id '" + getActiveFlowId() + "' does not equal the expected active flow '"
				+ expectedActiveFlowId + "'", expectedActiveFlowId, getActiveFlowId());
	}

	/**
	 * Assert that the current state of the flow execution equals the provided
	 * state id.
	 * @param expectedCurrentStateId The expected current state.
	 */
	protected void assertCurrentStateEquals(String expectedCurrentStateId) {
		assertEquals("The current state '" + getCurrentStateId() + "' does not equal the expected state '"
				+ expectedCurrentStateId + "'", expectedCurrentStateId, getCurrentStateId());
	}

	/**
	 * Assert that the last supported event that occured in the flow execution
	 * equals the provided event.
	 * @param expectedLastEventId the expected event.
	 */
	protected void assertEventEquals(String expectedEventId) {
		assertEquals("The last event '" + getEventId() + "' does not equal the expected event '"
				+ expectedEventId + "'", expectedEventId, getEventId());
	}

	/**
	 * Returns the active flow in the flow execution being tested; specifically,
	 * a flow is active if it has a session active in the flow execution.
	 * @return The active flow id.
	 */
	protected String getActiveFlowId() {
		return flowExecution.getActiveFlowId();
	}

	/**
	 * Returns the current state of the flow execution being tested.
	 */
	protected String getCurrentStateId() {
		return flowExecution.getCurrentStateId();
	}

	/**
	 * Returns the last supported event that occured in the flow execution being
	 * tested.
	 * @return the last event id
	 */
	protected String getEventId() {
		return flowExecution.getEventId();
	}

	/**
	 * Assert that an attribute with specified name is present in given model.
	 */
	protected void assertModelAttributePresent(Map model, String attributeName) {
		assertTrue("The model attribute '" + attributeName + "' is not present in model", model
				.containsKey(attributeName));
	}

	/**
	 * Assert that an attribute exists in the model map of the specified type.
	 * @param model the model map
	 * @param attributeName the attribute name
	 * @param clazz the required type
	 */
	protected void assertModelAttributeInstanceOf(Map model, String attributeName, Class clazz) {
		assertModelAttributePresent(model, attributeName);
		Assert.isInstanceOf(clazz, model.get(attributeName));
	}

	/**
	 * Assert that an attribute exists in the model map of the specified value.
	 * @param model the model map
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	protected void assertModelAttributeEquals(Map model, String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			assertModelAttributeInstanceOf(model, attributeName, attributeValue.getClass());
		}
		assertEquals("The model attribute '" + attributeName + "' must equal '" + attributeValue + "'", attributeValue,
				model.get(attributeName));
	}

	/**
	 * Assert that a collection exists in the model map under the provided
	 * attribute name, with the specified size.
	 * @param model the model map
	 * @param attributeName the attribute name
	 * @param size the expected collection size
	 */
	protected void assertModelCollectionAttributeSize(Map model, String attributeName, int size) {
		assertModelAttributeInstanceOf(model, attributeName, Collection.class);
		assertEquals("The model collection attribute '" + attributeName + "' must have " + size + " elements", size,
				((Collection)model.get(attributeName)).size());
	}

	/**
	 * Assert that a bean property attribute in the model map has a property
	 * with the provided property value.
	 * @param model the model map
	 * @param attributeName the attribute name (of a javabean)
	 * @param propertyName the bean property name
	 * @param propertyValue the expected property value
	 */
	protected void assertModelAttributePropertyEquals(Map model, String attributeName, String propertyName,
			Object propertyValue) {
		assertModelAttributePresent(model, attributeName);
		Object value = model.get(attributeName);
		Assert.isTrue(!BeanUtils.isSimpleProperty(value.getClass()), "Attribute value must be a bean");
		BeanWrapper wrapper = new BeanWrapperImpl(value);
		assertEquals(propertyValue, wrapper.getPropertyValue(propertyName));
	}
}