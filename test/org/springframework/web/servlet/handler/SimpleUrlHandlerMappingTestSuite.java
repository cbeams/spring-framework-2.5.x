package org.springframework.web.servlet.handler;

import junit.framework.TestCase;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.WebUtils;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class SimpleUrlHandlerMappingTestSuite extends TestCase {

	public void testHandlerBeanNotFound() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PARAM,
		                    "/org/springframework/web/servlet/handler/map1.xml");
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PREFIX_PARAM,
		                    "/org/springframework/web/servlet/handler/");
		XmlWebApplicationContext root = new XmlWebApplicationContext();
		root.initRootContext(sc);
		XmlWebApplicationContext wac = new XmlWebApplicationContext();
		try {
			wac.initNestedContext(sc, "map2err", root, null);
			fail("Should have thrown NoSuchBeanDefinitionException");
		}
		catch (FatalBeanException ex) {
			NoSuchBeanDefinitionException nestedEx = (NoSuchBeanDefinitionException) ex.getRootCause();
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
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PARAM,
		                    "/org/springframework/web/servlet/handler/map2.xml");
		XmlWebApplicationContext wac = new XmlWebApplicationContext();
		wac.initRootContext(sc);
		Object bean = wac.getBean("mainController");
		HandlerMapping hm = (HandlerMapping) wac.getBean(beanName);

		MockHttpServletRequest req = new MockHttpServletRequest(null, "GET", "/welcome.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/show.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/bookseats.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/original-welcome.html");
		req.setAttribute(WebUtils.INCLUDE_URI_REQUEST_ATTRIBUTE, "/welcome.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/original-show.html");
		req.setAttribute(WebUtils.INCLUDE_URI_REQUEST_ATTRIBUTE, "/show.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/original-bookseats.html");
		req.setAttribute(WebUtils.INCLUDE_URI_REQUEST_ATTRIBUTE, "/bookseats.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
	}

}
