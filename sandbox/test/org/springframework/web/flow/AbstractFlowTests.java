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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.SessionKeyUtils;

/**
 * Base class for flow integration tests; belongs in the spring-test.jar
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowTests extends AbstractTransactionalSpringContextTests {

	private Flow flow;

	protected FlowExecution flowExecution;

	protected void setFlow(Flow flow) {
		Assert.notNull(flow, "Flow is required for this test");
		this.flow = flow;
	}

	protected void assertCurrentStateEquals(String expectedCurrentStateId) {
		assertEquals("The current state '" + getCurrentStateId() + "' does not equal the expected state '"
				+ expectedCurrentStateId + "'", expectedCurrentStateId, getCurrentStateId());
	}

	protected String getCurrentStateId() {
		return flowExecution.getCurrentStateId();
	}

	protected FlowExecution getFlowSessionExecution() {
		return flowExecution;
	}

	protected void assertModelAttributePresent(Map attributeMap, String attributeName) {
		assertTrue("The model attribute '" + attributeName + "' is not present in model", attributeMap
				.containsKey(attributeName));
	}

	protected void assertModelAttributeInstanceOf(Map attributeMap, String attributeName, Class clazz) {
		assertModelAttributePresent(attributeMap, attributeName);
		Assert.isInstanceOf(clazz, attributeMap.get(attributeName));
	}

	protected void assertModelAttributeEquals(Map attributeMap, String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			assertModelAttributeInstanceOf(attributeMap, attributeName, attributeValue.getClass());
		}
		assertEquals("The model attribute '" + attributeName + "' must equal '" + attributeValue + "'", attributeValue,
				attributeMap.get(attributeName));
	}

	protected void assertModelCollectionAttributeSize(Map attributeMap, String attributeName, int size) {
		assertModelAttributeInstanceOf(attributeMap, attributeName, Collection.class);
		assertEquals("The model collection attribute '" + attributeName + "' must have " + size + " elements", size,
				((Collection)attributeMap.get(attributeName)).size());
	}

	protected void assertModelAttributePropertyEquals(Map attributeMap, String attributeName, String propertyName,
			Object propertyValue) {
		assertModelAttributePresent(attributeMap, attributeName);
		Object value = attributeMap.get(attributeName);
		Assert.isTrue(!BeanUtils.isSimpleProperty(value.getClass()), "Attribute value must be a bean");
		BeanWrapper wrapper = new BeanWrapperImpl(value);
		assertEquals(propertyValue, wrapper.getPropertyValue(propertyName));
	}

	protected String generateUniqueFlowSessionId(FlowExecutionStack stack) {
		return SessionKeyUtils.generateMD5SessionKey(String.valueOf(stack.hashCode()), true);
	}

	protected Flow getFlow() {
		return flow;
	}

	protected ModelAndView startFlow(HttpServletRequest request, HttpServletResponse response, Map input) {
		this.flowExecution = createFlowExecution(getFlow());
		return this.flowExecution.start(input, request, response);
	}

	protected FlowExecution createFlowExecution(Flow flow) {
		return new FlowExecutionStack(flow);
	}

	protected ModelAndView startFlow(Map input) {
		return startFlow(new MockHttpServletRequest(), new MockHttpServletResponse(), input);
	}

	protected ModelAndView executeEvent(String eventId, MockHttpServletRequest request, MockHttpServletResponse response) {
		return getFlowSessionExecution().signalEvent(eventId, getCurrentStateId(), request, response);
	}

}