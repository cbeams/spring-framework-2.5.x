
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

package org.springframework.web.servlet.mvc;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rod Johnson
 * @since March 2, 2003
 */
public class ParameterizableViewControllerTestSuite extends TestCase {

	public void testPropertyNotSet() throws Exception {
		ParameterizableViewController pvc = new ParameterizableViewController();
		try {
			pvc.initApplicationContext();
			fail("should require viewName property to be set");
		}
		catch (IllegalArgumentException ex){
			// ok
		}
	}
	
	public void testModelIsCorrect() throws Exception {
		String viewName = "viewName";
		ParameterizableViewController pvc = new ParameterizableViewController();
		pvc.setViewName(viewName);
		pvc.initApplicationContext();
		// We don't care about the params
		ModelAndView mv = pvc.handleRequest(new MockHttpServletRequest("GET", "foo.html"), null);
		assertTrue("model has no data", mv.getModel().size() == 0);
		assertTrue("model has correct viewname", mv.getViewName().equals(viewName));
		
		assertTrue("getViewName matches", pvc.getViewName().equals(viewName));
	}

}
