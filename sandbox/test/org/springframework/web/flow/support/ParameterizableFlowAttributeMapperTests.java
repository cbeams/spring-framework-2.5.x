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
package org.springframework.web.flow.support;

import java.util.Collections;
import java.util.Map;

import org.springframework.mock.web.flow.MockRequestContext;

import junit.framework.TestCase;

/**
 * @author Erwin Vervaet
 */
public class ParameterizableFlowAttributeMapperTests extends TestCase {

	public void testSimpleMapping() {
		ParameterizableFlowAttributeMapper mapper = new ParameterizableFlowAttributeMapper();
		mapper.setInputMappings(Collections.singleton("someAttribute"));
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("someAttribute", "someValue");
		// FIXME this currently throws an EvaluationException 
		Map input = mapper.createSubflowInput(context);
		assertEquals(1, input.size());
		assertEquals("someValue", input.get("someAttribute"));
	}

}
