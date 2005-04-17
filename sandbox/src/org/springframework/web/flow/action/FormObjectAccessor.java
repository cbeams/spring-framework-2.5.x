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

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;

/**
 * Convenience wrapper that encapsulates logic on how to retrieve and expose
 * form objects and associated errors to and from a flow execution context.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FormObjectAccessor {

	/**
	 * The form object instance is aliased under this attribute name in the
	 * flow context by the default form setup and bind and validate actions.
	 */
	public static final String FORM_OBJECT_ALIAS = "formObject";

	private RequestContext context;

	/**
	 * Creates a form object accessor that wraps the given context.
	 * @param context the flow execution request context
	 */
	public FormObjectAccessor(RequestContext context) {
		this.context = context;
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #FORM_OBJECT_ALIAS}. Will try all scopes.
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the context
	 */
	public Object getFormObject() throws IllegalStateException {
		try {
			return getFormObject(ScopeType.REQUEST);
		}
		catch (IllegalStateException e) {
			return getFormObject(ScopeType.FLOW);
		}
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #FORM_OBJECT_ALIAS}.
	 * @param scope the scope to obtain the form object from
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the context
	 */
	public Object getFormObject(ScopeType scope) throws IllegalStateException {
		return getFormObject(FORM_OBJECT_ALIAS, scope);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param formObjectName the name of the form object in the context
	 * @param scope the scope to obtain the form object from
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in
	 *         the context
	 */
	public Object getFormObject(String formObjectName, ScopeType scope) throws IllegalStateException {
		return getScope(scope).getRequiredAttribute(formObjectName);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param formObjectName the name of the form in the context
	 * @param formObjectClass the class of the form object, which will be verified
	 * @param scope the scope to obtain the form object from
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the context
	 *         or is not of the required type
	 */
	public Object getFormObject(String formObjectName, Class formObjectClass, ScopeType scope)
			throws IllegalStateException {
		return getScope(scope).getRequiredAttribute(formObjectName, formObjectClass);
	}

	/**
	 * Expose given form object using given name in specified scope.
	 * @param formObject the form object
	 * @param formObjectName the name of the form object
	 * @param scope the scope in which to expose the form object
	 */
	public void exposeFormObject(Object formObject, String formObjectName, ScopeType scope) {
		getScope(scope).setAttribute(formObjectName, formObject);
		alias(formObject, scope);
	}

	/**
	 * Expose given form object using the well known alias
	 * {@link #FORM_OBJECT_ALIAS} in the specified scope.
	 * @param formObject the form object
	 * @param scope the scope in which to expose the form object
	 */
	private void alias(Object formObject, ScopeType scope) {
		getScope(scope).setAttribute(FORM_OBJECT_ALIAS, formObject);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the form object name {@link #FORM_OBJECT_ALIAS}.
	 * This method will search all scopes.
	 * @return the form object Errors tracker
	 * @throws IllegalStateException if the Errors instance is not found in the context
	 */
	public Errors getFormErrors() throws IllegalStateException {
		try {
			return (Errors)getFormErrors(ScopeType.REQUEST);
		}
		catch (IllegalStateException e) {
			return (Errors)getFormErrors(ScopeType.FLOW);
		}
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the form object name {@link #FORM_OBJECT_ALIAS}.
	 * @param scope the scope to obtain the errors from
	 * @return the form object Errors tracker
	 * @throws IllegalStateException if the Errors instance is not found in the context
	 */
	public Errors getFormErrors(ScopeType scope) throws IllegalStateException {
		return getFormErrors(FORM_OBJECT_ALIAS, scope);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the specified form object name.
	 * @param formObjectName the name of the Errors object, which will be
	 *        prefixed with {@link BindException#ERROR_KEY_PREFIX}
	 * @param scope the scope to obtain the errors from
	 * @return the form object errors instance
	 * @throws IllegalStateException if the Errors instance is not found in the context
	 */
	public Errors getFormErrors(String formObjectName, ScopeType scope) throws IllegalStateException {
		return (Errors)getScope(scope).getRequiredAttribute(BindException.ERROR_KEY_PREFIX + formObjectName,
				Errors.class);
	}

	/**
	 * Expose a <i>new</i> errors instance in the specified scope for given form
	 * object with the specified form object name.
	 * @param formObject the form object
	 * @param formObjectName the name of the form object
	 * @param scope the scope to expose the errors in
	 */
	public void exposeErrors(Object formObject, String formObjectName, ScopeType scope) {
		Errors errors = new BindException(formObject, formObjectName);
		exposeErrors(errors, scope);
	}

	/**
	 * Expose given errors instance in the specified scope.
	 * @param errors the errors object
	 * @param scope the scope to expose the errors in
	 */
	public void exposeErrors(Errors errors, ScopeType scope) {
		getScope(scope).setAttribute(BindException.ERROR_KEY_PREFIX + errors.getObjectName(), errors);
		alias(errors, scope);
	}

	/**
	 * Expose a <i>new</i> errors instance in the specified scope for given
	 * form object using the well-known alias {@link #FORM_OBJECT_ALIAS}.
	 * @param formObject the form object to expose an errors instance for
	 * @param scope the scope to expose the errors in
	 */
	private void alias(Errors errors, ScopeType scope) {
		getScope(scope).setAttribute(BindException.ERROR_KEY_PREFIX + FORM_OBJECT_ALIAS, errors);
	}

	/**
	 * Helper method to get the indicated scope from the flow execution request context.
	 */
	private Scope getScope(ScopeType scope) {
		return scope == ScopeType.FLOW ? this.context.getFlowScope() : this.context.getRequestScope();
	}
}