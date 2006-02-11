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
import org.easymock.MockControl;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.dynamic.Refreshable;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.JdkVersion;
import org.springframework.scripting.Calculator;
import org.springframework.scripting.Messenger;
import org.springframework.scripting.ScriptSource;

import java.io.FileNotFoundException;

/**
 * Unit tests for the GroovyScriptFactory class.
 *
 * @author Rob Harrop
 * @author Rick Evans
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

	public void testWithTwoClassesDefinedInTheOneGroovyFile_CorrectClassFirst() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/scripting/groovy/twoClassesCorrectOneFirst.xml");
		Messenger messenger = (Messenger) ctx.getBean("messenger");
		assertNotNull(messenger);
		assertEquals("Hello World!", messenger.getMessage());
	}

	public void testWithTwoClassesDefinedInTheOneGroovyFile_WrongClassFirst() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		try {
			ApplicationContext ctx =
					new ClassPathXmlApplicationContext("org/springframework/scripting/groovy/twoClassesWrongOneFirst.xml");
			ctx.getBean("messenger", Messenger.class);
			fail("Must have failed: two classes defined in GroovyScriptFactory source, non-Messenger class defined first.");
		}
		// just testing for failure here, hence catching Exception...
		catch (Exception expected) {
		}
	}

	public void testCtorWithNullScriptSourceLocator() throws Exception {
		try {
			new GroovyScriptFactory(null);
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testCtorWithEmptyScriptSourceLocator() throws Exception {
		try {
			new GroovyScriptFactory("");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testCtorWithWhitespacedScriptSourceLocator() throws Exception {
		try {
			new GroovyScriptFactory("\n   ");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testWithInlineScriptWithLeadingWhitespace() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		try {
			new ClassPathXmlApplicationContext("org/springframework/scripting/groovy/lwspBadGroovyContext.xml");
			fail("Must have thrown a BeanCreationException ('inline:' prefix was preceded by whitespace");
		}
		catch (BeanCreationException expected) {
			assertTrue(expected.contains(FileNotFoundException.class));
		}
	}

	public void testGetScriptedObjectDoesNotChokeOnNullInterfacesBeingPassedIn() throws Exception {
		MockControl mock = MockControl.createControl(ScriptSource.class);
		ScriptSource scriptSource = (ScriptSource) mock.getMock();
		scriptSource.getScriptAsString();
		mock.setDefaultReturnValue("class Bar {}");
		mock.replay();

		GroovyScriptFactory factory = new GroovyScriptFactory("a script source locator (doesn't matter here)");
		Object scriptedObject = factory.getScriptedObject(scriptSource, null);
		assertNotNull(scriptedObject);
		mock.verify();
	}

	public void testGetScriptedObjectDoesChokeOnNullScriptSourceBeingPassedIn() throws Exception {
		GroovyScriptFactory factory = new GroovyScriptFactory("a script source locator (doesn't matter here)");
		try {
			factory.getScriptedObject(null, null);
			fail("Must have thrown a NullPointerException as per contract ('null' ScriptSource supplied");
		}
		catch (NullPointerException expected) {
		}
	}

}
