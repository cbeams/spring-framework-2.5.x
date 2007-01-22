/*
 * Copyright 2002-2007 the original author or authors.
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

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.WebUtils;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class SimpleUrlHandlerMappingTests extends TestCase {

	public void testHandlerBeanNotFound() throws Exception {
		MockServletContext sc = new MockServletContext("");
		XmlWebApplicationContext root = new XmlWebApplicationContext();
		root.setServletContext(sc);
		root.setConfigLocations(new String[] {"/org/springframework/web/servlet/handler/map1.xml"});
		root.refresh();
		XmlWebApplicationContext wac = new XmlWebApplicationContext();
		wac.setParent(root);
		wac.setServletContext(sc);
		wac.setNamespace("map2err");
		wac.setConfigLocations(new String[] {"/org/springframework/web/servlet/handler/map2err.xml"});
		try {
			wac.refresh();
			fail("Should have thrown NoSuchBeanDefinitionException");
		}
		catch (FatalBeanException ex) {
			NoSuchBeanDefinitionException nestedEx = (NoSuchBeanDefinitionException) ex.getCause();
			assertEquals("mainControlle", nestedEx.getBeanName());
		}
	}

	public void testUrlMappingWithUrlMap() throws Exception {
		checkMappings("urlMapping");
	}

	public void testUrlMappingWithProps() throws Exception {
		checkMappings("urlMappingWithProps");
	}

	private void checkMappings(String beanName) throws Exception {
		MockServletContext sc = new MockServletContext("");
		XmlWebApplicationContext wac = new XmlWebApplicationContext();
		wac.setServletContext(sc);
		wac.setConfigLocations(new String[] {"/org/springframework/web/servlet/handler/map2.xml"});
		wac.refresh();
		Object bean = wac.getBean("mainController");
		Object otherBean = wac.getBean("otherController");
		Object defaultBean = wac.getBean("starController");
		HandlerMapping hm = (HandlerMapping) wac.getBean(beanName);

		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/welcome.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
		assertEquals("/welcome.html", req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));

		req = new MockHttpServletRequest("GET", "/welcome.x");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == otherBean);
		assertEquals("welcome.x", req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));

		req = new MockHttpServletRequest("GET", "/");
		req.setServletPath("/welcome.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/welcome.html");
		req.setContextPath("/app");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/show.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/bookseats.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/original-welcome.html");
		req.setAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE, "/welcome.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/original-show.html");
		req.setAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE, "/show.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/original-bookseats.html");
		req.setAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE, "/bookseats.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest("GET", "/");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
		assertEquals("/", req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));

		req = new MockHttpServletRequest("GET", "/somePath");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == defaultBean);
		assertEquals("/somePath", req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
	}

}
