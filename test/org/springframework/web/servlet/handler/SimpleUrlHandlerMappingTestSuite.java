package org.springframework.web.servlet.handler;

import junit.framework.TestCase;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class SimpleUrlHandlerMappingTestSuite extends TestCase {

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
		req.setAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE, "/welcome.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/original-show.html");
		req.setAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE, "/show.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/original-bookseats.html");
		req.setAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE, "/bookseats.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
	}

}
