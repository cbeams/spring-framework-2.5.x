package org.springframework.web.servlet.handler;

import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class SimpleUrlHandlerMappingTestSuite extends TestCase {

	public static final String CONF = "/org/springframework/web/servlet/handler/map2.xml";
	
	private ApplicationContext ac;

	public SimpleUrlHandlerMappingTestSuite() throws IOException {
		ac = new ClassPathXmlApplicationContext(CONF);
	}

	public void testUrlMappingWithUrlMap() throws Exception {
		checkMappings((HandlerMapping) ac.getBean("urlMapping"));
	}

	public void testUrlMappingWithProps() throws Exception {
		checkMappings((HandlerMapping) ac.getBean("urlMappingWithProps"));
	}

	private void checkMappings(HandlerMapping hm) throws Exception {
		Object bean = ac.getBean("mainController");
		
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
