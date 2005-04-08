/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.samples.phonebook.web.flow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.test.web.flow.AbstractFlowExecutionTests;
import org.springframework.web.flow.SimpleEvent;
import org.springframework.web.flow.ViewDescriptor;

public class SearchPersonFlowTests extends AbstractFlowExecutionTests {

	public SearchPersonFlowTests() {
		setDependencyCheck(false);
	}

	protected String flowId() {
		return "person.Search";
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/samples/phonebook/deploy/service-layer.xml",
				"classpath:org/springframework/samples/phonebook/deploy/web-layer.xml" };
	}

	public void testStartFlow() {
		startFlow();
		assertCurrentStateEquals("criteria.view");
	}
	
	public void testCriteriaView_Submit_Success() {
		startFlow();
		Map properties = new HashMap();
		properties.put("firstName", "Keith");
		properties.put("lastName", "Donald");
		ViewDescriptor view = signalEvent(new SimpleEvent(this, "submit", properties));
		assertCurrentStateEquals("results.view");
		asserts().assertCollectionAttributeSize(view, "persons", 1);
	}
	
	public void testCriteriaView_Submit_Error() {
		startFlow();
		ViewDescriptor view = signalEvent(new SimpleEvent(this, "submit", null));
		assertCurrentStateEquals("criteria.view");
	}

}