package org.springframework.context.support;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * Helper class that allows for accessing a MessageSource as a ResourceBundle.
 * Used for example to expose a Spring MessageSource to JSTL web views.
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see org.springframework.context.MessageSource
 * @see java.util.ResourceBundle
 * @see org.springframework.web.servlet.support.JstlUtils#exposeLocalizationContext
 */
public class MessageSourceResourceBundle extends ResourceBundle {

	private final MessageSource source;

	private final Locale locale;

	public MessageSourceResourceBundle(MessageSource source, Locale locale) {
		this.source = source;
		this.locale = locale;
	}

	/**
	 * This implementation resolves the code in the MessageSource.
	 * Returns null if the message could not be resolved.
	 */
	protected Object handleGetObject(String code) {
		try {
			return this.source.getMessage(code, null, this.locale);
		}
		catch (NoSuchMessageException ex) {
			return null;
		}
	}

	/**
	 * This implementation returns null, as a MessageSource does
	 * not allow for enumerating the defined message codes.
	 */
	public Enumeration getKeys() {
		return null;
	}

}
