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
package org.springframework.web.flow.action;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown if a form object could not be retrieved via its identifier.
 * Provides information about the form object class and the identifier.
 * 
 * @see org.springframework.web.flow.action.FormAction
 * 
 * @author Keith Donald 
 * @author Juergen Hoeller
 * @author Erwin Vervaet
 */
public class FormObjectRetrievalFailureException extends NestedRuntimeException {

	private String formObjectName;

	private Object formObjectClass;

	/**
	 * Create a new FormObjectRetrievalFailureException for the given object,
	 * with the default "not found" message.
	 * @param formObjectClass the persistent class
	 * @param formObjectName the id of the object that should have been retrieved
	 */
	public FormObjectRetrievalFailureException(Class formObjectClass, String formObjectName) {
		this(formObjectClass, formObjectName, "Form object of class [" + formObjectClass.getName() + "] with name ["
				+ formObjectName + "]: not found", null);
	}

	/**
	 * Create a new ObjectRetrievalFailureException for the given object,
	 * with the given explicit message and exception.
	 * @param formObjectClass the persistent class
	 * @param formObjectName the id of the object that should have been retrieved
	 * @param msg exception message
	 * @param cause source exception
	 */
	public FormObjectRetrievalFailureException(Class formObjectClass, String formObjectName, String msg, Throwable cause) {
		super(msg, cause);
		this.formObjectClass = formObjectClass;
		this.formObjectName = formObjectName;
	}

	/**
	 * Create a new FormObjectRetrievalFailureException for the given object,
	 * with the default "not found" message.
	 * @param formObjectClassName the name of the persistent class
	 * @param formObjectName the ID of the object that should have been retrieved
	 */
	public FormObjectRetrievalFailureException(String formObjectClassName, String formObjectName) {
		this(formObjectClassName, formObjectName, "Form object of class [" + formObjectClassName + "] with name ["
				+ formObjectName + "]: not found", null);
	}

	/**
	 * Create a new FormObjectRetrievalFailureException for the given object,
	 * with the given explicit message and exception.
	 * @param formObjectClassName the name of the persistent class
	 * @param formObjectName the ID of the object that should have been retrieved
	 * @param msg exception message
	 * @param cause source exception
	 */
	public FormObjectRetrievalFailureException(String formObjectClassName, String formObjectName, String msg,
			Throwable cause) {
		super(msg, cause);
		this.formObjectClass = formObjectClassName;
		this.formObjectName = formObjectName;
	}

	/**
	 * Return the persistent class of the object that was not found.
	 * If no Class was specified, this method returns null.
	 */
	public Class getFormObjectClass() {
		return (this.formObjectClass instanceof Class ? (Class)this.formObjectClass : null);
	}

	/**
	 * Return the name of the class of the object that was not found.
	 * Will work for both Class objects and String names.
	 */
	public String getFormObjectClassName() {
		return (this.formObjectClass instanceof Class ?
				((Class)this.formObjectClass).getName() : this.formObjectClass.toString());
	}

	/**
	 * Return the name of the object that was not found.
	 */
	public Object getFormObjectName() {
		return formObjectName;
	}
}