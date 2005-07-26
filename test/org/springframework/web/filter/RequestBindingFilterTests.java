package org.springframework.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.TestCase;

import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.RequestHolder;

/**
 * 
 * @author Rod Johnson
 * @since 1.3
 */
public class RequestBindingFilterTests extends TestCase {

	public void testHappyPath() throws Exception {
		testFilterInvocation(null);
	}
	
	public void testWithException() throws Exception {
		testFilterInvocation(new ServletException());
	}
		
	public void testFilterInvocation(final ServletException sex) throws Exception {
		
		final MockHttpServletRequest req = new MockHttpServletRequest();
		final MockHttpServletResponse resp = new MockHttpServletResponse();
		
		// Expect one invocation by the filter being tested
		class DummyFilterChain implements FilterChain {
			public int invocations = 0;
			public void doFilter(ServletRequest req, ServletResponse resp) throws IOException, ServletException {
				++invocations;
				if (invocations == 1) {
					assertSame(req, RequestHolder.currentRequest());
					if (sex != null) {
						throw sex;
					}
				}
				else {
					throw new IllegalStateException("Too many invocations");
				}
			}
		};
		
		DummyFilterChain fc = new DummyFilterChain();
		
		MockFilterConfig mfc = new MockFilterConfig(new MockServletContext(), "foo");
		
		RequestBindingFilter rbf = new RequestBindingFilter();
		rbf.setFilterConfig(mfc);
		
		try {
			rbf.doFilter(req, resp, fc);
			if (sex != null) {
				fail();
			}
		}
		catch (ServletException ex) {
			assertNotNull(sex);
		}
		
		try {
			RequestHolder.currentRequest();
			fail();
		}
		catch (IllegalStateException ex) {
			// Ok
		}

		assertEquals(1, fc.invocations);
	}

}
