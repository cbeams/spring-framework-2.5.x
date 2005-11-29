/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.web.context;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.RequestHolder;

public class SessionLoaderTests extends TestCase {


	public void testSessionLoader() {
		
		MockServletContext servletContext = new MockServletContext();
		SessionLoader sessionLoader = new SessionLoader();
		servletContext.addInitParameter(SessionLoader.CONFIG_LOCATION_PARAM, "/org/springframework/web/context/WEB-INF/sessionContext.xml");
		ConfigurableWebApplicationContext applicationContext = sessionLoader.initWebApplicationContext(servletContext);
		
		TestBean rod1 = null;
		TestBean rod2 = null;
		TestBean rob1 = null;
		TestBean rob2 = null;
		TestBean test = null;
		
		HttpServletRequest request = new MockHttpServletRequest();
		RequestHolder.bind(request);
		rod1 = (TestBean)applicationContext.getBean("rodProto", TestBean.class);
		rob1 = (TestBean)applicationContext.getBean("rob", TestBean.class);
		test = (TestBean)applicationContext.getBean("rodProto", TestBean.class);
		assertFalse(applicationContext.isSingleton("rodProto"));
		assertSame(rod1, test);
		RequestHolder.clear();
		
		request = new MockHttpServletRequest();
		RequestHolder.bind(request);
		rod2 = (TestBean)applicationContext.getBean("rodProto", TestBean.class);
		rob2 = (TestBean)applicationContext.getBean("rob", TestBean.class);
		RequestHolder.clear();
		
		assertNotSame(rod1, rod2);
		assertSame(rob1, rob2);
	}
	
	public void testSessionLoaderWithParent() {
		MockServletContext servletContext = new MockServletContext();
		ContextLoader contextLoader = new ContextLoader();
		servletContext.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "/org/springframework/web/context/WEB-INF/applicationContext.xml");
		contextLoader.initWebApplicationContext(servletContext);
		
		SessionLoader sessionLoader = new SessionLoader();
		servletContext.addInitParameter(SessionLoader.CONFIG_LOCATION_PARAM, "/org/springframework/web/context/WEB-INF/sessionContext.xml");
		ConfigurableWebApplicationContext applicationContext = sessionLoader.initWebApplicationContext(servletContext);
		
		TestBean rod = null;
		
		rod = (TestBean)applicationContext.getBean("rod", TestBean.class);
	}
}
