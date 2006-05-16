/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.context.scope;

import junit.framework.TestCase;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.TestBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class RequestScopeTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	private MockHttpServletRequest request;

	private RequestAttributes requestAttributes;

	protected void setUp() throws Exception {
		this.beanFactory = new DefaultListableBeanFactory();
		this.beanFactory.registerScope("request", new RequestScope());
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.loadBeanDefinitions(new ClassPathResource("requestScopeTests.xml", getClass()));

		this.request = new MockHttpServletRequest();
		this.requestAttributes = new ServletRequestAttributes(this.request);
		RequestContextHolder.setRequestAttributes(this.requestAttributes);
	}

	public void testGetFromScope() throws Exception {
		String name = "requestScopedObject";
		assertNull(request.getAttribute(name));
		TestBean bean = (TestBean)this.beanFactory.getBean(name);
		assertEquals(request.getAttribute(name), bean);
		assertSame(bean, this.beanFactory.getBean(name));
	}

}
