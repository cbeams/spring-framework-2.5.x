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

package org.springframework.web.servlet.tags;

import java.beans.PropertyEditor;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.util.HtmlUtils;

/**
 * Simple adapter to expose status of a field or object.
 * Set as a variable by the bind tag. Intended for use by
 * JSP and JSTL expressions, and Velocity or FreeMarker macros.
 * It allows for tag cooperation.
 *
 * <p>Obviously, object status representations do not have an
 * expression and a value but only error codes and messages.
 * For simplicity's sake and to be able to use the same tag,
 * the same status class is used for both scenarios.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Darren Davison
 */
public class BindStatus {
    
    private RequestContext requestContext;

	private Errors errors;

	private PropertyEditor editor;
	
	private boolean htmlEscape;

	private Object value = null;

	private final String[] errorCodes;

	private final String[] errorMessages;

    private final String path;

    private final String expression;

    private final String beanName;
    
    protected final Log logger = LogFactory.getLog(getClass());


	/**
	 * Create a new BindStatus instance,
	 * representing a field or object status.
	 * @param path the bean and property path for which values and errors
	 * will be resolved
	 */
	public BindStatus(RequestContext requestContext, String path, boolean htmlEscape) {
	    this.requestContext = requestContext;
		this.path = path;
		this.htmlEscape = htmlEscape;
		
		// determine name of the object and property
		int dotPos = path.indexOf('.');
		if (dotPos == -1) {
			// property not set, only the object itself
		    beanName = path;
			this.expression = null;
		}
		else {
		    beanName = path.substring(0, dotPos);
			this.expression = path.substring(dotPos + 1);
		}
		
		this.errors = requestContext.getErrors(beanName, false);
		if (this.errors == null) 
		    throw new IllegalStateException("Could not find Errors instance for bean [" + beanName + 
	            "] in request: add the Errors model to your ModelAndView via errors.getModel()");
		
		List fieldErrors = null;

		if (this.expression != null) {
			if ("*".equals(this.expression)) {
				fieldErrors = this.errors.getAllErrors();
			}
			else if (this.expression.endsWith("*")) {
				fieldErrors = this.errors.getFieldErrors(this.expression);
			}
			else {
				fieldErrors = this.errors.getFieldErrors(this.expression);
				value = this.errors.getFieldValue(this.expression);
				if (this.errors instanceof BindException) {
					this.editor = ((BindException) this.errors).getCustomEditor(this.expression);
				}
				else {
					logger.warn("Cannot not expose custom property editor because Errors instance [" + this.errors +
											"] is not of type BindException");
				}
				if (htmlEscape && value instanceof String) {
					value = HtmlUtils.htmlEscape((String) value);
				}
			}
		}
		else {
			fieldErrors = this.errors.getGlobalErrors();
		}
        
		this.errorCodes = getErrorCodes(fieldErrors);
		this.errorMessages = getErrorMessages(fieldErrors);
    }

	/**
	 * Extract the error codes from the given ObjectError list.
	 */
	private String[] getErrorCodes(List fes) {
		String[] codes = new String[fes.size()];
		for (int i = 0; i < fes.size(); i++) {
			ObjectError error = (ObjectError) fes.get(i);
			codes[i] = error.getCode();
		}
		return codes;
	}

	/**
	 * Extract the error messages from the given ObjectError list.
	 */
	private String[] getErrorMessages(List fes) throws NoSuchMessageException {
		String[] messages = new String[fes.size()];
		for (int i = 0; i < fes.size(); i++) {
			ObjectError error = (ObjectError) fes.get(i);
			messages[i] = requestContext.getMessage(error, htmlEscape);
		}
		return messages;
	}

    /**
	 * Return a bind expression that can be used in HTML forms as input name
	 * for the respective field, or null if not field-specific.
	 * <p>Returns a bind path appropriate for resubmission, e.g. "address.street".
	 * Note that the complete bind path as required by the bind tag is
	 * "customer.address.street", if bound to a "customer" bean.
	 */
	public String getExpression() {
		return this.expression;
	}

	/**
	 * Return the current value of the field, i.e. either the property value
	 * or a rejected update, or null if not field-specific.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Return a suitable display value for the field, i.e. empty string
	 * instead of a null value, or null if not field-specific.
	 */
	public String getDisplayValue() {
		return (value != null) ? value.toString() : "";
	}

	/**
	 * Return if this status represents a field or object error.
	 */
	public boolean isError() {
		// need to check array size since BindTag creates empty String[]
		return (errorCodes != null) && (errorCodes.length > 0);
	}

	/**
	 * Return the error codes for the field or object, if any.
	 * Returns an empty array instead of null if none.
	 */
	public String[] getErrorCodes() {
		return (errorCodes != null ? errorCodes : new String[0]);
	}

	/**
	 * Return the first error codes for the field or object, if any.
	 */
	public String getErrorCode() {
		return (errorCodes != null && errorCodes.length > 0 ? errorCodes[0] : "");
	}

	/**
	 * Return the resolved error messages for the field or object,
	 * if any. Returns an empty array instead of null if none.
	 */
	public String[] getErrorMessages() {
		return (errorMessages != null ? errorMessages : new String[0]);
	}

	/**
	 * Return the first error message for the field or object, if any.
	 */
	public String getErrorMessage() {
		return (errorMessages != null && errorMessages.length > 0 ? errorMessages[0] : "");
	}

	/**
	 * Return an error message string, concatenating all messages
	 * separated by the given delimiter.
	 * @param delimiter separator string, e.g. ", " or "<br>"
	 * @return the error message string
	 */
	public String getErrorMessagesAsString(String delimiter) {
		if (errorMessages == null)
			return "";
		return StringUtils.arrayToDelimitedString(errorMessages, delimiter);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("BindStatus: beanName=[").append(beanName)
				.append("]; expression=[").append(expression)
				.append("]; value=[").append(value)
				.append("]");
		if (isError()) {
			sb.append("; error codes='" + errorCodes + "'; error messages='" + errorMessages + "'; ");
		}
		sb.append("; source=" + (isError() ? "error" : "bean"));
		return sb.toString();
	}

    /**
     * @return Returns the beanName.
     */
    public String getBeanName() {
        return beanName;
    }
    
    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * @return Returns the errors.
     */
    public Errors getErrors() {
        return errors;
    }
    
    /**
     * @return Returns the editor.
     */
    public PropertyEditor getEditor() {
        return editor;
    }
    
}
