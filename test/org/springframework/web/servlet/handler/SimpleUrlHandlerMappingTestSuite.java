package org.springframework.web.servlet.handler;

import junit.framework.TestCase;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class SimpleUrlHandlerMappingTestSuite extends TestCase {

	public static final String CONF = "/org/springframework/web/servlet/handler/map2.xml";
	
	private WebApplicationContext wac;

	public void setUp() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PARAM, CONF);
		wac = new XmlWebApplicationContext();
		wac.setServletContext(sc);
	}

	public void testUrlMappingWithUrlMap() throws Exception {
		checkMappings((HandlerMapping) wac.getBean("urlMapping"));
	}

	public void testUrlMappingWithProps() throws Exception {
		checkMappings((HandlerMapping) wac.getBean("urlMappingWithProps"));
	}

	private void checkMappings(HandlerMapping hm) throws Exception {
		Object bean = wac.getBean("mainController");
		
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
