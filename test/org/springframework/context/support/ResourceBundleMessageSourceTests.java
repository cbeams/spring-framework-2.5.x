package org.springframework.context.support;

import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.NoSuchMessageException;

/**
 * @author Juergen Hoeller
 * @since 03.02.2004
 */
public class ResourceBundleMessageSourceTests extends TestCase {

	protected void doTestMessageAccess(boolean reloadable, boolean fallbackToSystemLocale,
	                                   boolean expectGermanFallback, boolean useCodeAsDefaultMessage) {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		String basepath = "org/springframework/context/support/";
		String[] basenames = null;
		if (reloadable) {
			basenames = new String[] {"classpath:" + basepath + "messages",
																"classpath:" + basepath + "more-messages"};
		}
		else {
			basenames = new String[] {basepath + "messages",
																basepath + "more-messages"};
		}
		pvs.addPropertyValue("basenames", basenames);
		if (!fallbackToSystemLocale) {
			pvs.addPropertyValue("fallbackToSystemLocale", Boolean.FALSE);
		}
		if (useCodeAsDefaultMessage) {
			pvs.addPropertyValue("useCodeAsDefaultMessage", Boolean.TRUE);
		}
		Class clazz = reloadable ? ReloadableResourceBundleMessageSource.class : ResourceBundleMessageSource.class;
		ac.registerSingleton("messageSource", clazz, pvs);
		ac.refresh();

		Locale.setDefault(expectGermanFallback ? Locale.GERMAN : Locale.CANADA);
		assertEquals("message1", ac.getMessage("code1", null, Locale.ENGLISH));
		assertEquals(fallbackToSystemLocale && expectGermanFallback ? "nachricht2" : "message2",
		             ac.getMessage("code2", null, Locale.ENGLISH));
		assertEquals("nachricht2", ac.getMessage("code2", null, Locale.GERMAN));
		assertEquals("nochricht2", ac.getMessage("code2", null, new Locale("DE", "at")));
		assertEquals("noochricht2", ac.getMessage("code2", null, new Locale("DE", "at", "oo")));
		assertEquals("message3", ac.getMessage("code3", null, Locale.ENGLISH));

		try {
			assertEquals("code4", ac.getMessage("code4", null, Locale.GERMAN));
			if (!useCodeAsDefaultMessage) {
				fail("Should have thrown NoSuchMessageException");
			}
		}
		catch (NoSuchMessageException ex) {
			if (useCodeAsDefaultMessage) {
				fail("Should have returned code as default message");
			}
		}
	}

	public void testMessageAccessWithDefaultMessageSource() {
		doTestMessageAccess(false, true, false, false);
	}

	public void testMessageAccessWithDefaultMessageSourceAndFallbackToGerman() {
		doTestMessageAccess(false, true, true, true);
	}

	public void testMessageAccessWithReloadableMessageSource() {
		doTestMessageAccess(true, true, false, false);
	}

	public void testMessageAccessWithReloadableMessageSourceAndFallbackToGerman() {
		doTestMessageAccess(true, true, true, true);
	}

	public void testMessageAccessWithReloadableMessageSourceAndFallbackTurnedOff() {
		doTestMessageAccess(true, false, false, false);
	}

	public void testMessageAccessWithReloadableMessageSourceAndFallbackTurnedOffAndFallbackToGerman() {
		doTestMessageAccess(true, false, true, true);
	}

}
