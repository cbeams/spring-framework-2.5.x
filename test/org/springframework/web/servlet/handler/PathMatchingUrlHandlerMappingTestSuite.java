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

package org.springframework.web.servlet.handler;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author Alef Arendsen
 */
public class PathMatchingUrlHandlerMappingTestSuite extends TestCase {

	public static final String CONF = "/org/springframework/web/servlet/handler/map3.xml";

	private HandlerMapping hm;

	private ConfigurableWebApplicationContext wac;

	public void setUp() throws Exception {
		MockServletContext sc = new MockServletContext("");
		wac = new XmlWebApplicationContext();
		wac.setServletContext(sc);
		wac.setConfigLocations(new String[] {CONF});
		wac.refresh();
		hm = (HandlerMapping) wac.getBean("urlMapping");
	}

	public void testRequestsWithHandlers() throws Exception {
		Object bean = wac.getBean("mainController");

		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/welcome.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/show.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/bookseats.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
	}

	public void testActualPathMatching() throws Exception {
		// there a couple of mappings defined with which we can test the
		// path matching, let's do that...

		Object bean = wac.getBean("mainController");
		Object defaultBean = wac.getBean("starController");

		// testing some normal behavior
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/pathmatchingTest.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is null", hec != null);
		assertTrue("Handler is correct bean", hec.getHandler() == bean);

		// no match, no forward slash included
		req = new MockHttpServletRequest("GET", "welcome.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);


		// testing some ????? behavior
		req = new MockHttpServletRequest("GET", "/pathmatchingAA.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		// testing some ????? behavior
		req = new MockHttpServletRequest("GET", "/pathmatchingA.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		// testing some ????? behavior
		req = new MockHttpServletRequest("GET", "/administrator/pathmatching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		// testing simple /**/ behavior
		req = new MockHttpServletRequest("GET", "/administrator/test/pathmatching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		// this should not match because of the administratorT
		req = new MockHttpServletRequest("GET", "/administratort/pathmatching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		// this should match because of *.jsp
		req = new MockHttpServletRequest("GET", "/bla.jsp");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		// this as well, because there's a **/ in there as well
		req = new MockHttpServletRequest("GET", "/testing/bla.jsp");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		// should match because because exact pattern is there
		req = new MockHttpServletRequest("GET", "/administrator/another/bla.xml");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		// should not match, because there's not .gif extension in there
		req = new MockHttpServletRequest("GET", "/administrator/another/bla.gif");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		// should match because there testlast* in there
		req = new MockHttpServletRequest("GET", "/administrator/test/testlastbit");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		// but this not, because it's testlast and not testla
		req = new MockHttpServletRequest("GET", "/administrator/test/testla");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/administrator/testing/longer/bla");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/administrator/testing/longer/test.jsp");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/administrator/testing/longer2/notmatching/notmatching");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/shortpattern/testing/toolong");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/XXpathXXmatching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/pathXXmatching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/XpathXXmatching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/XXpathmatching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/show12.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/show123.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/show1.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/reallyGood-test-is-this.jpeg");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/reallyGood-tst-is-this.jpeg");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/testing/test.jpeg");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/testing/test.jpg");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/anotherTest");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/stillAnotherTest");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		// there outofpattern*yeah in the pattern, so this should fail
		req = new MockHttpServletRequest("GET", "/outofpattern*ye");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/test't est/path'm atching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);

		req = new MockHttpServletRequest("GET", "/test%26t%20est/path%26m%20atching.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);
	}

	public void testDefaultMapping() throws Exception {
		Object bean = wac.getBean("starController");
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/goggog.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
	}

}
