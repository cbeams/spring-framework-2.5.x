package org.springframework.web.servlet.handler;

import junit.framework.TestCase;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

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
		root.setServletContext(sc);
		XmlWebApplicationContext wac = new XmlWebApplicationContext(root, "map2err");
		try {
			wac.setServletContext(sc);
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
		WebApplicationContext wac = new XmlWebApplicationContext();
		wac.setServletContext(sc);
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
	}

}
