package org.springframework.web.multipart.cos;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.util.WebUtils;

/**
 * INCOMPLETE: How to mock com.oreilly.servlet.MultipartRequest?
 * @author Juergen Hoeller
 * @since 08.10.2003
 */
public class CosMultipartResolverTests extends TestCase {

	public void testWithApplicationContext() throws MultipartException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.getServletContext().setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		wac.refresh();
		CosMultipartResolver resolver = new CosMultipartResolver();
		resolver.setMaximumFileSize(1000);
		resolver.setHeaderEncoding("enc");
		resolver.setApplicationContext(wac);
		assertEquals(1000, resolver.getMaximumFileSize());
		assertEquals("enc", resolver.getHeaderEncoding());
		assertTrue(resolver.getUploadTempDir().endsWith("mytemp"));

		MockHttpServletRequest originalRequest = new MockHttpServletRequest(null, null, null);
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		assertTrue(resolver.isMultipart(originalRequest));
	}

	public void testWithServletContext() throws ServletException, IOException {
		MockServletContext sc = new MockServletContext();
		sc.setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		CosMultipartResolver resolver = new CosMultipartResolver(sc);
		assertTrue(resolver.getUploadTempDir().endsWith("mytemp"));
	}

}
