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

package org.springframework.jms.remoting;

import java.lang.reflect.InvocationTargetException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInvocation;
import org.codehaus.activemq.ActiveMQConnectionFactory;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;

/**
 * @author James Strachan
 */
public class JmsInvokerTests extends TestCase {

	private ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");

	private QueueConnection connection;

	public void testJmsProxyFactoryBeanAndServiceExporter() throws Throwable {
		TestBean target = new TestBean("myname", 99);
		final JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.setJmsTemplate(createJmsTemplate());
		exporter.afterPropertiesSet();
		subscribeToQueue(exporter, getName());

		JmsInvokerProxyFactoryBean pfb = new JmsInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");

		QueueSession queueSession = createQueueSession();
		pfb.setSession(queueSession);
		pfb.setQueue(queueSession.createQueue(getName()));
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

		pfb.destroy();
	}

	public void testJmsProxyFactoryBeanAndServiceExporterWithJMSException() throws Exception {
		TestBean target = new TestBean("myname", 99);
		final JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.setJmsTemplate(createJmsTemplate());
		exporter.afterPropertiesSet();
		subscribeToQueue(exporter, getName());

		JmsInvokerProxyFactoryBean pfb = new JmsInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");

		QueueSession queueSession = createQueueSession();
		pfb.setSession(queueSession);
		pfb.setQueue(queueSession.createQueue(getName()));
		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();

		// lets force an exception by closing the session
		queueSession.close();
		try {
			proxy.setAge(50);
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
			assertTrue(ex.getCause() instanceof JMSException);
		}

		pfb.destroy();
	}

	public void testJmsProxyFactoryBeanAndServiceExporterWithInvocationAttributes() throws Exception {
		TestBean target = new TestBean("myname", 99);
		final JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.setJmsTemplate(createJmsTemplate());
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
		subscribeToQueue(exporter, getName());

		JmsInvokerProxyFactoryBean pfb = new JmsInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");

		QueueSession session = createQueueSession();
		pfb.setSession(session);
		pfb.setQueue(session.createQueue(getName()));
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

		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());

		pfb.destroy();
	}

	public void testJmsProxyFactoryBeanAndServiceExporterWithCustomInvocationObject() throws Exception {
		TestBean target = new TestBean("myname", 99);
		final JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
		exporter.setJmsTemplate(createJmsTemplate());
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
		subscribeToQueue(exporter, getName());

		JmsInvokerProxyFactoryBean pfb = new JmsInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl");

		QueueSession queueSession = createQueueSession();
		pfb.setSession(queueSession);
		pfb.setQueue(queueSession.createQueue(getName()));
		pfb.setRemoteInvocationFactory(new RemoteInvocationFactory() {
			public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
				RemoteInvocation invocation = new TestRemoteInvocation(methodInvocation);
				assertNull(invocation.getAttributes());
				assertNull(invocation.getAttribute("myKey"));
				return invocation;
			}
		});
		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());

		pfb.destroy();
	}

	public void testJmsInvokerWithSpecialLocalMethods() throws Exception {
		String serviceUrl = "http://myurl";
		JmsInvokerProxyFactoryBean pfb = new JmsInvokerProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl(serviceUrl);

		QueueSession session = createQueueSession();
		pfb.setSession(session);
		pfb.setQueue(session.createQueue(getName()));
		pfb.afterPropertiesSet();
		ITestBean proxy = (ITestBean) pfb.getObject();

		// shouldn't go through to remote service
		assertTrue(proxy.toString().indexOf("JMS invoker") != -1);
		assertTrue(proxy.toString().indexOf(serviceUrl) != -1);
		assertEquals(proxy.hashCode(), proxy.hashCode());
		assertTrue(proxy.equals(proxy));

		// lets force an exception by closing the session
		session.close();
		try {
			proxy.setAge(50);
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
			assertTrue(ex.getCause() instanceof JMSException);
		}

		pfb.destroy();
	}

	protected void subscribeToQueue(JmsInvokerServiceExporter exporter, String queueName) throws JMSException {
		QueueSession serverSession = createQueueSession();
		Queue queue = serverSession.createQueue(queueName);
		MessageConsumer consumer = serverSession.createConsumer(queue);
		consumer.setMessageListener(exporter);
	}

	protected JmsTemplate createJmsTemplate() {
		JmsTemplate answer = new JmsTemplate();
		answer.setConnectionFactory(connectionFactory);
		answer.setPubSubDomain(false);
		return answer;
	}

	protected QueueSession createQueueSession() throws JMSException {
		return getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	protected QueueConnection getConnection() throws JMSException {
		if (connection == null) {
			connection = connectionFactory.createQueueConnection();
			connection.start();
		}
		return connection;
	}

	protected void tearDown() throws Exception {
		if (connection != null) {
			connection.close();
		}
		connectionFactory.stop();
	}


	private static class TestRemoteInvocation extends RemoteInvocation {

		public TestRemoteInvocation(MethodInvocation methodInvocation) {
			super(methodInvocation);
		}
	}

}
