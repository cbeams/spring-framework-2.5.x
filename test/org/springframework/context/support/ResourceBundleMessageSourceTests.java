package org.springframework.context.support;

import java.util.Locale;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.MessageSourceResolvable;
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
		MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(new String[]{"code4", "code3"}, null);
		assertEquals("message3", ac.getMessage(resolvable, Locale.ENGLISH));

		assertEquals("message3", ac.getMessage("code3", null, Locale.ENGLISH));
		resolvable = new DefaultMessageSourceResolvable(new String[]{"code4", "code3"}, null);
		assertEquals("message3", ac.getMessage(resolvable, Locale.ENGLISH));

		Object[] args = new Object[]{"Hello", new DefaultMessageSourceResolvable(new String[]{"code1"}, null)};
		assertEquals("Hello, message1", ac.getMessage("hello", args, Locale.ENGLISH));

		// test null message args
		assertEquals("{0}, {1}", ac.getMessage("hello", null, Locale.ENGLISH));

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

	public void testResourceBundleMessageSourceStandalone() {
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasename("org/springframework/context/support/messages");
		assertEquals("message1",  ms.getMessage("code1", null, Locale.ENGLISH));
	}

	public void testReloadableResourceBundleMessageSourceStandalone() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("org/springframework/context/support/messages");
		assertEquals("message1",  ms.getMessage("code1", null, Locale.ENGLISH));
	}

	public void testReloadableResourceBundleMessageSourceWithDefaultCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("org/springframework/context/support/messages");
		ms.setDefaultCharset("ISO-8859-1");
		assertEquals("message1",  ms.getMessage("code1", null, Locale.ENGLISH));
	}

	public void testReloadableResourceBundleMessageSourceWithInappropriateDefaultCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("org/springframework/context/support/messages");
		ms.setDefaultCharset("unicode");
		Properties fileCharsets = new Properties();
		fileCharsets.setProperty("org/springframework/context/support/messages_de", "unicode");
		ms.setFileCharsets(fileCharsets);
		ms.setFallbackToSystemLocale(false);
		try {
			ms.getMessage("code1", null, Locale.ENGLISH);
			fail("Should have thrown NoSuchMessageException");
		}
		catch (NoSuchMessageException ex) {
			// expected
		}
	}

	public void testReloadableResourceBundleMessageSourceWithInappropriateEnglishCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("org/springframework/context/support/messages");
		ms.setFallbackToSystemLocale(false);
		Properties fileCharsets = new Properties();
		fileCharsets.setProperty("org/springframework/context/support/messages", "unicode");
		ms.setFileCharsets(fileCharsets);
		try {
			ms.getMessage("code1", null, Locale.ENGLISH);
			fail("Should have thrown NoSuchMessageException");
		}
		catch (NoSuchMessageException ex) {
			// expected
		}
	}

	public void testReloadableResourceBundleMessageSourceWithInappropriateGermanCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("org/springframework/context/support/messages");
		ms.setFallbackToSystemLocale(false);
		Properties fileCharsets = new Properties();
		fileCharsets.setProperty("org/springframework/context/support/messages_de", "unicode");
		ms.setFileCharsets(fileCharsets);
		assertEquals("message1",  ms.getMessage("code1", null, Locale.ENGLISH));
	}

}
