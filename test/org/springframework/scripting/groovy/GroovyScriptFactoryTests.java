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

package org.springframework.scripting.groovy;

import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.dynamic.Refreshable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.JdkVersion;
import org.springframework.scripting.Calculator;
import org.springframework.scripting.Messenger;

/**
 * @author Rob Harrop
 */
public class GroovyScriptFactoryTests extends TestCase {

	public void testStatic() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/groovy/groovyContext.xml");
		Calculator calc = (Calculator) ctx.getBean("calculator");
		Messenger messenger = (Messenger) ctx.getBean("messenger");

		assertFalse("Shouldn't get proxy when refresh is disabled", AopUtils.isAopProxy(calc));
		assertFalse("Shouldn't get proxy when refresh is disabled", AopUtils.isAopProxy(messenger));

		assertFalse("Scripted object should not be instance of Refreshable", calc instanceof Refreshable);
		assertFalse("Scripted object should not be instance of Refreshable", messenger instanceof Refreshable);

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect", desiredMessage, messenger.getMessage());
	}

	public void testNonStatic() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/groovy/groovyRefreshableContext.xml");
		Messenger messenger = (Messenger) ctx.getBean("messenger");

		assertTrue("Should be a proxy for refreshable scripts", AopUtils.isAopProxy(messenger));
		assertTrue("Should be an instance of Refreshable", messenger instanceof Refreshable);

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect", desiredMessage, messenger.getMessage());

		Refreshable refreshable = (Refreshable) messenger;
		refreshable.refresh();

		assertEquals("Message is incorrect after refresh.", desiredMessage, messenger.getMessage());
		assertEquals("Incorrect refresh count", 2, refreshable.getRefreshCount());
	}

}
