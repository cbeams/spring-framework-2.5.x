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
