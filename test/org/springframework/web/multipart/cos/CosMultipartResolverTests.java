/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.multipart.cos;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
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
		resolver.setMaxUploadSize(1000);
		resolver.setDefaultEncoding("enc");
		resolver.setServletContext(wac.getServletContext());
		assertEquals(1000, resolver.getMaxUploadSize());
		assertEquals("enc", resolver.getDefaultEncoding());
		assertEquals(new File("mytemp"), resolver.getUploadTempDir());

		MockHttpServletRequest originalRequest = new MockHttpServletRequest();
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		assertTrue(resolver.isMultipart(originalRequest));
	}

	public void testWithServletContext() throws ServletException, IOException {
		MockServletContext sc = new MockServletContext();
		sc.setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		CosMultipartResolver resolver = new CosMultipartResolver(sc);
		assertEquals(new File("mytemp"), resolver.getUploadTempDir());
	}
	
	public void testMultipartResolution() throws MultipartException, IOException{
		MockServletContext sc = new MockServletContext();
		MockHttpServletRequest rq = new MockHttpServletRequest(sc);
		CosMultipartResolver resolver = new CosMultipartResolver(sc);
		resolver.setUploadTempDir(new FileSystemResource("bogusTmpDir"));
		try {
			resolver.resolveMultipart(rq);
			fail("the http request was mocked, expected a MultipartException");
		} 
		catch (MultipartException e){
			//expected
		}
		new File("bogusTmpDir").delete();
	}

	public void testWithPhysicalFile() throws IOException{
		MockServletContext sc = new MockServletContext();
		CosMultipartResolver resolver = new CosMultipartResolver(sc);
		resolver.setUploadTempDir(new FileSystemResource("bogusTmpDir"));
		assertTrue(new File("bogusTmpDir").exists());
		new File("bogusTmpDir").delete();
	}
}
