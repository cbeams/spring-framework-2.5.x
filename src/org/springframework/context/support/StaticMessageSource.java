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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simple implementation of MessageSource that allows messages
 * to be held in a Java object, and added programmatically.
 * This MessageSource supports internationalization.
 *
 * <p>Intended for testing rather than use in production systems.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class StaticMessageSource extends AbstractMessageSource {

	private final Map messages = new HashMap();

	protected MessageFormat resolveCode(String code, Locale locale) {
		return (MessageFormat) this.messages.get(code + "_" + locale.toString());
	}

	/**
	 * Associate the given message with the given code.
	 * @param code lookup code
   * @param locale locale message should be found within
	 * @param message message associated with this lookup code
	 */
	public void addMessage(String code, Locale locale, String message) {
		this.messages.put(code + "_" + locale.toString(), new MessageFormat(message));
		logger.info("Added message [" + message + "] for code [" + code + "] and Locale [" + locale + "]");
	}

	public String toString() {
		return getClass().getName() + ": " + this.messages;
	}

}

