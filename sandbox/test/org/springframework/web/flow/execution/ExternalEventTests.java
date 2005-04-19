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
package org.springframework.web.flow.execution;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;

import junit.framework.TestCase;

/**
 * @author Erwin Vervaet
 */
public class ExternalEventTests extends TestCase {

	public void testSearchForParameter() {
		MockHttpServletRequest request=new MockHttpServletRequest();
		request.addParameter("_flowExecutionId", "D15ADADC-B632-01C4-93B4-672B0BE2D2C9");
		request.addParameter("page", "1");
		request.addParameter("_eventId_numbers.x", "12");
		request.addParameter("_eventId_numbers.y", "0");
		ExternalEvent event=new HttpServletRequestEvent(request, new MockHttpServletResponse());
		assertEquals("numbers", event.getId());
	}

}
