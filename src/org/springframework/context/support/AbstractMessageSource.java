/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
 * Subclasses must implement the abstract resolveCode method.
 *
 * <p>Supports not only MessageSourceResolvables as primary messages
 * but also resolution of message arguments that are in turn
 * MessageSourceResolvables themselves.
 *
 * <p>This class does not implement caching, thus subclasses can
 * dynamically change messages over time. Subclasses are encouraged
 * to cache their messages in a modification-aware fashion.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #resolveCode
 */
public abstract class AbstractMessageSource implements HierarchicalMessageSource {

	protected final Log logger = LogFactory.getLog(getClass());

	private MessageSource parentMessageSource;

	private boolean useCodeAsDefaultMessage = false;


	public void setParentMessageSource(MessageSource parent) {
		this.parentMessageSource = parent;
	}

	public MessageSource getParentMessageSource() {
		return parentMessageSource;
	}

	/**
	 * Set whether to use the message code as default message instead of
	 * throwing a NoSuchMessageException. Useful for development and debugging.
	 * Default is false.
	 * <p>Note: In case of a MessageSourceResolvable with multiple codes
	 * (like a FieldError) and a MessageSource that has a parent MessageSource,
	 * do <i>not</i> activate "useCodeAsDefaultMessage" in the <i>parent</i>:
	 * Else, you'll get the first code returned as message by the parent,
	 * without attempts to check further codes.
	 * <p>To be able to work with "useCodeAsDefaultMessage" turned on in the parent,
	 * AbstractMessageSource and AbstractApplicationContext contain special checks
	 * to delegate to the internal <code>getMessageInternal</code> method if available.
	 * In general, it is recommended to just use "useCodeAsDefaultMessage" during
	 * development and not rely on it in production in the first place, though.
	 * @see #getMessage(String, Object[], Locale)
	 * @see #getMessageInternal
	 * @see org.springframework.validation.FieldError
	 */
	public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
		this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
	}


	public final String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}
		if (defaultMessage == null && this.useCodeAsDefaultMessage) {
			return code;
		}
		return defaultMessage;
	}

	public final String getMessage(String code, Object[] args, Locale locale)
	    throws NoSuchMessageException {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}
		if (this.useCodeAsDefaultMessage) {
			return code;
		}
		throw new NoSuchMessageException(code, locale);
	}

	public final String getMessage(MessageSourceResolvable resolvable, Locale locale)
	    throws NoSuchMessageException {
		String[] codes = resolvable.getCodes();
		if (codes == null) {
			throw new NoSuchMessageException(null, locale);
		}
		for (int i = 0; i < codes.length; i++) {
			String msg = getMessageInternal(codes[i], resolvable.getArguments(), locale);
			if (msg != null) {
				return msg;
			}
		}
		if (resolvable.getDefaultMessage() != null) {
			return resolvable.getDefaultMessage();
		}
		if (this.useCodeAsDefaultMessage && codes.length > 0) {
			return codes[0];
		}
		throw new NoSuchMessageException(codes.length > 0 ? codes[codes.length - 1] : null, locale);
	}

	/**
	 * Resolve the given code and arguments as message in the given Locale,
	 * returning null if not found. Does <i>not</i> fall back to the code
	 * as default message. Invoked by getMessage methods.
	 * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
	 * this class are encouraged to base message names on the relevant fully
	 * qualified class name, thus avoiding conflict and ensuring maximum clarity.
	 * @param args array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or null if none.
	 * @param locale the Locale in which to do the lookup
	 * @return the resolved message, or null if not found
	 * @see #getMessage(String, Object[], String, Locale)
	 * @see #getMessage(String, Object[], Locale)
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see #setUseCodeAsDefaultMessage
	 */
	protected String getMessageInternal(String code, Object[] args, Locale locale) {
		if (code == null) {
			return null;
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}
		MessageFormat messageFormat = resolveCode(code, locale);
		if (messageFormat != null) {
			return messageFormat.format(resolveArguments(args, locale));
		}
		else if (this.parentMessageSource != null) {
			if (this.parentMessageSource instanceof AbstractMessageSource) {
				// Call internal method to avoid getting the default code back
				// in case of "useCodeAsDefaultMessage" being activated.
				return ((AbstractMessageSource) this.parentMessageSource).getMessageInternal(code, args, locale);
			}
			else {
				// Check parent MessageSource, returning null if not found there.
				return this.parentMessageSource.getMessage(code, args, null, locale);
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Search through the given array of objects, find any
	 * MessageSourceResolvable objects and resolve them.
	 * <p>Allows for messages to have MessageSourceResolvables as arguments.
	 * @param args array of arguments for a message
	 * @param locale the locale to resolve through
	 * @return an array of arguments with any MessageSourceResolvables resolved
	 */
	protected Object[] resolveArguments(Object[] args, Locale locale) {
		if (args == null) {
			return new Object[0];
		}
		List resolvedArgs = new ArrayList(args.length);
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof MessageSourceResolvable) {
				resolvedArgs.add(getMessage((MessageSourceResolvable) args[i], locale));
			}
			else {
				resolvedArgs.add(args[i]);
			}
		}
		return resolvedArgs.toArray(new Object[resolvedArgs.size()]);
	}

	/**
	 * Create a MessageFormat for the given message and Locale.
	 * <p>This implementation creates an empty MessageFormat first,
	 * populating it with Locale and pattern afterwards, to stay
	 * compatible with J2SE 1.3.
	 * @param msg the message to create a MessageFormat for
	 * @param locale the Locale to create a MessageFormat for
	 * @return the MessageFormat instance
	 */
	protected MessageFormat createMessageFormat(String msg, Locale locale) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating MessageFormat for pattern [" + msg + "] and locale '" + locale + "'");
		}
		MessageFormat messageFormat = new MessageFormat("");
		messageFormat.setLocale(locale);
		messageFormat.applyPattern(msg);
		return messageFormat;
	}


	/**
	 * Subclasses must implement this method to resolve a message.
	 * <p>Returns a MessageFormat instance rather than a message String,
	 * to allow for appropriate caching of MessageFormats in subclasses.
	 * @param code the code of the message to resolve
	 * @param locale the Locale to resolve the code for
	 * (subclasses are encouraged to support internationalization)
	 * @return the MessageFormat for the message, or null if not found
	 */
	protected abstract MessageFormat resolveCode(String code, Locale locale);

}
