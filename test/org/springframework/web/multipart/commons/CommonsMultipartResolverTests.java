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

package org.springframework.web.multipart.commons;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.multipart.support.StringMultipartFileEditor;
import org.springframework.web.util.WebUtils;

/**
 * @author Juergen Hoeller
 * @since 08.10.2003
 */
public class CommonsMultipartResolverTests extends TestCase {

	public void testWithApplicationContext() throws MultipartException, IOException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.getServletContext().setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		wac.refresh();
		MockCommonsMultipartResolver resolver = new MockCommonsMultipartResolver();
		DiskFileUpload fileUpload = resolver.getFileUpload();
		resolver.setMaxUploadSize(1000);
		resolver.setMaxInMemorySize(100);
		resolver.setDefaultEncoding("enc");
		resolver.setServletContext(wac.getServletContext());
		assertEquals(1000, fileUpload.getSizeMax());
		assertEquals(100, fileUpload.getSizeThreshold());
		assertEquals("enc", fileUpload.getHeaderEncoding());
		assertTrue(fileUpload.getRepositoryPath().endsWith("mytemp"));

		MockHttpServletRequest originalRequest = new MockHttpServletRequest();
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		assertTrue(resolver.isMultipart(originalRequest));
		MultipartHttpServletRequest request = resolver.resolveMultipart(originalRequest);

		Set parameterNames = new HashSet();
		Enumeration parameterEnum = request.getParameterNames();
		while (parameterEnum.hasMoreElements()) {
			parameterNames.add(parameterEnum.nextElement());
		}
		assertEquals(2, parameterNames.size());
		assertTrue(parameterNames.contains("field3"));
		assertTrue(parameterNames.contains("field4"));
		assertEquals("value3", request.getParameter("field3"));
		List parameterValues = Arrays.asList(request.getParameterValues("field3"));
		assertEquals(1, parameterValues.size());
		assertTrue(parameterValues.contains("value3"));
		assertEquals("value4", request.getParameter("field4"));
		parameterValues = Arrays.asList(request.getParameterValues("field4"));
		assertEquals(2, parameterValues.size());
		assertTrue(parameterValues.contains("value4"));
		assertTrue(parameterValues.contains("value5"));
		List parameterMapKeys = new ArrayList();
		List parameterMapValues = new ArrayList();
		for (Iterator parameterMapIter = request.getParameterMap().keySet().iterator(); parameterMapIter.hasNext();) {
			String key = (String) parameterMapIter.next();
			parameterMapKeys.add(key);
			parameterMapValues.add(request.getParameterMap().get(key));
		}
		assertEquals(2, parameterMapKeys.size());
		assertEquals(2, parameterMapValues.size());
		int field3Index = parameterMapKeys.indexOf("field3");
		int field4Index = parameterMapKeys.indexOf("field4");
		assertTrue(field3Index != -1);
		assertTrue(field4Index != -1);
		parameterValues = Arrays.asList((String[]) parameterMapValues.get(field3Index));
		assertEquals(1, parameterValues.size());
		assertTrue(parameterValues.contains("value3"));
		parameterValues = Arrays.asList((String[]) parameterMapValues.get(field4Index));
		assertEquals(2, parameterValues.size());
		assertTrue(parameterValues.contains("value4"));
		assertTrue(parameterValues.contains("value5"));

		Set fileNames = new HashSet();
		Iterator fileIter = request.getFileNames();
		while (fileIter.hasNext()) {
			fileNames.add(fileIter.next());
		}
		assertEquals(3, fileNames.size());
		assertTrue(fileNames.contains("field1"));
		assertTrue(fileNames.contains("field2"));
		assertTrue(fileNames.contains("field2x"));
		CommonsMultipartFile file1 = (CommonsMultipartFile) request.getFile("field1");
		CommonsMultipartFile file2 = (CommonsMultipartFile) request.getFile("field2");
		CommonsMultipartFile file2x = (CommonsMultipartFile) request.getFile("field2x");
		List fileMapKeys = new ArrayList();
		List fileMapValues = new ArrayList();
		for (Iterator fileMapIter = request.getFileMap().keySet().iterator(); fileMapIter.hasNext();) {
			String key = (String) fileMapIter.next();
			fileMapKeys.add(key);
			fileMapValues.add(request.getFileMap().get(key));
		}
		assertEquals(3, fileMapKeys.size());
		assertEquals(3, fileMapValues.size());
		int field1Index = fileMapKeys.indexOf("field1");
		int field2Index = fileMapKeys.indexOf("field2");
		int field2xIndex = fileMapKeys.indexOf("field2x");
		assertTrue(field1Index != -1);
		assertTrue(field2Index != -1);
		assertTrue(field2xIndex != -1);
		MultipartFile mapFile1 = (MultipartFile) fileMapValues.get(field1Index);
		MultipartFile mapFile2 = (MultipartFile) fileMapValues.get(field2Index);
		MultipartFile mapFile2x = (MultipartFile) fileMapValues.get(field2xIndex);
		assertEquals(mapFile1, file1);
		assertEquals(mapFile2, file2);
		assertEquals(mapFile2x, file2x);

		assertEquals("type1", file1.getContentType());
		assertEquals("type2", file2.getContentType());
		assertEquals("type2", file2x.getContentType());
		assertEquals("field1.txt", file1.getOriginalFilename());
		assertEquals("field2.txt", file2.getOriginalFilename());
		assertEquals("field2x.txt", file2x.getOriginalFilename());
		assertEquals("text1", new String(file1.getBytes()));
		assertEquals("text2", new String(file2.getBytes()));
		assertEquals(5, file1.getSize());
		assertEquals(5, file2.getSize());
		assertTrue(file1.getInputStream() instanceof ByteArrayInputStream);
		assertTrue(file2.getInputStream() instanceof ByteArrayInputStream);
		File transfer1 = new File("C:/transfer1");
		File transfer2 = new File("C:/transfer2");
		file1.transferTo(transfer1);
		file2.transferTo(transfer2);
		assertEquals(transfer1, ((MockFileItem) file1.getFileItem()).writtenFile);
		assertEquals(transfer2, ((MockFileItem) file2.getFileItem()).writtenFile);

		MultipartTestBean1 mtb1 = new MultipartTestBean1();
		assertEquals(null, mtb1.getField1());
		assertEquals(null, mtb1.getField2());
		ServletRequestDataBinder binder = new ServletRequestDataBinder(mtb1, "mybean");
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		binder.bind(request);
		assertEquals(file1, mtb1.getField1());
		assertEquals(new String(file2.getBytes()), new String(mtb1.getField2()));

		MultipartTestBean2 mtb2 = new MultipartTestBean2();
		assertEquals(null, mtb2.getField1());
		assertEquals(null, mtb2.getField2());
		binder = new ServletRequestDataBinder(mtb2, "mybean");
		binder.registerCustomEditor(String.class, "field1", new StringMultipartFileEditor());
		binder.registerCustomEditor(String.class, "field2", new StringMultipartFileEditor("UTF-16"));
		binder.bind(request);
		assertEquals(new String(file1.getBytes()), mtb2.getField1());
		assertEquals(new String(file2.getBytes(), "UTF-16"), mtb2.getField2());

		resolver.cleanupMultipart(request);
		assertTrue(((MockFileItem) file1.getFileItem()).deleted);
		assertTrue(((MockFileItem) file2.getFileItem()).deleted);

		resolver.setEmpty(true);
		request = resolver.resolveMultipart(originalRequest);
		binder.setBindEmptyMultipartFiles(false);
		String firstBound = mtb2.getField1();
		binder.bind(request);
		assertTrue(mtb2.getField1().length() > 0);
		assertEquals(firstBound, mtb2.getField1());

		request = resolver.resolveMultipart(originalRequest);
		binder.setBindEmptyMultipartFiles(true);
		binder.bind(request);
		assertTrue(mtb2.getField1().length() == 0);
	}

	public void testWithServletContextAndFilter() throws ServletException, IOException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.registerSingleton("filterMultipartResolver", MockCommonsMultipartResolver.class, new MutablePropertyValues());
		wac.getServletContext().setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		wac.refresh();
		wac.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(wac.getServletContext());
		assertTrue(resolver.getFileUpload().getRepositoryPath().endsWith("mytemp"));

		MockFilterConfig filterConfig = new MockFilterConfig(wac.getServletContext(), "filter");
		final MultipartFilter filter = new MultipartFilter();
		filter.init(filterConfig);

		final List files = new ArrayList();
		final FilterChain filterChain = new FilterChain() {
			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
				MultipartHttpServletRequest request = (MultipartHttpServletRequest) servletRequest;
				files.addAll(request.getFileMap().values());
			}
		};

		FilterChain filterChain2 = new FilterChain() {
			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
			    throws IOException, ServletException {
				filter.doFilter(servletRequest, servletResponse, filterChain);
			}
		};

		MockHttpServletRequest originalRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		filter.doFilter(originalRequest, response, filterChain2);

		CommonsMultipartFile file1 = (CommonsMultipartFile) files.get(0);
		CommonsMultipartFile file2 = (CommonsMultipartFile) files.get(1);
		assertTrue(((MockFileItem) file1.getFileItem()).deleted);
		assertTrue(((MockFileItem) file2.getFileItem()).deleted);
	}

	public void testWithServletContextAndFilterWithCustomBeanName() throws ServletException, IOException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		wac.registerSingleton("myMultipartResolver", MockCommonsMultipartResolver.class, new MutablePropertyValues());
		wac.getServletContext().setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		wac.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(wac.getServletContext());
		assertTrue(resolver.getFileUpload().getRepositoryPath().endsWith("mytemp"));

		MockFilterConfig filterConfig = new MockFilterConfig(wac.getServletContext(), "filter");
		filterConfig.addInitParameter("multipartResolverBeanName", "myMultipartResolver");

		final List files = new ArrayList();
		FilterChain filterChain = new FilterChain() {
			public void doFilter(ServletRequest originalRequest, ServletResponse response) {
				if (originalRequest instanceof MultipartHttpServletRequest) {
					MultipartHttpServletRequest request = (MultipartHttpServletRequest) originalRequest;
					files.addAll(request.getFileMap().values());
				}
			}
		};

		MultipartFilter filter = new MultipartFilter() {
			private boolean invoked = false;
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
																			FilterChain filterChain) throws ServletException, IOException {
				super.doFilterInternal(request, response, filterChain);
				if (invoked) {
					throw new ServletException("Should not have been invoked twice");
				}
				invoked = true;
			}
		};
		filter.init(filterConfig);

		MockHttpServletRequest originalRequest = new MockHttpServletRequest();
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		HttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(originalRequest, response, filterChain);
		filter.doFilter(originalRequest, response, filterChain);
		CommonsMultipartFile file1 = (CommonsMultipartFile) files.get(0);
		CommonsMultipartFile file2 = (CommonsMultipartFile) files.get(1);
		assertTrue(((MockFileItem) file1.getFileItem()).deleted);
		assertTrue(((MockFileItem) file2.getFileItem()).deleted);
	}


	public static class MockCommonsMultipartResolver extends CommonsMultipartResolver {

		private boolean empty;

		protected void setEmpty(boolean empty) {
			this.empty = empty;
		}

		protected DiskFileUpload newFileUpload() {
			return new DiskFileUpload() {
				public List parseRequest(HttpServletRequest request) {
					if (request instanceof MultipartHttpServletRequest) {
						throw new IllegalStateException("Already a multipart request");
					}
					List fileItems = new ArrayList();
					MockFileItem fileItem1 = new MockFileItem(
					    "field1", "type1", empty ? "" : "field1.txt", empty ? "" : "text1");
					MockFileItem fileItem2 = new MockFileItem(
					    "field2", "type2", empty ? "" : "C:/field2.txt", empty ? "" : "text2");
					MockFileItem fileItem2x = new MockFileItem(
					    "field2x", "type2", empty ? "" : "C:\\field2x.txt", empty ? "" : "text2");
					MockFileItem fileItem3 = new MockFileItem("field3", null, null, "value3");
					MockFileItem fileItem4 = new MockFileItem("field4", null, null, "value4");
					MockFileItem fileItem5 = new MockFileItem("field4", null, null, "value5");
					fileItems.add(fileItem1);
					fileItems.add(fileItem2);
					fileItems.add(fileItem2x);
					fileItems.add(fileItem3);
					fileItems.add(fileItem4);
					fileItems.add(fileItem5);
					return fileItems;
				}
			};
		}
	}


	private static class MockFileItem implements FileItem {

		private String fieldName;
		private String contentType;
		private String name;
		private String value;

		private File writtenFile;
		private boolean deleted;

		public MockFileItem(String fieldName, String contentType, String name, String value) {
			this.fieldName = fieldName;
			this.contentType = contentType;
			this.name = name;
			this.value = value;
		}

		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(value.getBytes());
		}

		public String getContentType() {
			return contentType;
		}

		public String getName() {
			return name;
		}

		public boolean isInMemory() {
			return true;
		}

		public long getSize() {
			return value.length();
		}

		public byte[] get() {
			return value.getBytes();
		}

		public String getString(String encoding) throws UnsupportedEncodingException {
			return new String(get(), encoding);
		}

		public String getString() {
			return value;
		}

		public void write(File file) throws Exception {
			this.writtenFile = file;
		}

		public File getWrittenFile() {
			return writtenFile;
		}

		public void delete() {
			this.deleted = true;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String s) {
			this.fieldName = s;
		}

		public boolean isFormField() {
			return (this.name == null);
		}

		public void setFormField(boolean b) {
			throw new UnsupportedOperationException();
		}

		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
	}


	public class MultipartTestBean1 {

		private MultipartFile field1;
		private byte[] field2;

		public void setField1(MultipartFile field1) {
			this.field1 = field1;
		}

		public MultipartFile getField1() {
			return field1;
		}

		public void setField2(byte[] field2) {
			this.field2 = field2;
		}

		public byte[] getField2() {
			return field2;
		}
	}


	public class MultipartTestBean2 {

		private String field1;
		private String field2;

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField1() {
			return field1;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}

		public String getField2() {
			return field2;
		}
	}

}
