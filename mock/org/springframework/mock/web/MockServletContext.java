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

package org.springframework.mock.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.util.WebUtils;

/**
 * Mock implementation of the ServletContext interface.
 *
 * <p>Used for testing the web framework; only rarely necessary for
 * testing application controllers, as long as they don't explicitly access
 * the ServletContext. In the latter case, ClassPathXmlApplicationContext
 * can be used to load them; else, XmlWebApplicationContext needs to be
 * used, possibly with this MockServletContext class.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class MockServletContext implements ServletContext {

	private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";


	private final Log logger = LogFactory.getLog(getClass());

	private final String resourceBasePath;
	
	private final ResourceLoader resourceLoader;

	private final Properties initParameters = new Properties();

	private final Hashtable attributes = new Hashtable();


	/**
	 * Create a new MockServletContext, using no base path and a
	 * DefaultResourceLoader (i.e. the classpath root as WAR root).
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public MockServletContext() {
		this("");
	}

	/**
	 * Create a new MockServletContext, using a DefaultResourceLoader.
	 * @param resourceBasePath the WAR root directory (should not end with a /)
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public MockServletContext(String resourceBasePath) {
		this(resourceBasePath, new DefaultResourceLoader());
	}

	/**
	 * Create a new MockServletContext.
	 * @param resourceBasePath the WAR root directory (should not end with a /)
	 * @param resourceLoader the ResourceLoader to use
	 */
	public MockServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
		this.resourceBasePath = resourceBasePath;
		this.resourceLoader = resourceLoader;

		// use JVM temp dir as ServletContext temp dir
		String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
		if (tempDir != null) {
			this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
		}
	}

	protected String getResourceLocation(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return this.resourceBasePath + path;
	}


	public ServletContext getContext(String name) {
		throw new UnsupportedOperationException("getContext");
	}

	public int getMajorVersion() {
		return 2;
	}

	public int getMinorVersion() {
		return 3;
	}

	public String getMimeType(String filePath) {
		throw new UnsupportedOperationException("getMimeType");
	}

	public Set getResourcePaths(String path) {
		throw new UnsupportedOperationException("getResourcePaths");
	}

	public URL getResource(String path) throws MalformedURLException {
		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
		try {
			return resource.getURL();
		}
		catch (IOException ex) {
			logger.info("Couldn't get URL for resource " + resource, ex);
			return null;
		}
	}

	public InputStream getResourceAsStream(String path) {
		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
		try {
			return resource.getInputStream();
		}
		catch (IOException ex) {
			logger.info("Couldn't open InputStream for resource " + resource, ex);
			return null;
		}
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return new MockRequestDispatcher(path);
	}

	public RequestDispatcher getNamedDispatcher(String path) {
		throw new UnsupportedOperationException("getNamedDispatcher");
	}

	public Servlet getServlet(String name) {
		throw new UnsupportedOperationException("getServlet");
	}

	public Enumeration getServlets() {
		throw new UnsupportedOperationException("getServlets");
	}

	public Enumeration getServletNames() {
		throw new UnsupportedOperationException("getServletNames");
	}

	public void log(String message) {
		logger.info(message);
	}

	public void log(Exception e, String message) {
		logger.info(message, e);
	}

	public void log(String message, Throwable t) {
		logger.info(message, t);
	}

	public String getRealPath(String path) {
		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
		try {
			return resource.getFile().getAbsolutePath();
		}
		catch (IOException ex) {
			logger.info("Couldn't determine real path of resource " + resource, ex);
			return null;
		}
	}

	public String getServerInfo() {
		return "MockServletContext";
	}

	public String getInitParameter(String name) {
		return this.initParameters.getProperty(name);
	}

	public void addInitParameter(String name, String value) {
		this.initParameters.put(name, value);
	}

	public Enumeration getInitParameterNames() {
		return this.initParameters.keys();
	}

	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Enumeration getAttributeNames() {
		return this.attributes.keys();
	}

	public void setAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public String getServletContextName() {
		return "MockServletContext";
	}

}
