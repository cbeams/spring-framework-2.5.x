/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NestingMessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * Abstract implementation of NestingMessageSource interface,
 * making it easy to implement custom MessageSources.
 * Subclasses must implement the abstract resolve() method.
 *
 * <p>This class does not implement caching, thus subclasses can
 * dynamically change messages over time.
 *
 * <p>Note: Some methods of this class are based on code from
 * Struts 1.1b3 implementation.
 * 
 * @author Rod Johnson
 * @see #resolve
 */
public abstract class AbstractNestingMessageSource implements NestingMessageSource {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent MessageSource */
	private MessageSource parent;

	/**
	 * The set of previously created MessageFormat objects, keyed by the
	 * key computed in <code>messageKey()</code>.
	 */
	private Map formats = new HashMap();

	public AbstractNestingMessageSource() {
	}

	public final void setParent(MessageSource parent) {
		this.parent = parent;
	}

	/**
	 * Try to resolve the message.Return default message if no message was found.
	 * @param code code to lookup up, such as 'calculator.noRateSet'
	 * @param locale Locale in which to do lookup
	 * @param args Array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message).
	 * @see <a href=http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html>java.text.MessageFormat</a>
	 * @param defaultMessage String to return if the lookup fails
	 * @return a resolved message if the lookup is successful;
	 * otherwise return the default message passed as a parameter
	 */
	public final String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		try {
			return getMessage(code, args, locale);
		}
		catch (NoSuchMessageException ex) {
			return defaultMessage;
		}
	}

	/**
	 * <b>Using all the attributes contained within the <code>MessageSourceResolvable</code>
	 * arg that was passed in (except for the <code>locale</code> attribute)</b>,
	 * try to resolve the message from the <code>MessageSource</code> contained within the <code>Context</code>.<p>
	 * <p>NOTE: We must throw a <code>NoSuchMessageException</code> on this method since
	 * at the time of calling this method we aren't able to determine if the <code>defaultMessage</code>
	 * attribute is null or not.
	 * @param resolvable Value object storing 4 attributes required to properly resolve a message.
	 * @param locale Locale to be used as the "driver" to figuring out what message to return.
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 * @return message Resolved message.
	 * @throws NoSuchMessageException not found in any locale
	 */
	public final String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		String[] codes = resolvable.getCodes();
		for (int i = 0; i < codes.length; i++) {
			try {
				return getMessage(codes[i], resolvable.getArgs(), locale);
			}
			catch (NoSuchMessageException ex) {
				// swallow it, we'll retry the other codes
			}
		}
		if (resolvable.getDefaultMessage() != null) {
			return resolvable.getDefaultMessage();
		}
		else {
			throw new NoSuchMessageException(codes[codes.length-1], locale);
		}
	}

	/**
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 * @param code code to lookup up, such as 'calculator.noRateSet'
	 * @param locale Locale in which to do lookup
	 * @param args Array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message).
	 * @see <a href=http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 * @return message
	 * @throws NoSuchMessageException not found in any locale
	 */
	public final String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		String mesg = resolve(code, locale);
		if (mesg == null) {
			if (this.parent != null) {
				mesg = this.parent.getMessage(code, args, locale);
			}
			else {
				logger.debug("Could not resolve message code [" + code + "] in locale [" + locale + "]");
				throw new NoSuchMessageException(code, locale);
			}
		}

		// Cache MessageFormat instances as they are accessed
		if (locale == null) {
			locale = Locale.getDefault();
		}
		MessageFormat format = null;
		String formatKey = messageKey(locale, code);
		synchronized (this.formats) {
			format = (MessageFormat) this.formats.get(formatKey);
			if (format == null) {
				format = new MessageFormat(mesg);
				this.formats.put(formatKey, format);
			}
		}
		return format.format(args);
	}

	/**
	 * Compute and return a key to be used in caching information
	 * by Locale and message key.
	 * @param locale The Locale for which this format key is calculated
	 * @param key The message key for which this format key is calculated
	 */
	protected String messageKey(Locale locale, String key) {
		return (localeKey(locale) + "." + key);
	}

	/**
	 * Compute and return a key to be used in caching information by a Locale.
	 * <strong>NOTE</strong>: The locale key for the default Locale in our
	 * environment is a zero length String.
	 * @param locale the locale for which a key is desired
	 */
	protected String localeKey(Locale locale) {
		if (locale == null || locale.equals(Locale.getDefault())) {
			return "";
		}
		else {
			return locale.toString();
		}
	}

	/**
	 * Subclasses must implement this method to resolve a message.
	 * @return the message, or null if not found
	 * @param code code of the message to resolve
	 * @param locale locale to resolve the code for.
	 * Subclasses are encouraged to support internationalization.
	 */
	protected abstract String resolve(String code, Locale locale);

}
