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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.flow.config.FlowFactoryBean;
import org.springframework.web.flow.config.XmlFlowBuilder;
import org.springframework.web.flow.config.XmlFlowBuilderTests;

/**
 * Test case for FlowExecutionStack
 * 
 * @see org.springframework.web.flow.FlowExecutionStack
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionStackTests extends TestCase {

	private FlowLocator flowLocator;

	private FlowExecutionStack flowExecution;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow.xml", XmlFlowBuilderTests.class));
		builder.setFlowServiceLocator(new XmlFlowBuilderTests.TestFlowServiceLocator());
		final Flow flow = new FlowFactoryBean(builder).getFlow();
		flowLocator = new FlowLocator() {
			public Flow getFlow(Class flowDefinitionImplementationClass) throws ServiceLookupException {
				if (flow.getClass().equals(flowDefinitionImplementationClass)) {
					return flow;
				}
				throw new NoSuchFlowDefinitionException(flowDefinitionImplementationClass);
			}

			public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
				if (flow.getId().equals(flowDefinitionId)) {
					return flow;
				}
				throw new NoSuchFlowDefinitionException(flowDefinitionId);
			}

			public Flow getFlow(String flowDefinitionId, Class requiredFlowBuilderImplementationClass)
					throws ServiceLookupException {
				return getFlow(flowDefinitionId);
			}
		};
		flowExecution = (FlowExecutionStack)flow.createExecution();
	}

	protected void runFlowExecutionRehydrationTest() throws Exception {
		// serialize the flowExecution
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(flowExecution);
		oout.flush();

		// deserialize the flowExecution
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream oin = new ObjectInputStream(bin);
		FlowExecutionStack restoredFlowExecution = (FlowExecutionStack)oin.readObject();

		assertNotNull(restoredFlowExecution);

		// rehydrate the flow execution
		restoredFlowExecution.rehydrate(flowLocator, flowExecution.getListenerList().toArray());

		assertEquals(flowExecution.isActive(), restoredFlowExecution.isActive());
		if (flowExecution.isActive()) {
			assertTrue(entriesCollectionsAreEqual(flowExecution.getActiveFlowSession().flowScope().attributeEntries(),
					restoredFlowExecution.getActiveFlowSession().flowScope().attributeEntries()));
			assertEquals(flowExecution.getCurrentStateId(), restoredFlowExecution.getCurrentStateId());
			assertEquals(flowExecution.getActiveFlowId(), restoredFlowExecution.getActiveFlowId());
			assertSame(flowExecution.getRootFlow(), restoredFlowExecution.getRootFlow());
		}
		assertEquals(flowExecution.getId(), restoredFlowExecution.getId());
		assertEquals(flowExecution.getLastEventId(), restoredFlowExecution.getLastEventId());
		assertEquals(flowExecution.getLastEventTimestamp(), restoredFlowExecution.getLastEventTimestamp());
		assertEquals(flowExecution.getListenerList().size(), restoredFlowExecution.getListenerList().size());
	}

	public void testRehydrate() throws Exception {
		// setup some input data
		Map inputData = new HashMap(1);
		inputData.put("name", "value");
		// start the flow execution
		flowExecution.start(new LocalEvent(this, "start", inputData));
		runFlowExecutionRehydrationTest();
	}

	public void testRehydrateNotStarted() throws Exception {
		// don't start the flow execution
		runFlowExecutionRehydrationTest();
	}

	/**
	 * Helper to test if 2 collections of Map.Entry objects contain the same
	 * values.
	 */
	private boolean entriesCollectionsAreEqual(Collection collection1, Collection collection2) {
		if (collection1.size() != collection2.size()) {
			return false;
		}
		for (Iterator it1 = collection1.iterator(), it2 = collection2.iterator(); it1.hasNext() && it2.hasNext();) {
			Map.Entry entry1 = (Map.Entry)it1.next();
			Map.Entry entry2 = (Map.Entry)it2.next();
			if (!entry1.equals(entry2)) {
				return false;
			}
		}
		return true;
	}
}