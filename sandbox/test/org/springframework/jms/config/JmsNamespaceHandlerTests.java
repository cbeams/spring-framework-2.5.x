/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jms.config;

import java.util.Iterator;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.beans.TestBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 * @author Mark Fisher
 */
public class JmsNamespaceHandlerTests extends TestCase {

	private static final String DEFAULT_CONNECTION_FACTORY = "connectionFactory";

	private static final String EXPLICIT_CONNECTION_FACTORY = "testConnectionFactory";

	private ApplicationContext context;

	protected void setUp() throws Exception {
		this.context = new ClassPathXmlApplicationContext("jmsNamespaceHandlerTests.xml", getClass());
	}

	public void testBeansCreated() {
		Map containers = context.getBeansOfType(DefaultMessageListenerContainer.class);
		Map listeners = context.getBeansOfType(MessageListener.class);

		assertEquals("context should contain 3 containers", 3, containers.size());
		assertEquals("context should contain 3 listeners", 3, listeners.size());
	}

	public void testContainerConfiguration() throws Exception {
		Map containers = context.getBeansOfType(DefaultMessageListenerContainer.class);
		ConnectionFactory defaultConnectionFactory = (ConnectionFactory) context.getBean(DEFAULT_CONNECTION_FACTORY);
		ConnectionFactory explicitConnectionFactory = (ConnectionFactory) context.getBean(EXPLICIT_CONNECTION_FACTORY);

		int defaultConnectionFactoryCount = 0;
		int explicitConnectionFactoryCount = 0;

		Iterator iter = containers.values().iterator();
		while (iter.hasNext()) {
			DefaultMessageListenerContainer container = (DefaultMessageListenerContainer) iter.next();
			if (container.getConnectionFactory().equals(defaultConnectionFactory)) {
				defaultConnectionFactoryCount++;
			}
			else if (container.getConnectionFactory().equals(explicitConnectionFactory)) {
				explicitConnectionFactoryCount++;
			}
		}

		assertEquals("1 container should have the default connectionFactory", 1, defaultConnectionFactoryCount);
		assertEquals("2 containers should have the explicit connectionFactory", 2, explicitConnectionFactoryCount);
	}

	public void testListeners() throws Exception {
		TestBean testBean1 = (TestBean) context.getBean("testBean1");
		TestBean testBean2 = (TestBean) context.getBean("testBean2");
		TestBean testBean3 = (TestBean) context.getBean("testBean3");

		assertNull(testBean1.getName());
		assertNull(testBean2.getName());
		assertNull(testBean3.getName());

		MockControl control1 = MockControl.createControl(TextMessage.class);
		TextMessage message1 = (TextMessage) control1.getMock();
		control1.expectAndReturn(message1.getText(), "Test1");
		control1.replay();

		MessageListener listener1 = (MessageListener) context.getBean("listener1");
		listener1.onMessage(message1);
		assertEquals("Test1", testBean1.getName());
		control1.verify();

		MockControl control2 = MockControl.createControl(TextMessage.class);
		TextMessage message2 = (TextMessage) control2.getMock();
		control2.expectAndReturn(message2.getText(), "Test2");
		control2.replay();

		MessageListener listener2 = (MessageListener) context.getBean("listener2");
		listener2.onMessage(message2);
		assertEquals("Test2", testBean2.getName());
		control2.verify();

		MockControl control3 = MockControl.createControl(TextMessage.class);
		TextMessage message3 = (TextMessage) control3.getMock();
		control3.expectAndReturn(message3.getText(), "Test3");
		control3.replay();

		MessageListener listener3 = (MessageListener) context.getBean(MessageListenerAdapter.class.getName());
		listener3.onMessage(message3);
		assertEquals("Test3", testBean3.getName());
		control3.verify();
	}

}
