/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.validation;

import java.beans.PropertyEditor;
import java.util.Map;

import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.PropertyAccessExceptionsException;

/**
 * Binder that allows for binding property values to a target object.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: DataBinder.java,v 1.7 2004-02-02 11:22:54 jhoeller Exp $
 */
public class DataBinder {

	public static final String MISSING_FIELD_ERROR_CODE = "required";

	private BindException errors;

	private String[] requiredFields;

	/**
	 * Create a new DataBinder instance.
	 * @param target target object to bind onto
	 * @param name name of the target object
	 */
	public DataBinder(Object target, String name) {
		this.errors = createErrors(target, name);
	}

	/**
	 * Create a new Errors instance for this data binder.
	 * Can be overridden in subclasses to.
	 * Needs to be a subclass of BindException.
	 * @param target target object to bind onto
	 * @param name name of the target object
	 * @return the Errors instance
	 * @see #close
	 */
	protected BindException createErrors(Object target, String name) {
		return new BindException(target, name);
	}

	/**
	 * Return the Errors instance for this data binder.
	 * @return the Errors instance, to be treated as Errors or as BindException
	 * @see Errors
	 */
	public BindException getErrors() {
		return errors;
	}

	/**
	 * Register fields that are required for each binding process.
	 * @param requiredFields array of field names
	 */
	public void setRequiredFields(String[] requiredFields) {
		this.requiredFields = requiredFields;
	}

	/**
	 * Return the fields that are required for each binding process.
	 * @return array of field names
	 */
	protected String[] getRequiredFields() {
		return requiredFields;
	}

	/**
	 * Register the given custom property editor for all properties
	 * of the given type.
	 * @param requiredType type of the property
	 * @param propertyEditor editor to register
	 */
	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		this.errors.getBeanWrapper().registerCustomEditor(requiredType, propertyEditor);
	}

	/**
	 * Register the given custom property editor for the given type and
	 * field, or for all fields of the given type.
	 * @param requiredType type of the property, can be null if a field is
	 * given but should be specified in any case for consistency checking
	 * @param field name of the field (can also be a nested path), or
	 * null if registering an editor for all fields of the given type
	 * @param propertyEditor editor to register
	 */
	public void registerCustomEditor(Class requiredType, String field, PropertyEditor propertyEditor) {
		this.errors.getBeanWrapper().registerCustomEditor(requiredType, field, propertyEditor);
	}

	/**
	 * Bind the given property values to this binder's target.
	 * This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * @param pvs property values to bind
	 */
	public void bind(PropertyValues pvs) {
		// check for missing fields
		if (this.requiredFields != null) {
			for (int i = 0; i < this.requiredFields.length; i++) {
				PropertyValue pv = pvs.getPropertyValue(this.requiredFields[i]);
				if (pv == null || "".equals(pv.getValue())) {
					// create field error with code "required"
					this.errors.addFieldError(
							new FieldError(this.errors.getObjectName(), this.requiredFields[i], "", true, MISSING_FIELD_ERROR_CODE,
														 new Object[] {this.requiredFields[i]}, "Field '" + this.requiredFields[i] + "' is required"));
				}
			}
		}
		try {
			// bind request parameters onto params, ignoring unknown properties
			this.errors.getBeanWrapper().setPropertyValues(pvs, true);
		}
		catch (PropertyAccessExceptionsException ex) {
			PropertyAccessException[] exs = ex.getPropertyAccessExceptions();
			for (int i = 0; i < exs.length; i++) {
				// create field with the exceptions's code, e.g. "typeMismatch"
				this.errors.addFieldError(
						new FieldError(this.errors.getObjectName(), exs[i].getPropertyChangeEvent().getPropertyName(),
													 exs[i].getPropertyChangeEvent().getNewValue(), true, exs[i].getErrorCode(), null,
													 exs[i].getLocalizedMessage()));
			}
		}
	}

	/**
	 * Close this DataBinder, which may result in throwing
	 * a BindException if it encountered any errors
	 * @throws BindException if there were any errors in the bind operation
	 */
	public Map close() throws BindException {
		if (this.errors.hasErrors()) {
			throw this.errors;
		}
		return this.errors.getModel();
	}

}
