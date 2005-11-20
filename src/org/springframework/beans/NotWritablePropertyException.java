/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans;

import java.util.Arrays;

import org.springframework.util.ObjectUtils;

/**
 * Exception thrown on an attempt to set the value of a property
 * that isn't writable, because there's no setter method. In some
 * situations alternatives are presented.
 *
 * @author Rod Johnson
 * @author Alef Arendsen
 * @author Arjen Poutsma
 */
public class NotWritablePropertyException extends InvalidPropertyException {

	private String[] possibleMatches = null;
	
	public NotWritablePropertyException(Class beanClass, String propertyName, String[] possibleMatches) {
		super(beanClass, propertyName, generateMessage(beanClass, propertyName, possibleMatches));
		this.possibleMatches = possibleMatches;
	}

	/**
	 * Create a new NotWritablePropertyException.
	 * @param beanClass the offending bean class
	 * @param propertyName the offending property
	 * @param msg the detail message
	 * @param ex the root cause
	 */
	public NotWritablePropertyException(Class beanClass, String propertyName, String msg, Throwable ex) {
		super(beanClass, propertyName, msg, ex);
	}
	
	private static String generateMessage(Class beanClass, String propertyName, String[] possibleMatches) {		
		StringBuffer buffy = new StringBuffer();
		buffy.append("Bean property '");
		buffy.append(propertyName);
		buffy.append("' is not writable or has an invalid setter method. ");
		
		if (ObjectUtils.isEmpty(possibleMatches)) {
			buffy.append("Does the parameter type of the setter match the return type of the getter?");
		} else {
			buffy.append("Did you mean ");
			for (int i = 0; i < possibleMatches.length; i++) {
				buffy.append('\'');
				buffy.append(possibleMatches[i]);
				
				if (i < possibleMatches.length - 2) {
					buffy.append("', ");
				} else if (i == possibleMatches.length - 2){
					buffy.append("', or ");
				}			
	 		}
			buffy.append("'?");
		}
		return buffy.toString();
	}

	public String[] getPossibleMatches() {
		return possibleMatches;
	}
	

}
