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

import java.io.Serializable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the MessageSourceResolvable interface.
 * Easy way to store all the necessary values needed to resolve
 * a message via a MessageSource.
 * @author Juergen Hoeller
 * @since 13.02.2004
 * @see org.springframework.context.MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 */
public class DefaultMessageSourceResolvable implements MessageSourceResolvable, Serializable {

	private final String[] codes;

	private final Object[] arguments;

	private final String defaultMessage;


	/**
	 * Create a new DefaultMessageSourceResolvable.
	 * @param codes the codes to be used to resolve this message
	 */
	public DefaultMessageSourceResolvable(String[] codes) {
		this(codes, null, null);
	}

	/**
	 * Create a new DefaultMessageSourceResolvable.
	 * @param codes the codes to be used to resolve this message
	 * @param arguments the array of arguments to be used to resolve this message
	 */
	public DefaultMessageSourceResolvable(String[] codes, Object[] arguments) {
		this(codes, arguments, null);
	}

	/**
	 * Create a new DefaultMessageSourceResolvable.
	 * @param codes the codes to be used to resolve this message
	 * @param arguments the array of arguments to be used to resolve this message
	 * @param defaultMessage the default message to be used to resolve this message
	 */
	public DefaultMessageSourceResolvable(String[] codes, Object[] arguments, String defaultMessage) {
		this.codes = codes;
		this.arguments = arguments;
		this.defaultMessage = defaultMessage;
	}

	/**
	 * Copy constructor: Create a new instance from another resolvable.
	 * @param resolvable the resolvable to copy from
	 */
	public DefaultMessageSourceResolvable(MessageSourceResolvable resolvable) {
		this(resolvable.getCodes(), resolvable.getArguments(), resolvable.getDefaultMessage());
	}


	public String[] getCodes() {
		return codes;
	}

	/**
	 * Return the default code of this resolvable, i.e. the last one in the
	 * codes array.
	 */
	public String getCode() {
		return (this.codes != null && this.codes.length > 0) ? this.codes[this.codes.length - 1] : null;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	protected String resolvableToString() {
		StringBuffer buf = new StringBuffer();
		buf.append("codes=[").append(StringUtils.arrayToDelimitedString(getCodes(), ",")).append("]; arguments=[");
		if (this.arguments == null) {
			buf.append("null");
		}
		else {
			for (int i = 0; i < getArguments().length; i++) {
				buf.append('(').append(getArguments()[i].getClass().getName()).append(")[");
				buf.append(getArguments()[i]).append(']');
				if (i < getArguments().length - 1) {
					buf.append(", ");
				}
			}
		}
		buf.append("]; defaultMessage=[").append(getDefaultMessage()).append(']');
		return buf.toString();
	}

	public String toString() {
		return resolvableToString();
	}

}
