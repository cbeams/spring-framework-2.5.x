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
 * dynamically change messages over time.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Seth Ladd
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
			throw new NoSuchMessageException(codes[codes.length - 1], locale);
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
		MessageFormat messageFormat = resolveCode(code, locale);
		if (messageFormat != null) {
			return messageFormat.format(resolveArguments(args, locale));
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
	 * Search through the given array of objects, find any
	 * MessageSourceResolvable objects and resolve them.
	 * <p>Allows for messages to have MessageSourceResolvables as arguments.
	 * @param args array of arguments for a message
	 * @param locale the locale to resolve through
	 * @return an array of arguments with any MessageSourceResolvables resolved
	 */
	private Object[] resolveArguments(Object[] args, Locale locale) {
		if (args == null) {
			return new Object[0];
		}
		List resolvedArgs = new ArrayList();
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof MessageSourceResolvable) {
				resolvedArgs.add(getMessage((MessageSourceResolvable) args[i],
				                            locale));
			}
			else {
				resolvedArgs.add(args[i]);
			}
		}
		return resolvedArgs.toArray(new Object[resolvedArgs.size()]);
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
