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

package org.springframework.scripting.bsh;

import junit.framework.TestCase;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.dynamic.Refreshable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scripting.Calculator;
import org.springframework.scripting.Messenger;

/**
 * Unit and integration tests for the BshScriptFactory class.
 *
 * @author Rob Harrop
 * @author Rick Evans
 */
public class BshScriptFactoryTests extends TestCase {

	private static final String BSH_SCRIPT_SOURCE_LOCATOR = "inline:String bingo;";


	public void testStatic() throws Exception {
		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshContext.xml");
		Calculator calc = (Calculator) ctx.getBean("calculator");
		Messenger messenger = (Messenger) ctx.getBean("messenger");

		assertFalse("Scripted object should not be instance of Refreshable", calc instanceof Refreshable);
		assertFalse("Scripted object should not be instance of Refreshable", messenger instanceof Refreshable);

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect", desiredMessage, messenger.getMessage());
	}

	public void testNonStatic() throws Exception {
		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshRefreshableContext.xml");
		Messenger messenger = (Messenger) ctx.getBean("messenger");

		assertTrue("Should be a proxy for refreshable scripts", AopUtils.isAopProxy(messenger));
		assertTrue("Should be an instance of Refreshable", messenger instanceof Refreshable);

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect", desiredMessage, messenger.getMessage());

		Refreshable refreshable = (Refreshable) messenger;
		refreshable.refresh();

		assertEquals("Message is incorrect after refresh", desiredMessage, messenger.getMessage());

		assertEquals("Incorrect refresh count", 2, refreshable.getRefreshCount());
	}

	public void testCtorWithNullScriptSourceLocator() throws Exception {
		try {
			new BshScriptFactory(null, new Class[]{Messenger.class});
			fail("Must have thrown exception by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testCtorWithEmptyScriptSourceLocator() throws Exception {
		try {
			new BshScriptFactory("", new Class[]{Messenger.class});
			fail("Must have thrown exception by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testCtorWithWhitespacedScriptSourceLocator() throws Exception {
		try {
			new BshScriptFactory("\n   ", new Class[]{Messenger.class});
			fail("Must have thrown exception by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testCtorWithNullScriptInterfacesArray() throws Exception {
		try {
			new BshScriptFactory(BSH_SCRIPT_SOURCE_LOCATOR, null);
			fail("Must have thrown exception by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testCtorWithEmptyScriptInterfacesArray() throws Exception {
		try {
			new BshScriptFactory(BSH_SCRIPT_SOURCE_LOCATOR, new Class[]{});
			fail("Must have thrown exception by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

}
