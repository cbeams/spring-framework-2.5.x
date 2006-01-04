package org.springframework.scripting.bsh;

import junit.framework.TestCase;
import org.springframework.scripting.Calculator;
import org.springframework.scripting.Messenger;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.dynamic.Refreshable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rob Harrop
 */
public class BshScriptFactoryTests extends TestCase {

	public void testStatic() throws Exception {
		Calculator calc = getCalculator();
		assertFalse("Scripted object should not be instance of Refreshable", calc instanceof Refreshable);
	}

	public void testNonStatic() throws Exception {
		Messenger messenger = getMessenger();

		assertTrue("Should be a proxy for refreshable scripts", AopUtils.isAopProxy(messenger));
		assertTrue("Should be an instance of Refreshable", messenger instanceof Refreshable);

		Refreshable refreshable = (Refreshable) messenger;

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect.", desiredMessage, messenger.getMessage());

		refreshable.refresh();

		assertEquals("Message is incorrect after refresh.", desiredMessage, messenger.getMessage());

		assertEquals("Incorrect refresh count", 2, refreshable.getRefreshCount());
	}

	protected Calculator getCalculator() {
		return (Calculator) getContext().getBean("calculator");
	}

	protected Messenger getMessenger() {
		return (Messenger) getContext().getBean("messenger");
	}

	protected ApplicationContext getContext() {
		return new ClassPathXmlApplicationContext("org/springframework/scripting/bsh/bshContext.xml");
	}
}
