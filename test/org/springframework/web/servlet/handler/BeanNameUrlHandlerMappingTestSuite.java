package org.springframework.web.servlet.handler;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author Rod Johnson
 */
public class BeanNameUrlHandlerMappingTestSuite extends TestCase {

	public static final String CONF = "/org/springframework/web/servlet/handler/map1.xml";
	
	private HandlerMapping hm;
	
	private WebApplicationContext wac;

	public void setUp() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PARAM, CONF);
		wac = new XmlWebApplicationContext();
		wac.setServletContext(sc);
		hm = new BeanNameUrlHandlerMapping();
		hm.setApplicationContext(wac);
	}

	public void testRequestsWithHandlers() throws Exception {
		Object bean = wac.getBean("godCtrl");

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
	
	public void testRequestsWithoutHandlers() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest(null, "GET", "/nonsense.html");
		Object h = hm.getHandler(req);
		assertTrue("Handler is null", h == null);
		
		req = new MockHttpServletRequest(null, "GET", "/foo/bar/baz.html");
		h = hm.getHandler(req);
		assertTrue("Handler is null", h == null);
	}

	public void testAsteriskMatches() throws ServletException {
		Object bean = wac.getBean("godCtrl");

		MockHttpServletRequest req = new MockHttpServletRequest(null, "GET", "/test.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/testarossa");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/tes");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec == null);
	}

}
