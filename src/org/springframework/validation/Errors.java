/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.validation;

import java.util.List;

import org.springframework.beans.PropertyAccessor;

/**
 * Interface to be implemented by objects that can store and expose
 * information about data binding errors for a specific object.
 *
 * <p>Field names can be properties of the target object (e.g. "name"
 * when binding to a customer object), or nested fields in case of
 * subobjects (e.g. "address.street"). Supports subtree navigation
 * via setNestedPath, e.g. an AddressValidator validates "address",
 * not being aware that this is a subobject of customer.
 *
 * <p>Note: Errors objects are single-threaded.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setNestedPath
 * @see BindException
 * @see DataBinder
 * @see ValidationUtils
 */
public interface Errors {

	/**
	 * Separator between path elements in a nested path,
	 * e.g. in "customer.name" or "customer.address.street".
	 * "." = same as the nested property separator in the beans package.
	 * @see org.springframework.beans.PropertyAccessor#NESTED_PROPERTY_SEPARATOR
	 */
	String NESTED_PATH_SEPARATOR = PropertyAccessor.NESTED_PROPERTY_SEPARATOR;


	/**
	 * Return the name of the bound root object.
	 */
	String getObjectName();

	/**
	 * Allow context to be changed so that standard validators can validate
	 * subtrees. Reject calls prepend the given path to the field names.
	 * <p>For example, an address validator could validate the subobject
	 * "address" of a customer object.
	 * @param nestedPath nested path within this object,
	 * e.g. "address" (defaults to "", <code>null</code> is also acceptable).
	 * Can end with a dot: both "address" and "address." are valid.
	 */
	void setNestedPath(String nestedPath);

	/**
	 * Return the current nested path of this Errors object.
	 * <p>Returns a nested path with a dot, i.e. "address.", for easy
	 * building of concatenated paths. Default is an empty String.
	 */
	String getNestedPath();

	/**
	 * Push the given sub path onto the nested path stack.
	 * A popNestedStack call will reset the original nested path
	 * before the corresponding pushNestedPath call.
	 * <p>Using the nested path stack allows to set temporary nested paths
	 * for subobjects without having to worry about a temporary path holder.
	 * <p>For example: current path "spouse.", pushNestedPath("child") ->
	 * result path "spouse.child."; popNestedPath() -> "spouse." again
	 * @param subPath the sub path to push onto the nested path stack
	 * @see #popNestedPath
	 */
	void pushNestedPath(String subPath);

	/**
	 * Pop the former nested path from the nested path stack.
	 * @throws IllegalStateException if there is no former nested path on the stack
	 * @see #pushNestedPath
	 */
	void popNestedPath() throws IllegalStateException;


	/**
	 * Register a global error for the entire target object,
	 * using the given error description.
	 * @param errorCode error code, interpretable as message key
	 */
	void reject(String errorCode);

	/**
	 * Register a global error for the entire target object,
	 * using the given error description.
	 * @param errorCode error code, interpretable as message key
	 * @param defaultMessage fallback default message
	 */
	void reject(String errorCode, String defaultMessage);

	/**
	 * Register a global error for the entire target object,
	 * using the given error description.
	 * @param errorCode error code, interpretable as message key
	 * @param errorArgs error arguments, for argument binding via MessageFormat
	 * (can be <code>null</code>)
	 * @param defaultMessage fallback default message
	 */
	void reject(String errorCode, Object[] errorArgs, String defaultMessage);

	/**
	 * Register a field error for the specified field of the current object
	 * (respecting the current nested path, if any), using the given error
	 * description.
	 * <p>The field name may be <code>null</code> or empty String to indicate
	 * the current object itself rather than a field of it. This may result
	 * in a corresponding field error within the nested object graph or a
	 * global error if the current object is the top object.
	 * @param field the field name (may be <code>null</code> or empty String)
	 * @param errorCode error code, interpretable as message key
	 * @see #getNestedPath()
	 */
	void rejectValue(String field, String errorCode);

	/**
	 * Register a field error for the specified field of the current object
	 * (respecting the current nested path, if any), using the given error
	 * description.
	 * <p>The field name may be <code>null</code> or empty String to indicate
	 * the current object itself rather than a field of it. This may result
	 * in a corresponding field error within the nested object graph or a
	 * global error if the current object is the top object.
	 * @param field the field name (may be <code>null</code> or empty String)
	 * @param errorCode error code, interpretable as message key
	 * @param defaultMessage fallback default message
	 * @see #getNestedPath()
	 */
	void rejectValue(String field, String errorCode, String defaultMessage);

	/**
	 * Register a field error for the specified field of the current object
	 * (respecting the current nested path, if any), using the given error
	 * description.
	 * <p>The field name may be <code>null</code> or empty String to indicate
	 * the current object itself rather than a field of it. This may result
	 * in a corresponding field error within the nested object graph or a
	 * global error if the current object is the top object.
	 * @param field the field name (may be <code>null</code> or empty String)
	 * @param errorCode error code, interpretable as message key
	 * @param errorArgs error arguments, for argument binding via MessageFormat
	 * (can be <code>null</code>)
	 * @param defaultMessage fallback default message
	 * @see #getNestedPath()
	 */
	void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage);

	/**
	 * Add all errors from the given <code>Errors</code> instance to this
	 * <code>Errors</code> instance. Convenience method to avoid repeated
	 * <code>reject</code> calls for merging an <code>Errors</code> instance
	 * into another <code>Errors</code> instance.
	 * <p>Note that the passed-in <code>Errors</code> instance is supposed
	 * to refer to the same target object, or at least contain compatible errors
	 * that apply to the target object of this <code>Errors</code> instance.
	 * @param errors the <code>Errors</code> instance to merge in
	 */
	void addAllErrors(Errors errors);


	/**
	 * Return if there were any errors.
	 */
	boolean hasErrors();

	/**
	 * Return the total number of errors.
	 */
	int getErrorCount();

	/**
	 * Get all errors, both global and field ones.
	 * @return List of ObjectError instances
	 */
	List getAllErrors();

	/**
	 * Return if there were any global (i.e. not field-specific) errors.
	 */
	boolean hasGlobalErrors();

	/**
	 * Return the number of global (i.e. not field-specific) errors.
	 */
	int getGlobalErrorCount();

	/**
	 * Get all global errors.
	 * @return List of ObjectError instances
	 */
	List getGlobalErrors();

	/**
	 * Get the first global error, if any.
	 * @return the global error, or <code>null</code>
	 */
	ObjectError getGlobalError();

	/**
	 * Return if there are any errors associated with a field.
	 * @return if there were any errors associated with a field
	 */
	boolean hasFieldErrors();

	/**
	 * Return the number of errors associated with a field.
	 * @return the number of errors associated with a field
	 */
	int getFieldErrorCount();

	/**
	 * Get all errors associated with a field.
	 * @return List of FieldError instances
	 */
	List getFieldErrors();

	/**
	 * Get the first error associated with a field, if any.
	 * @return the field-specific error, or <code>null</code>
	 */
	FieldError getFieldError();

	/**
	 * Return if there are any errors associated with the given field.
	 * @param field the field name
	 * @return if there were any errors associated with the given field
	 */
	boolean hasFieldErrors(String field);

	/**
	 * Return the number of errors associated with the given field.
	 * @param field the field name
	 * @return the number of errors associated with the given field
	 */
	int getFieldErrorCount(String field);

	/**
	 * Get all errors associated with the given field.
	 * <p>Should support full field names like "name" but also pattern
	 * matches like "na*" or "address.*".
	 * @param field the field name
	 * @return List of FieldError instances
	 */
	List getFieldErrors(String field);

	/**
	 * Get the first error associated with the given field, if any.
	 * @param field the field name
	 * @return the field-specific error, or <code>null</code>
	 */
	FieldError getFieldError(String field);


	/**
	 * Return the current value of the given field, either the current
	 * bean property value or a rejected update from the last binding.
	 * <p>Allows for convenient access to user-specified field values,
	 * even if there were type mismatches.
	 * @param field the field name
	 * @return the current value of the given field
	 */
	Object getFieldValue(String field);

	/**
	 * Return the type of a given field.
	 * <p>Should be able to determine the type even when the field
	 * value is <code>null</code>, for example from some descriptor.
	 * @param field the field name
	 * @return the type of the field, or <code>null</code> if not determinable
	 */
	Class getFieldType(String field);

}
