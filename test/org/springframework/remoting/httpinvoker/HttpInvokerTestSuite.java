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

package org.springframework.remoting.httpinvoker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * @author Juergen Hoeller
 * @since 09.08.2004
 */
public class HttpInvokerTestSuite extends TestCase {

	public void testHttpInvokerProxyFactoryBeanAndServiceExporter() throws Throwable {
		TestBean target = new TestBean("myname", 99);
		final HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.afterPropertiesSet();

		HttpInvokerProxyFactoryBean pfb = new HttpInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");

		pfb.setHttpInvokerRequestExecutor(new AbstractHttpInvokerRequestExecutor() {
			protected RemoteInvocationResult doExecuteRequest(
					HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
					throws IOException, ClassNotFoundException {
				assertEquals("http://myurl", config.getServiceUrl());
				MockHttpServletRequest request = new MockHttpServletRequest();
				MockHttpServletResponse response = new MockHttpServletResponse();
				request.setContent(baos.toByteArray());
				exporter.handleRequest(request, response);
				return readRemoteInvocationResult(new ByteArrayInputStream(response.getContentAsByteArray()));
			}
		});

		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());
		proxy.setAge(50);
		assertEquals(50, proxy.getAge());

		try {
			proxy.exceptional(new IllegalStateException());
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
		try {
			proxy.exceptional(new IllegalAccessException());
			fail("Should have thrown IllegalAccessException");
		}
		catch (IllegalAccessException ex) {
			// expected
		}
	}

	public void testHttpInvokerProxyFactoryBeanAndServiceExporterWithIOException() throws Exception {
		TestBean target = new TestBean("myname", 99);
		final HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.afterPropertiesSet();

		HttpInvokerProxyFactoryBean pfb = new HttpInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");

		pfb.setHttpInvokerRequestExecutor(new HttpInvokerRequestExecutor() {
			public RemoteInvocationResult executeRequest(
					HttpInvokerClientConfiguration config, RemoteInvocation invocation) throws IOException {
				throw new IOException("argh");
			}
		});

		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();
		try {
			proxy.setAge(50);
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
			assertTrue(ex.getCause() instanceof IOException);
		}
	}

	public void testHttpInvokerProxyFactoryBeanAndServiceExporterWithInvocationAttributes() throws Exception {
		TestBean target = new TestBean("myname", 99);
		final HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.setRemoteInvocationExecutor(new DefaultRemoteInvocationExecutor() {
			public Object invoke(RemoteInvocation invocation, Object targetObject)
					throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
				assertNotNull(invocation.getAttributes());
				assertEquals(1, invocation.getAttributes().size());
				assertEquals("myValue", invocation.getAttributes().get("myKey"));
				assertEquals("myValue", invocation.getAttribute("myKey"));
				return super.invoke(invocation, targetObject);
			}
		});
		exporter.afterPropertiesSet();

		HttpInvokerProxyFactoryBean pfb = new HttpInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");
		pfb.setRemoteInvocationFactory(new RemoteInvocationFactory() {
			public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
				RemoteInvocation invocation = new RemoteInvocation(methodInvocation);
				invocation.addAttribute("myKey", "myValue");
				try {
					invocation.addAttribute("myKey", "myValue");
					fail("Should have thrown IllegalStateException");
				}
				catch (IllegalStateException ex) {
					// expected: already defined
				}
				assertNotNull(invocation.getAttributes());
				assertEquals(1, invocation.getAttributes().size());
				assertEquals("myValue", invocation.getAttributes().get("myKey"));
				assertEquals("myValue", invocation.getAttribute("myKey"));
				return invocation;
			}
		});

		pfb.setHttpInvokerRequestExecutor(new AbstractHttpInvokerRequestExecutor() {
			protected RemoteInvocationResult doExecuteRequest(
					HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
					throws IOException, ClassNotFoundException {
				assertEquals("http://myurl", config.getServiceUrl());
				MockHttpServletRequest request = new MockHttpServletRequest();
				MockHttpServletResponse response = new MockHttpServletResponse();
				request.setContent(baos.toByteArray());
				exporter.handleRequest(request, response);
				return readRemoteInvocationResult(new ByteArrayInputStream(response.getContentAsByteArray()));
			}
		});

		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());
	}

	public void testHttpInvokerProxyFactoryBeanAndServiceExporterWithCustomInvocationObject() throws Exception {
		TestBean target = new TestBean("myname", 99);
		final HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.setRemoteInvocationExecutor(new DefaultRemoteInvocationExecutor() {
			public Object invoke(RemoteInvocation invocation, Object targetObject)
					throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
				assertTrue(invocation instanceof TestRemoteInvocation);
				assertNull(invocation.getAttributes());
				assertNull(invocation.getAttribute("myKey"));
				return super.invoke(invocation, targetObject);
			}
		});
		exporter.afterPropertiesSet();

		HttpInvokerProxyFactoryBean pfb = new HttpInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");
		pfb.setRemoteInvocationFactory(new RemoteInvocationFactory() {
			public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
				RemoteInvocation invocation = new TestRemoteInvocation(methodInvocation);
				assertNull(invocation.getAttributes());
				assertNull(invocation.getAttribute("myKey"));
				return invocation;
			}
		});

		pfb.setHttpInvokerRequestExecutor(new AbstractHttpInvokerRequestExecutor() {
			protected RemoteInvocationResult doExecuteRequest(
					HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
					throws IOException, ClassNotFoundException {
				assertEquals("http://myurl", config.getServiceUrl());
				MockHttpServletRequest request = new MockHttpServletRequest();
				MockHttpServletResponse response = new MockHttpServletResponse();
				request.setContent(baos.toByteArray());
				exporter.handleRequest(request, response);
				return readRemoteInvocationResult(new ByteArrayInputStream(response.getContentAsByteArray()));
			}
		});

		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());
	}


	private static class TestRemoteInvocation extends RemoteInvocation {

		public TestRemoteInvocation(MethodInvocation methodInvocation) {
			super(methodInvocation);
		}

	}

}
