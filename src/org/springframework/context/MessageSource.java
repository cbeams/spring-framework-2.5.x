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

package org.springframework.context;

import java.util.Locale;

/**
 * Interface to be implemented by objects that can resolve messages.
 * This enables parameterization and internationalization of messages.
 *
 * <p>Spring provides two out-of-the-box implementations for production:
 * <ul>
 * <li><b>ResourceBundleMessageSource</b>, built on standard java.util.ResourceBundle
 * <li><b>ReloadableResourceBundleMessageSource</b>, being able to reload message
 * definitions without restarting the VM
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
public interface MessageSource {

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
	 * this class are encouraged to base message names on the relevant fully
	 * qualified class name, thus avoiding conflict and ensuring maximum clarity.
	 * @param args array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or null if none.
	 * @param locale the Locale in which to do the lookup
	 * @param defaultMessage String to return if the lookup fails
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	/**
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * @param args Array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or null if none.
	 * @param locale the Locale in which to do the lookup
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

	/**
	 * Try to resolve the message using all the attributes contained within the
	 * <code>MessageSourceResolvable</code> argument that was passed in.
	 * <p>NOTE: We must throw a <code>NoSuchMessageException</code> on this method
	 * since at the time of calling this method we aren't able to determine if the
	 * <code>defaultMessage</code> property of the resolvable is null or not.
	 * @param resolvable value object storing attributes required to properly resolve a message
	 * @param locale the Locale in which to do the lookup
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
