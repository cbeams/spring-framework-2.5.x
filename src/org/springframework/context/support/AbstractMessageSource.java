/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Abstract implementation of HierarchicalMessageSource interface,
 * making it easy to implement a custom MessageSource.
 * Subclasses must implement the abstract resolve method.
 *
 * <p>This class does not implement caching, thus subclasses can
 * dynamically change messages over time.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #resolve
 */
public abstract class AbstractMessageSource implements HierarchicalMessageSource {

	protected final Log logger = LogFactory.getLog(getClass());

	private MessageSource parentMessageSource;

	private boolean useCodeAsDefaultMessage = false;

	public final void setParentMessageSource(MessageSource parent) {
		this.parentMessageSource = parent;
	}

	public final MessageSource getParentMessageSource() {
		return parentMessageSource;
	}

	/**
	 * Set whether to use the message code as default message
	 * instead of throwing a NoSuchMessageException.
	 * Useful for development and debugging. Default is false.
	 * @see #getMessage(String, Object[], Locale)
	 */
	public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
		this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
	}

	public final String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		try {
			return getMessage(code, args, locale);
		}
		catch (NoSuchMessageException ex) {
			return defaultMessage;
		}
	}

	public final String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		try {
			return getMessageInternal(code, args, locale);
		}
		catch (NoSuchMessageException ex) {
			if (this.useCodeAsDefaultMessage) {
				return code;
			}
			else {
				throw ex;
			}
		}
	}

	public final String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		String[] codes = resolvable.getCodes();
		for (int i = 0; i < codes.length; i++) {
			try {
				return getMessageInternal(codes[i], resolvable.getArguments(), locale);
			}
			catch (NoSuchMessageException ex) {
				// swallow it, we'll retry the other codes
			}
		}
		if (resolvable.getDefaultMessage() != null) {
			return resolvable.getDefaultMessage();
		}
		else if (this.useCodeAsDefaultMessage && codes.length > 0) {
			return codes[0];
		}
		else {
			throw new NoSuchMessageException(codes[codes.length-1], locale);
		}
	}

	/**
	 * Resolve the given code and arguments as message in the given Locale,
	 * throwing a NoSuchMessageException if not found. Does <i>not</i> fall
	 * back to the code as default message. Invoked by getMessage methods.
	 * @see #getMessage(String, Object[], Locale)
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see #setUseCodeAsDefaultMessage
	 */
	protected String getMessageInternal(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		if (locale == null) {
			locale = Locale.getDefault();
		}
		MessageFormat messageFormat = resolve(code, locale);
		if (messageFormat != null) {
			return messageFormat.format(args);
		}
		else {
			if (this.parentMessageSource != null) {
				return this.parentMessageSource.getMessage(code, args, locale);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve message code [" + code + "] in locale [" + locale + "]");
				}
				throw new NoSuchMessageException(code, locale);
			}
		}
	}

	/**
	 * Subclasses must implement this method to resolve a message.
	 * <p>Returns a MessageFormat instance rather than a message String,
	 * to allow for appropriate caching of MessageFormats in subclasses.
	 * @param code the code of the message to resolve
	 * @param locale the Locale to resolve the code for.
	 * Subclasses are encouraged to support internationalization.
	 * @return the MessageFormat for the message, or null if not found
	 */
	protected abstract MessageFormat resolve(String code, Locale locale);

}
