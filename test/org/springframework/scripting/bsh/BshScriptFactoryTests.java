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

package org.springframework.scripting.bsh;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.dynamic.Refreshable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.JdkVersion;
import org.springframework.core.NestedRuntimeException;
import org.springframework.scripting.Calculator;
import org.springframework.scripting.ConfigurableMessenger;
import org.springframework.scripting.Messenger;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ScriptFactoryPostProcessor;

/**
 * Unit and integration tests for the BshScriptFactory class.
 *
 * @author Rob Harrop
 * @author Rick Evans
 * @author Juergen Hoeller
 */
public class BshScriptFactoryTests extends TestCase {

	public void testStatic() throws Exception {
		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshContext.xml");
		Calculator calc = (Calculator) ctx.getBean("calculator");
		Messenger messenger = (Messenger) ctx.getBean("messenger");

		assertFalse("Scripted object should not be instance of Refreshable", calc instanceof Refreshable);
		assertFalse("Scripted object should not be instance of Refreshable", messenger instanceof Refreshable);

		assertEquals(5, calc.add(2, 3));

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect", desiredMessage, messenger.getMessage());
	}

	public void testStaticWithNullReturnValue() throws Exception {
		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshContext.xml");
		ConfigurableMessenger messenger = (ConfigurableMessenger) ctx.getBean("messenger");

		messenger.setMessage(null);
		assertNull(messenger.getMessage());
	}

	public void testStaticWithScriptImplementingInterface() throws Exception {
		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshContext.xml");
		Messenger messenger = (Messenger) ctx.getBean("messengerImpl");

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect", desiredMessage, messenger.getMessage());
	}

	public void testStaticWithScriptReturningInstance() throws Exception {
		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshContext.xml");
		Messenger messenger = (Messenger) ctx.getBean("messengerInstance");

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

	public void testScriptCompilationException() throws Exception {
		try {
			new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshBrokenContext.xml");
			fail("Must throw exception for broken script file");
		}
		catch (NestedRuntimeException ex) {
			assertTrue(ex.contains(ScriptCompilationException.class));
		}
	}

	public void testScriptThatCompilesButIsJustPlainBad() throws Exception {
		MockControl mock = MockControl.createControl(ScriptSource.class);
		ScriptSource script = (ScriptSource) mock.getMock();
		script.getScriptAsString();
		final String badScript = "String getMessage() { throw new IllegalArgumentException(); }";
		mock.setReturnValue(badScript);
		mock.replay();
		BshScriptFactory factory = new BshScriptFactory(
				ScriptFactoryPostProcessor.INLINE_SCRIPT_PREFIX + badScript,
				new Class[]{Messenger.class});
		try {
			Messenger messenger = (Messenger) factory.getScriptedObject(script, new Class[]{Messenger.class});
			messenger.getMessage();
			fail("Must have thrown a BshScriptUtils.BshExecutionException.");
		}
		catch (BshScriptUtils.BshExecutionException expected) {
		}
		mock.verify();
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

	public void testResourceScriptFromTag() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		ApplicationContext ctx = new ClassPathXmlApplicationContext("bsh-with-xsd.xml", getClass());
		Messenger messenger = (Messenger) ctx.getBean("messenger");
		assertEquals("Hello World!", messenger.getMessage());
		assertFalse(messenger instanceof Refreshable);

		Messenger messengerImpl = (Messenger) ctx.getBean("messengerImpl");
		assertEquals("Hello World!", messengerImpl.getMessage());

		Messenger messengerInstance = (Messenger) ctx.getBean("messengerInstance");
		assertEquals("Hello World!", messengerInstance.getMessage());
	}

	public void testInlineScriptFromTag() throws Exception {
	  if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		ApplicationContext ctx = new ClassPathXmlApplicationContext("bsh-with-xsd.xml", getClass());
		Calculator calculator = (Calculator) ctx.getBean("calculator");
		assertNotNull(calculator);
		assertFalse(calculator instanceof Refreshable);
	}

	public void testRefreshableFromTag() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		ApplicationContext ctx = new ClassPathXmlApplicationContext("bsh-with-xsd.xml", getClass());
		Messenger messenger = (Messenger) ctx.getBean("refreshableMessenger");
		assertEquals("Hello World!", messenger.getMessage());
		assertTrue("Messenger should be Refreshable", messenger instanceof Refreshable);
	}

}
