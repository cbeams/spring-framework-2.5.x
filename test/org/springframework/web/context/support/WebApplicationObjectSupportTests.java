package org.springframework.web.context.support;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.util.WebUtils;

/**
 * @author Juergen Hoeller
 * @since 28.08.2003
 */
public class WebApplicationObjectSupportTests extends TestCase {

	public void testWebApplicationObjectSupport() {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		File tempDir = new File("");
		wac.getServletContext().setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, tempDir);
		wac.refresh();
		WebApplicationObjectSupport wao = new WebApplicationObjectSupport() {
		};
		wao.setApplicationContext(wac);
		assertEquals(wao.getServletContext(), wac.getServletContext());
		assertEquals(wao.getTempDir(), tempDir);
	}

	public void testWebApplicationObjectSupportWithWrongContext() {
		StaticApplicationContext ac = new StaticApplicationContext();
		WebApplicationObjectSupport wao = new WebApplicationObjectSupport() {
		};
		try {
			wao.setApplicationContext(ac);
			fail("Should have thrown ApplicationContextException");
		}
		catch (ApplicationContextException ex) {
			// expected
		}
	}

}
