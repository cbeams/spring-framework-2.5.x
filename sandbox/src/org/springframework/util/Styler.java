/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.Converter;
import org.springframework.binding.convert.support.ObjectToStringConverter;
import org.springframework.binding.format.support.ThreadLocalFormatterLocator;

/**
 * Strategy that encapsulates value string styling algorithms according to
 * Spring conventions.
 * 
 * @author Keith Donald
 */
public class Styler {

	private static Styler INSTANCE;
	static {
		Converter converter = new ObjectToStringConverter(new ThreadLocalFormatterLocator());
		INSTANCE = new Styler(new ConversionExecutor(converter, String.class));
	}

	private ConversionExecutor toStringExecutor;

	public Styler(ConversionExecutor toStringExecutor) {
		this.toStringExecutor = toStringExecutor;
	}

	public static void load(Styler styler) {
		INSTANCE = styler;
	}

	/**
	 * Return the default object styler singleton instance, for convenient
	 * access.
	 * 
	 * @return The default object styler
	 */
	public static Styler instance() {
		Assert.notNull(INSTANCE, "The global string conversion executor instance has not been initialized");
		return INSTANCE;
	}

	/**
	 * Static accessor that calls the default styler's style method.
	 * 
	 * @param o the argument to style
	 * @return The styled string.
	 */
	public static String call(Object o) {
		return (String)INSTANCE.style(o);
	}

	/**
	 * Styles the string form of this object.
	 * 
	 * @param o The object to be styled.
	 * @return The styled string.
	 */
	public String style(Object source) {
		if (source == null) {
			return "[null]";
		}
		return (String)this.toStringExecutor.call(source);
	}
}